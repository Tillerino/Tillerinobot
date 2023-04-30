use std::collections::BTreeMap;
use std::env::var;
use std::fmt::Debug;

use lapin::{BasicProperties, Channel, Connection, ConnectionProperties, Consumer, options::*, types::FieldTable};
use lapin::message::Delivery;
use serde::{Deserialize, Serialize};

use crate::Error;

#[derive(Serialize, Debug)]
#[serde(tag = "@c")]
pub (crate) enum GameChatEvent {
	#[serde(rename = ".Joined")]
	Joined {
		#[serde(rename = "eventId")]
		event_id: i64,
		nick: String,
		timestamp: i64,
  },
	#[serde(rename = ".Sighted")]
	Sighted {
		#[serde(rename = "eventId")]
		event_id: i64,
		timestamp: i64,
		nick: String,
	},
	#[serde(rename = ".PrivateMessage")]
	PrivateMessage {
		#[serde(rename = "eventId")]
		event_id: i64,
		nick: String,
		timestamp: i64,
		message: String,
	},
	#[serde(rename = ".PrivateAction")]
	PrivateAction {
		#[serde(rename = "eventId")]
		event_id: i64,
		nick: String,
		timestamp: i64,
		action: String,
	},
}

#[derive(Deserialize, Debug)]
pub (crate) struct RabbitRpcCall<A> {
	method: String,
	args: A,
}

#[derive(Serialize, Debug)]
struct RabbitRpcResponse<O, E> {
	method: String,
	result: RabbitRpcResult<O, E>,
	mdc: BTreeMap<String, String>,
}

#[derive(Serialize, Debug)]
struct RabbitRpcResult<Ok, Err> {
	ok: Option<Ok>,
	err: Option<Err>,
}

impl<O, E> From<Result<O, E>> for RabbitRpcResult<O, E> {
	fn from(res: Result<O, E>) -> Self {
		match res {
			Ok(ok) => Self { ok: Some(ok), err: None },
			Err(err) => Self { ok: None, err: Some(err) },
		}
	}
}

pub(crate) struct RabbitConfig {
	host: String,
	port: u16,
}

impl RabbitConfig {
	pub(crate) async fn connect(&self) -> lapin::Result<Connection> {
		let addr = format!("amqp://{}:{}/%2f", self.host, self.port);

		println!("Connecting to {}", addr);
		let conn = Connection::connect(&addr, ConnectionProperties::default()
			.with_connection_name("tillerinobot-irc".into())).await?;
		println!("Connected to {}", addr);
		Ok(conn)
	}
}

impl Default for RabbitConfig {
	fn default() -> Self {
		Self {
			host: var("RABBIT_HOST").unwrap_or_else(|_| "rabbitmq".into()),
			port: var("RABBIT_PORT").ok().map(|s| str::parse(s.as_str()).expect("unable to parse RABBIT_PORT")).unwrap_or(5672),
    }
	}
}

async fn set_up_rpc_queue(channel: &Channel, queue_name: &str, ttl: i32) -> Result<Consumer, lapin::Error> {
	let mut queue_opts = FieldTable::default();
	queue_opts.insert("x-message-ttl".into(), ttl.into());
	channel.queue_declare(queue_name,
		QueueDeclareOptions { auto_delete: false, durable: true, nowait: true, ..QueueDeclareOptions::default() },
		queue_opts).await?;

	channel.basic_consume(queue_name, "", BasicConsumeOptions::default(), FieldTable::default()).await
}

async fn respond_to_rpc<A, O, E>(channel: &Channel, delivery: Delivery, call: RabbitRpcCall<A>, result: Result<O, E>) -> Result<(), Error> where
	O: Serialize,
	E: Serialize {
	if let (Some(reply_to), Some(correlation_id)) = (delivery.properties.reply_to(), delivery.properties.correlation_id()) {
		let response = RabbitRpcResponse { method: call.method, result: result.into(), mdc: BTreeMap::new() };
		let response_string = serde_json::to_string(&response)?;
		println!("sending response: {}", response_string);
		channel.basic_publish("", reply_to.as_str(), BasicPublishOptions::default(),
			response_string.as_bytes(), BasicProperties::default().with_correlation_id(correlation_id.clone())).await?;
	}
	Ok(channel.basic_ack(delivery.delivery_tag, BasicAckOptions::default()).await?)
}

pub(crate) mod irc_writer {
	use irc::client::Sender;
	use lapin::Channel;
	use serde::Serialize;
	use tokio_stream::StreamExt;

	use crate::Error;
	use crate::ircc::Pinger;
	use crate::rabbit::RabbitRpcCall;

	type IrcWriterCall = RabbitRpcCall<[String; 2]>;

	#[derive(Serialize, Debug)]
	struct IrcWriterOk {
		pub(crate) ping: Option<i64>
	}

	#[derive(Serialize, Debug)]
	#[serde(tag = "@c")]
	pub(crate) enum IrcWriterErr {
		#[serde(rename = ".Unknown")]
		Unknown { },
		#[serde(rename = ".Retry")]
		Retry {
			millis: i64,
		},
		#[serde(rename = ".PingDeath")]
		PingDeath {
			ping: i64,
		},
	}

	impl From<irc::error::Error> for IrcWriterErr {
		fn from(_: irc::error::Error) -> Self {
			IrcWriterErr::Retry { millis: 1000 }
		}
	}

	pub(crate) async fn listen_for_calls(irc_sender: Sender, channel: Channel, mut pinger: Pinger) -> Result<(), Error> {
		let mut consumer = crate::rabbit::set_up_rpc_queue(&channel, "irc_writer", 12000).await?;
		println!("polling rabbit");
		while let Some(delivery) = consumer.next().await.transpose()? {
			let call: IrcWriterCall = serde_json::from_slice(delivery.data.as_slice())?;
			println!("got message from rabbit: {:?}", call);
			let result: Result<IrcWriterOk, IrcWriterErr> = handle_irc_writer_call(&irc_sender, &mut pinger, &call).await;
			crate::rabbit::respond_to_rpc(&channel, delivery, call, result).await?;
		}
		Ok(())
	}

	async fn handle_irc_writer_call(irc_sender: &Sender, pinger: &mut Pinger, call: &IrcWriterCall) -> Result<IrcWriterOk, IrcWriterErr> {
		match call.method.as_str() {
			"message" => {
				let ping = pinger.ping().await?;
				irc_sender.send_privmsg(&call.args[1], &call.args[0])?;
				Ok(IrcWriterOk { ping })
			},
			"action" => {
				let ping = pinger.ping().await?;
				irc_sender.send_action(&call.args[1], &call.args[0])?;
				Ok(IrcWriterOk { ping })
			},
			_ => {
				println!("unknown method: {}, {:?}", call.method, call.args);
				Err(IrcWriterErr::Unknown { })
			}
		}
	}
}

pub(crate) mod game_chat_client {
	use std::sync::{Arc, Mutex};

	use lapin::Channel;
	use serde::Serialize;
	use tokio_stream::StreamExt;

	use crate::Error;

	#[derive(Serialize, Debug, Default, Clone)]
	pub(crate) struct GameChatClientMetrics {
		#[serde(rename = "connected")]
		pub(crate) is_connected: bool,
		#[serde(rename = "runningSince")]
		pub(crate) running_since: i64,
		#[serde(rename = "lastPingDeath")]
		pub(crate) last_ping_death: i64,
		#[serde(rename = "lastInteraction")]
		pub(crate) last_interaction: i64,
		#[serde(rename = "lastReceivedMessage")]
		pub(crate) last_received_message: i64,
	}

	#[derive(Serialize, Debug)]
	#[serde(tag = "@c")]
	pub(crate) enum GameChatClientMetricsErr {
		#[serde(rename = ".Unknown")]
		Unknown { },
	}

	type GameChatClientMetricsCall = crate::rabbit::RabbitRpcCall<[String; 0]>;

	pub(crate) async fn listen_for_calls(channel: Channel, metrics: Arc<Mutex<GameChatClientMetrics>>) -> Result<(), Error> {
		let mut consumer = crate::rabbit::set_up_rpc_queue(&channel, "game_chat_client", 1000).await?;
		println!("polling rabbit");
		while let Some(delivery) = consumer.next().await.transpose()? {
			let call: GameChatClientMetricsCall = serde_json::from_slice(delivery.data.as_slice())?;
			println!("got message from rabbit: {:?}", call);
			let result: Result<GameChatClientMetrics, GameChatClientMetricsErr> = match call.method.as_str() {
				"getMetrics" => {
					Ok(metrics.lock().unwrap().clone())
				},
				_ => Err(GameChatClientMetricsErr::Unknown { })
			};
			crate::rabbit::respond_to_rpc(&channel, delivery, call, result).await?;
		}
		Ok(())
	}

	pub fn now_millis() -> i64 {
		std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap().as_millis() as i64
	}
}
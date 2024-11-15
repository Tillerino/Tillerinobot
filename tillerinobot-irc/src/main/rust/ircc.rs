// file is called ircc not to be confused with the irc crate

use std::env::var;
use std::sync::{Arc, Mutex};

use irc::client::prelude::*;
use tokio::sync::mpsc;
use tokio::sync::mpsc::error::TrySendError;
use uuid::Uuid;
use lapin::{BasicProperties, Channel};
use lapin::options::BasicPublishOptions;
use tokio_stream::StreamExt;
use crate::Error;
use crate::rabbit::game_chat_client::GameChatClientMetrics;

use crate::rabbit::{game_chat_client, GameChatEvent};
use crate::rabbit::irc_writer::IrcWriterErr;

#[derive(Debug, Clone)]
pub(crate) struct IrcConfig {
	pub host: String,
	pub port: u16,
	pub nickname: String,
	pub password: String,
	pub autojoin: Vec<String>,
	pub ignore: bool,
	pub silent: bool,
}

impl Default for IrcConfig {
	fn default() -> Self {
		Self {
			host: var("TILLERINOBOT_IRC_SERVER").unwrap_or("irc.ppy.sh".into()),
			port: var("TILLERINOBOT_IRC_PORT").ok().map(|s| str::parse(s.as_str()).expect("unable to parse TILLERINOBOT_IRC_PORT")).unwrap_or(6667),
			nickname: var("TILLERINOBOT_IRC_NICKNAME").unwrap_or("tillerinobot".into()),
			password: var("TILLERINOBOT_IRC_PASSWORD").unwrap_or("".into()),
			autojoin: var("TILLERINOBOT_IRC_AUTOJOIN").ok().map(|s| s.split(',').map(|s| s.trim().to_string()).collect()).unwrap_or(vec!["#osu".into()]),
			ignore: var("TILLERINOBOT_IRC_IGNORE").ok().map(|s| str::parse(s.as_str()).expect("unable to parse TILLERINOBOT_IRC_IGNORE")).unwrap_or(false),
			silent: var("TILLERINOBOT_IRC_SILENT").ok().map(|s| str::parse(s.as_str()).expect("unable to parse TILLERINOBOT_IRC_SILENT")).unwrap_or(false),
		}
	}
}

impl IrcConfig {
	pub(crate) async fn connect(&self) -> Result<Client, irc::error::Error> {
		println!("Connecting to {}:{} as {}", self.host, self.port, self.nickname);
		let config = Config {
			server: Some(self.host.clone()),
			port: Some(self.port.clone()),
			nickname: Some(self.nickname.clone()),
			password: Some(self.password.clone()),
			channels: self.autojoin.clone(),
			..Config::default()
		};
		Ok(Client::from_config(config).await?)
	}
}

pub struct IrcEventConverter {
	irc_config: IrcConfig,
	event_id_counter: i64,
	metrics: Arc<Mutex<GameChatClientMetrics>>
}

impl IrcEventConverter {
	pub(crate) fn create(irc_config: IrcConfig, metrics: Arc<Mutex<GameChatClientMetrics>>) -> Self {
		Self {
			irc_config,
			event_id_counter: std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap().as_micros() as i64,
			metrics,
		}
	}

	pub (crate) async fn listen_for_incoming_messages(&mut self, mut client: Client, channel: &Channel, ponger: Ponger) -> Result<(), Error> {
		let stream = &mut client.stream()?;
		while let Some(message) = stream.next().await.transpose()? {
			print!("{}", message);
			if self.irc_config.ignore {
				continue;
			}
			for event in self.create_rabbit_event(message, &ponger).await {
				let message = serde_json::to_string(&event)?;
				println!("Sending message to rabbit: {}", message);
				channel.basic_publish("", "irc-reader", BasicPublishOptions::default(), message.as_bytes(), BasicProperties::default()).await?;
			}
		}
		Ok(())
	}

	pub(crate) async fn create_rabbit_event(&mut self, message: Message, pinger: &Ponger) -> Vec<GameChatEvent> {
		self.event_id_counter += 1;
		let event_id = self.event_id_counter;
		let timestamp = game_chat_client::now_millis();

		self.metrics.lock().unwrap().last_interaction = timestamp;
		match message.command {
			Command::PRIVMSG(_, _) => {
				self.metrics.lock().unwrap().last_received_message = timestamp;
			},
			Command::PONG(_, Some(msg)) if Uuid::try_parse(&msg).is_ok() => {
				pinger.pong(msg.clone()).await;
				return vec![];
			},
			// Bancho IRC puts the identifier in a different place of the command than NgIRCd
			Command::PONG(msg, _) if Uuid::try_parse(&msg).is_ok() => {
				pinger.pong(msg.clone()).await;
				return vec![];
			},
			Command::Response(Response::RPL_NAMREPLY, args) => {
				return self.convert_names_event(timestamp, args);
			},
			_ => {}
		}

		let nick = match message.prefix {
			Some(Prefix::Nickname(a, _, _)) if a == self.irc_config.nickname => {
				format!("message prefix was own nick {}", a);
				return vec![]
			},
			Some(Prefix::Nickname(a, _, _)) => a,
			Some(Prefix::ServerName(a)) => {
				format!("message prefix was server {}", a);
				return vec![]
			},
			None => {
				format!("message prefix was None");
				return vec![]
			},
		};

		match message.command {
			Command::PRIVMSG(target, msg) if target == self.irc_config.nickname
					&& msg.starts_with("\u{1}ACTION ") && msg.ends_with("\u{1}") =>
				vec![GameChatEvent::PrivateAction { event_id, nick, timestamp, action: msg[8..msg.len() - 1].to_owned() }],
			Command::PRIVMSG(target, msg) if target == self.irc_config.nickname =>
				vec![GameChatEvent::PrivateMessage { event_id, nick, timestamp, message: msg }],
			Command::JOIN(_, _, _) => vec![GameChatEvent::Joined { event_id, nick, timestamp }],
			_ => vec![GameChatEvent::Sighted { event_id, nick, timestamp }],
		}
	}

	fn convert_names_event(&mut self, timestamp: i64, args: Vec<String>) -> Vec<GameChatEvent> {
		match args.get(3) {
			// broken names response?
			None => vec![],
			Some(str) => {
				str
					.split(" ")
					.map(|a| if a.starts_with("+") || a.starts_with("@") { a[1..].to_owned() } else { a.to_owned() })
					.filter(|a| a != &self.irc_config.nickname)
					.map(|a| {
						self.event_id_counter += 1;
						let event_id = self.event_id_counter;
						GameChatEvent::Sighted { event_id, nick: a.to_owned(), timestamp }
					})
					.collect()
			}
		}
	}
}

pub(crate) struct Pinger {
	counter: u64,
	sender: Sender,
	rx: mpsc::Receiver<String>,
	metrics: Arc<Mutex<GameChatClientMetrics>>,
}

pub(crate) struct Ponger {
	tx: mpsc::Sender<String>,
}

pub(crate) fn create_pinger(sender: Sender, metrics: Arc<Mutex<GameChatClientMetrics>>) -> (Pinger, Ponger) {
	let (tx, rx) = mpsc::channel(100);
	(Pinger { counter: 0, sender, rx, metrics }, Ponger { tx })
}

impl Pinger {
	pub(crate) async fn ping(&mut self) -> Result<Option<i64>, IrcWriterErr> {
		self.counter += 1;
		if self.counter % 10 != 0 {
			return Ok(None);
		}
		let marker = Uuid::new_v4().to_string();
		match self.sender.send(Command::PING(marker.clone(), None)) {
			Ok(_) => {},
			Err(e) => {
				println!("ping send error: {}", e);
				return Err(IrcWriterErr::Retry { millis: 1000 });
			},
		}
		let start = tokio::time::Instant::now();
		let deadline = start + std::time::Duration::from_secs(10);
		loop {
			let result = tokio::time::timeout_at(deadline, self.rx.recv()).await;
			match result {
				Ok(Some(msg)) if msg == marker => {
					return Ok(Some((tokio::time::Instant::now() - start).as_millis() as i64))
				},
				Ok(maybe) => {
					println!("ping got something else: {}", maybe.unwrap_or("None".to_owned()));
					continue;
				},
				_ => {
					self.metrics.lock().unwrap().last_ping_death = game_chat_client::now_millis();
					return Err(IrcWriterErr::PingDeath { ping: (tokio::time::Instant::now() - start).as_millis() as i64 })
				}
			}
		}
	}
}

impl Ponger {
	async fn pong(&self, msg: String) {
		match self.tx.try_send(msg) {
			Ok(_) => {  },
			Err(TrySendError::Full(msg)) => println!("ERROR: pong queue is full: {}", msg.to_string()),
			Err(TrySendError::Closed(msg)) => println!("ERROR: pong queue kakki: {}", msg.to_string()),
		}
	}
}

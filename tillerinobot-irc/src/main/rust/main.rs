use std::sync::{Arc, Mutex};
use std::sync::atomic::{AtomicBool, Ordering};
use std::time::Duration;

use irc::client::{Client, Sender};
use lapin::{Channel, ConnectionStatus};
use serde_json;
use thiserror::Error;
use tokio::signal;
use tokio::signal::unix::SignalKind;

use ircc::{IrcConfig, IrcEventConverter};

use crate::rabbit::{game_chat_client, RabbitConfig};

mod rabbit;
mod ircc;
mod probes;

#[derive(Error, Debug)]
enum Error {
	#[error("IRC error: {0}")]
	Irc(#[from] irc::error::Error),
	#[error("RabbitMQ error: {0}")]
	Rabbit(#[from] lapin::Error),
	#[error("Serde error: {0}")]
	Serde(#[from] serde_json::Error),
	#[error("IO error: {0}")]
	Io(#[from] std::io::Error),
}

#[tokio::main(flavor = "current_thread")]
async fn main() -> Result<(), Error> {
	let probes_config = probes::HttpConfig::default();

	let main = Main::default();
	let probes_fut = probes_config.start(main.rabbit_connected.clone(), main.metrics.clone());

	let connect_fut = main.connect_loop();

	tokio::select! {
		_ = probes_fut => {}
		r = connect_fut => { r?; }
	}
	Ok(())
}

#[derive(Default)]
struct Main {
	irc_config: IrcConfig,
	rabbit_config: RabbitConfig,
	rabbit_connected: Arc<Mutex<Option<ConnectionStatus>>>,
	quit: Arc<AtomicBool>,
	metrics: Arc<Mutex<rabbit::game_chat_client::GameChatClientMetrics>>,
}

impl Main {
	async fn connect_loop(self: &Self) -> Result<(), Error> {
		while !self.quit.load(Ordering::Relaxed) {
			let result = self.connect_once().await;
			match result {
				Ok(()) => {
					// When the IRC connection is closed, the loop ends naturally :shrug:
					println!("Connect loop ended.");
				},
				Err(Error::Irc(e)) => {
					println!("Irc Error ({}).", e);
				},
				Err(Error::Rabbit(e)) => {
					println!("Rabbit Error ({}).", e);
				},
				// other errors are not recoverable
				Err(e) => {
					println!("Error. Not reconnecting. ({})", e);
					return Err(e)
				},
			}
			if !self.quit.load(Ordering::Relaxed) {
				tokio::time::sleep(Duration::from_secs(1)).await;
			}
		}
		Ok(())
	}

	async fn connect_once(self: &Self) -> Result<(), Error> {
		let channel = self.connect_rabbit().await?;

		tokio::select! {
			r = rabbit::game_chat_client::listen_for_calls(channel.clone(), self.metrics.clone()) => { r?; }
			r = self.connect_once_irc(channel) => { r?; }
		}
		Ok(())
	}

	async fn connect_once_irc(self: &Self, channel: Channel) -> Result<(), Error> {
		let client = self.connect_irc().await?;
		let mut irc_converter = IrcEventConverter::create(self.irc_config.clone(), self.metrics.clone());

		let (pinger, ponger) = ircc::create_pinger(client.sender().clone(), self.metrics.clone());
		tokio::select! {
			r = killer(client.sender().clone(), channel.clone(), self.quit.clone()) => { r?; }
			r = rabbit::irc_writer::listen_for_calls(client.sender().clone(), channel.clone(), pinger) => { r?; }
			r = irc_converter.listen_for_incoming_messages(client, channel, ponger) => {
				{
					let mut metrics = self.metrics.lock().unwrap();
					metrics.is_connected = false;
				}
				r?;
			}
		}
		Ok(())
	}

	async fn connect_irc(self: &Self) -> Result<Client, irc::error::Error> {
		println!("connecting to IRC");
		let client = self.irc_config.connect().await?;
		println!("connected to IRC");
		client.identify()?;
		println!("identified on IRC");
		{
			let mut metrics = self.metrics.lock().unwrap();
			metrics.is_connected = true;
			metrics.running_since = game_chat_client::now_millis();
		}
		Ok(client)
	}

	async fn connect_rabbit(self: &Self) -> Result<Channel, lapin::Error> {
		let rabbit_connection = self.rabbit_config.connect().await?;
		let channel = rabbit_connection.create_channel().await?;
		{
			let mut r = self.rabbit_connected.lock().unwrap();
			*r = Some(rabbit_connection.status().clone());
		}
		Ok(channel)
	}
}

async fn killer(sender: Sender, channel: Channel, quit: Arc<AtomicBool>) -> Result<(), Error> {
	match signal::unix::signal(SignalKind::terminate()) {
		Ok(mut stream) => {
			while let Some(_) = stream.recv().await {
				println!("Received quit signal. Closing all connections.");
				quit.store(true, Ordering::Relaxed);
				sender.send_quit("Shutting down.")?;
				channel.close(200, "Received quit signal").await?;
			}
		},
		Err(e) => {
			println!("Error setting up signal handler: {}", e);
		}
	}
	Ok(())
}

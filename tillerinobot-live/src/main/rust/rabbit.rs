use std::sync::Mutex;

use futures_util::stream::StreamExt;
use lapin::{Channel, ChannelStatus, Connection, ConnectionProperties, Consumer, ExchangeKind, message::Delivery, options::*, types::FieldTable};
use serde::{Deserialize, Serialize};
use sha2::Digest;
use tokio::sync::mpsc::error::TrySendError;
use tokio_amqp::LapinTokioExt;
use warp::filters::ws::Message;

use crate::websocket::{Conn, CONNECTIONS};

lazy_static! {
    pub static ref READY: Mutex<ChannelStatus> = Mutex::default();
}

#[derive(Deserialize, Debug)]
#[serde(tag = "@type")]
pub enum RabbitMessage {
    #[serde(rename="RECEIVED")]
    Received {
        #[serde(rename="eventId")]
        event_id: u64,
        #[serde(rename="ircUserName")]
        irc_user_name: String
    },
    #[serde(rename="SENT")]
    Sent {
        #[serde(rename="eventId")]
        event_id: u64,
        #[serde(rename="ircUserName")]
        irc_user_name: String,
        #[serde(skip_serializing_if = "Option::is_none")]
        ping: Option<i32>
    },
    #[serde(rename="RECEIVED_DETAILS")]
    ReceivedDetails {
        #[serde(rename="eventId")]
        event_id: u64,
        text: String
    }
}

#[derive(Serialize, Debug)]
enum FrontendMessage {
    #[serde(rename="received")]
    Received {
        #[serde(rename="eventId")]
        event_id: u64,
        user: i32
    },
    #[serde(rename="sent")]
    Sent {
        #[serde(rename="eventId")]
        event_id: u64,
        user: i32,
        #[serde(skip_serializing_if = "Option::is_none")]
        ping: Option<i32>
    },
    #[serde(rename="messageDetails")]
    MessageDetails {
        #[serde(rename="eventId")]
        event_id: u64,
        message: String
    }
}

pub(crate) async fn run_rabbit() {
    loop {
        match consume_rabbit().await {
            Ok(_) => println!("rabbit body succeeded. what the hell?"),
            Err(e) => println!("Rabbit error: {}", e)
        }
    }
}

async fn consume_rabbit() -> lapin::Result<()> {
    let channel_a = connect_rabbit_channel().await?;
    channel_a.basic_qos(100, BasicQosOptions::default()).await?;
    let mut consumer = create_rabbit_consumer(channel_a).await?;
    println!("Listening to events");
    while let Some(Ok((_, delivery))) = consumer.next().await {
        delivery.ack(BasicAckOptions::default()).await?;
        if let Err(e) = consume_single(delivery) {
            println!("Error consuming event: {}", e);
        }
    }
    Ok(())
}

async fn connect_rabbit_channel() -> lapin::Result<Channel> {
    let rabbit_host = std::env::var("RABBIT_HOST").unwrap_or_else(|_| "rabbitmq".into());
    let rabbit_port: u16 = std::env::var("RABBIT_PORT").ok().and_then(|s| str::parse(s.as_str()).ok()).unwrap_or(5672);
    let addr = format!("amqp://{}:{}/%2f", rabbit_host, rabbit_port);

    println!("Connecting to {}", addr);
    let conn = Connection::connect(&addr, ConnectionProperties::default().with_tokio()).await?;
    println!("Connected to {}", addr);
    let channel_a = conn.create_channel().await?;
    {
        let mut r = READY.lock().unwrap();
        *r = channel_a.status().clone();
    }
    Ok(channel_a)
}

async fn create_rabbit_consumer(channel_a: Channel) -> lapin::Result<Consumer> {
    channel_a.exchange_declare("live-activity", ExchangeKind::Fanout, ExchangeDeclareOptions::default(), FieldTable::default()).await?;

    let options = QueueDeclareOptions { passive: false, durable: false, exclusive: true, auto_delete: true, nowait: false };
    let q = channel_a.queue_declare("", options, FieldTable::default()).await?;
    channel_a.queue_bind(q.name().as_str(), "live-activity", "", QueueBindOptions::default(), FieldTable::default()).await?;

    channel_a.basic_consume(q.name().as_str(), "live-activity", BasicConsumeOptions::default(), FieldTable::default()).await
}

fn consume_single(delivery: Delivery) -> serde_json::Result<()> {
    let str: RabbitMessage = serde_json::from_slice(delivery.data.as_ref())?;

    // remove broken connections while iterating
    CONNECTIONS.lock().unwrap().retain(|c| {
        match c.web.try_send(Ok(Message::text(serde_json::to_string(&convert_message(&c, &str)).unwrap()))) {
            Ok(_) => true,
            Err(TrySendError::Full(_)) => true, // keep the connection for now
            Err(TrySendError::Closed(_)) => { println!("Dropping connection {}", c.salt); false }
        }
    });

    Ok(())
}

fn convert_message(conn: &Conn, msg: &RabbitMessage) -> FrontendMessage {
    match msg {
        RabbitMessage::Received { event_id, irc_user_name } => FrontendMessage::Received { event_id: *event_id, user: anonymize_user_id(&conn, irc_user_name) },
        RabbitMessage::Sent { event_id, irc_user_name, ping} => FrontendMessage::Sent { event_id: *event_id, user: anonymize_user_id(&conn, irc_user_name), ping: *ping },
        RabbitMessage::ReceivedDetails { event_id, text } => FrontendMessage::MessageDetails { event_id: *event_id, message: text.to_string() }
    }
}

fn anonymize_user_id(conn: &Conn, name: &String) -> i32 {
    let mut digest = sha2::Sha512::new();
    digest.update(name);
    digest.update(conn.salt.to_be_bytes());
    let mut truncated: [u8; 4] = [ 0, 0, 0, 0 ];
    truncated.clone_from_slice(&digest.finalize()[0..4]);
    i32::from_le_bytes(truncated)
}
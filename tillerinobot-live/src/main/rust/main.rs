#![feature(drain_filter)]

#[macro_use]
extern crate lazy_static;

use std::sync::Mutex;

use futures_util::{FutureExt, stream::StreamExt};
use lapin::{Channel, ChannelStatus, Connection, ConnectionProperties, Consumer, ExchangeKind, message::Delivery, options::*, types::FieldTable};
use rand_chacha::ChaChaRng;
use rand_core::{RngCore, SeedableRng};
use serde::{Deserialize, Serialize};
use serde_json;
use sha2::Digest;
use tokio::sync::{mpsc, mpsc::error::TrySendError};
use tokio_amqp::LapinTokioExt;
use tokio_stream::wrappers::ReceiverStream;
use warp::{Filter, filters::ws::Message, http::StatusCode};

struct Conn {
    web: mpsc::Sender<Result<Message, warp::Error>>,
    salt: u64,
}

#[derive(Deserialize, Debug)]
#[serde(tag = "@type")]
enum RabbitMessage {
    #[serde(rename="RECEIVED")] Received { #[serde(rename="eventId")] event_id: u64, #[serde(rename="ircUserName")] irc_user_name: String },
    #[serde(rename="SENT")] Sent { #[serde(rename="eventId")] event_id: u64, #[serde(rename="ircUserName")] irc_user_name: String, ping: Option<i32> },
    #[serde(rename="RECEIVED_DETAILS")] ReceivedDetails { #[serde(rename="eventId")] event_id: u64, text: String }
}

#[derive(Serialize, Debug)]
enum FrontendMessage {
    #[serde(rename="received")] Received { #[serde(rename="eventId")] event_id: u64, user: i32 },
    #[serde(rename="sent")] Sent { #[serde(rename="eventId")] event_id: u64, user: i32, ping: Option<i32> },
    #[serde(rename="messageDetails")] MessageDetails { #[serde(rename="eventId")] event_id: u64, message: String }
}


lazy_static! {
    static ref CONNECTIONS: Mutex<Vec<Conn>> = Mutex::new(vec![]);
    static ref RND: Mutex<ChaChaRng> = Mutex::new(ChaChaRng::from_entropy());
    static ref READY: Mutex<ChannelStatus> = Mutex::default();
}

#[tokio::main]
async fn main() {
    // quit if one of these exits, although they really shouldn't
    tokio::select!(
        _ = tokio::spawn(run_rabbit()) => (),
        _ = run_http() => ())
}

async fn run_rabbit() {
    loop {
        match consume_rabbit().await {
            Ok(_) => println!("rabbit body succeeded. what the hell?"),
            Err(e) => println!("Rabbit error: {}", e)
        }
    }
}

async fn consume_rabbit() -> lapin::Result<()> {
    let channel_a = connect_rabbit_channel().await?;
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
    CONNECTIONS.lock().unwrap().drain_filter(|c| {
        match c.web.try_send(Ok(Message::text(serde_json::to_string(&convert_message(&c, &str)).unwrap()))) {
            Ok(_) => false,
            Err(TrySendError::Full(_)) => false, // keep the connection for now
            Err(TrySendError::Closed(_)) => { println!("Dropping connection {}", c.salt); true }
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

async fn run_http() {
    let websocket = warp::path!("live" / "v0")
        .and(warp::ws())
        .map(|ws: warp::ws::Ws| {
            ws.max_send_queue(100).on_upgrade(|web| async {
                // Just echo all messages back...
                let (sink, _) = web.split();
                let salt = {
                    let mut rnd = RND.lock().unwrap();
                    rnd.next_u64()
                };

                let mut all = CONNECTIONS.lock().unwrap();

                let (tx, rx) = mpsc::channel(100);
                let rx = ReceiverStream::new(rx);
                tokio::task::spawn(rx.forward(sink).map(|result| {
                    if let Err(e) = result {
                        eprintln!("websocket send error: {}", e);
                    }
                }));

                all.push(Conn { web: tx, salt })
            })
        });

    let liveness = warp::get()
        .and(warp::path!("live"))
        .map(|| {
            "live\n"
        });

    let readiness = warp::get()
        .and(warp::path!("ready"))
        .map(|| {
            if READY.lock().unwrap().connected() {
                warp::reply::with_status("ready\n", StatusCode::OK)
            } else {
                warp::reply::with_status("not ready\n", StatusCode::NOT_FOUND)
            }
        });

    warp::serve(websocket.or(liveness).or(readiness)).run(([0, 0, 0, 0], 8080)).await;
}


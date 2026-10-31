use std::net::SocketAddr;
use std::sync::Mutex;

use axum::Router;
use axum::extract::ws::{Message, WebSocketUpgrade};
use axum::http::StatusCode;
use axum::routing::get;
use futures_util::{FutureExt, stream::StreamExt};
use getrandom::SysRng;
use rand_chacha::ChaChaRng;
use rand_core::{Rng, SeedableRng};
use tokio::sync::mpsc;
use tokio_stream::wrappers::ReceiverStream;

use crate::rabbit::READY;

lazy_static! {
    static ref RND: Mutex<ChaChaRng> = Mutex::new(ChaChaRng::try_from_rng(&mut SysRng).unwrap());
    pub static ref CONNECTIONS: Mutex<Vec<Conn>> = Mutex::new(vec![]);
}

pub struct Conn {
    pub web: mpsc::Sender<Result<Message, axum::Error>>,
    pub salt: u64,
}

pub async fn run_http() -> () {
    let websocket = get(|ws: WebSocketUpgrade| async move {
        // Clients only send "PING", so tungstenite's 128KiB read buffer per connection would be waste.
        ws.read_buffer_size(4 * 1024)
            .max_write_buffer_size(256 * 1024)
            .on_upgrade(|web| async {
                // Just echo all messages back...
                let (sink, mut source) = web.split();
                let salt = {
                    let mut rnd = RND.lock().unwrap();
                    rnd.next_u64()
                };

                let (tx, rx) = mpsc::channel(100);
                let rx = ReceiverStream::new(rx);
                tokio::task::spawn(rx.forward(sink).map(|result| {
                    if let Err(e) = result {
                        eprintln!("websocket send error: {}", e);
                    }
                }));

                {
                    let mut all = CONNECTIONS.lock().unwrap();
                    all.push(Conn { web: tx.clone(), salt });
                }

                while let Some(x) = source.next().await {
                    match x {
                        Ok(msg) => if let Ok(str) = msg.to_text() { if str.eq_ignore_ascii_case("PING") { tx.send(Ok(Message::Text("PONG".into()))).await; } }
                        Err(_) => {}
                    }
                }
            })
    });

    let liveness = get(|| async {
        "live\n"
    });

    let readiness = get(|| async {
        if READY.lock().unwrap().as_ref().is_some_and(|status| status.connected()) {
            (StatusCode::OK, "ready\n")
        } else {
            (StatusCode::NOT_FOUND, "not ready\n")
        }
    });

    let all_routes = Router::new()
        .route("/live/v0", websocket)
        .route("/live", liveness)
        .route("/ready", readiness);

    let listener = tokio::net::TcpListener::bind(SocketAddr::from(([0, 0, 0, 0], 8080))).await.unwrap();
    axum::serve(listener, all_routes).await.unwrap()
}

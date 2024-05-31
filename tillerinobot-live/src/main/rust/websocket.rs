use std::sync::Mutex;

use futures_util::{FutureExt, stream::StreamExt};
use rand_chacha::ChaChaRng;
use rand_core::{RngCore, SeedableRng};
use tokio::sync::mpsc;
use tokio_stream::wrappers::ReceiverStream;
use warp::{Filter, http::StatusCode};
use warp::ws::Message;

use crate::rabbit::READY;

lazy_static! {
    static ref RND: Mutex<ChaChaRng> = Mutex::new(ChaChaRng::from_entropy());
    pub static ref CONNECTIONS: Mutex<Vec<Conn>> = Mutex::new(vec![]);
}

pub struct Conn {
    pub web: mpsc::Sender<Result<Message, warp::Error>>,
    pub salt: u64,
}

pub async fn run_http() -> () {
    let websocket = warp::path!("live" / "v0")
        .and(warp::ws())
        .map(|ws: warp::ws::Ws| {
            ws.max_send_queue(100).on_upgrade(|web| async {
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
                        Ok(msg) => if let Ok(str) = msg.to_str() { if str.eq_ignore_ascii_case("PING") { tx.send(Ok(Message::text("PONG"))).await; } }
                        Err(_) => {}
                    }
                }
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

    let all_routes = websocket.or(liveness).or(readiness);

    warp::serve(all_routes)
        .run(([0, 0, 0, 0], 8080))
        .await
}

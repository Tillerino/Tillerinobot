#[macro_use]
extern crate lazy_static;

mod rabbit;
mod websocket;

use rabbit::run_rabbit;
use websocket::run_http;

#[tokio::main(worker_threads = 1)] // run everything in a single thread
async fn main() {
    // quit if one of these exits, although they really shouldn't
    tokio::select!(
        _ = run_rabbit() => (),
        _ = run_http() => ())
}

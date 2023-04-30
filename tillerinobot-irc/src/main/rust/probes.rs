use lapin::ConnectionStatus;
use std::sync::{Arc, Mutex};
use warp::http::StatusCode;
use warp::{reply, Filter};
use crate::rabbit::game_chat_client::GameChatClientMetrics;

pub(crate) struct HttpConfig {
	port: u16,
}

impl Default for HttpConfig {
	fn default() -> Self {
		Self {
			port: option_env!("HTTP_PORT").unwrap_or("8080").parse().expect("unable to parse TILLERINOBOT_HTTP_PORT"),
		}
	}
}

impl HttpConfig {
	pub(crate) async fn start(&self, rabbit_status: Arc<Mutex<Option<ConnectionStatus>>>, metrics: Arc<Mutex<GameChatClientMetrics>>) {
		warp::serve(warp::path("live").map(move || {
			let lock = rabbit_status.lock().unwrap();
			if !lock.clone().map(|s| s.connected()).unwrap_or(false) {
				return reply::with_status("RabbitMQ not connected", StatusCode::SERVICE_UNAVAILABLE);
			}
			if !metrics.lock().unwrap().is_connected {
				return reply::with_status("IRC not connected", StatusCode::SERVICE_UNAVAILABLE);
			}
			return reply::with_status("OK", StatusCode::OK);
		}))
			.run(([0, 0, 0, 0], self.port))
			.await;
	}
}

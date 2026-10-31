use axum::Router;
use axum::http::StatusCode;
use axum::routing::get;
use lapin::ConnectionStatus;
use std::net::SocketAddr;
use std::sync::{Arc, Mutex};
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
		let live = get(move || {
			let rabbit_status = rabbit_status.clone();
			let metrics = metrics.clone();
			async move {
				let lock = rabbit_status.lock().unwrap();
				if !lock.clone().map(|s| s.connected()).unwrap_or(false) {
					return (StatusCode::SERVICE_UNAVAILABLE, "RabbitMQ not connected");
				}
				if !metrics.lock().unwrap().is_connected {
					return (StatusCode::SERVICE_UNAVAILABLE, "IRC not connected");
				}
				return (StatusCode::OK, "OK");
			}
		});

		let listener = tokio::net::TcpListener::bind(SocketAddr::from(([0, 0, 0, 0], self.port))).await.unwrap();
		axum::serve(listener, Router::new().route("/live", live)).await.unwrap();
	}
}

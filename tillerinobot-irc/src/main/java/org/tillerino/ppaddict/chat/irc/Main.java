package org.tillerino.ppaddict.chat.irc;

import io.undertow.Undertow;
import io.undertow.util.Headers;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
	public static void main(String[] args) {
		log.info("starting irc");
		Undertow server = Undertow.builder()
			.addHttpListener(8080, "0.0.0.0")
			.setHandler(exchange -> {
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				exchange.getResponseSender().send("Hello World");
			})
			.setIoThreads(1)
			.setWorkerThreads(1)
			.build();
		server.start();
	}
}

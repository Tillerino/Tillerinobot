package org.tillerino.ppaddict.live;

import static org.assertj.core.api.Assertions.assertThat;
import static org.tillerino.ppaddict.live.LiveContainer.getLive;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.rabbit.RabbitMqConfiguration;
import org.tillerino.ppaddict.rabbit.RabbitMqContainerConnection;

import lombok.extern.slf4j.Slf4j;

/**
 * We just want to stress the container a bit. It is supposed to run with 64MiB memory.
 *
 * I think I messed up the test, because everything seems crazy efficient.
 * After warm up, the throughput is around 100k messages per second with 100 clients.
 * There appears to be a GC pause in the tens of ms once or twice a second.
 * Chunk sizes 10 and 100 are similar here w.r.t. speed.
 *
 * 1000 clients work, but we see pretty consistent garbage collection pauses of 50ms.
 *
 * 10000 clients lead to OOMs
 */
@Slf4j
public class MemoryIT {
	private final int numberOfClients = 100;

	// turn this up for real testing
	private final int messages = 1000;

	int chunkSize = 100;

	int readerBufferSize = 100;

	private final List<WebSocketClient> webSocketClients = IntStream.range(0, numberOfClients)
			.mapToObj(x -> new WebSocketClient())
			.toList();

	@WebSocket
	public class CollectingWebSocketClient {
		BlockingQueue<String> messages = new ArrayBlockingQueue<>(readerBufferSize);
		@OnWebSocketMessage
		public void message(String text) throws InterruptedException {
			messages.put(text);
		}
	}

	private List<CollectingWebSocketClient> clients = new ArrayList<>();

	@RegisterExtension
	public final RabbitMqContainerConnection rabbitMq = new RabbitMqContainerConnection(null);

	LiveActivity source;

	@BeforeEach
	public void setUp() throws Exception {
		source = RabbitMqConfiguration.liveActivity(rabbitMq.getConnection());
		URI uri = new URI("ws://" + getLive().getContainerIpAddress() + ":" + getLive().getMappedPort(8080) + "/live/v0");
		log.info("Connecting clients");
		Thread.sleep(1000); // flaky without :/
		for (WebSocketClient webSocketClient: webSocketClients) {
			webSocketClient.start();
			CollectingWebSocketClient client = new CollectingWebSocketClient();
			Future<Session> connect = webSocketClient.connect(client, uri);
			connect.get(10, TimeUnit.SECONDS);
			clients.add(client);
		}
	}

	@AfterEach
	public void tearDown() throws Exception {
		for (WebSocketClient webSocketClient: webSocketClients) {
			webSocketClient.stop();
		}
	}

	@Test
	public void throughput() throws InterruptedException {
		for (int i = 0; i < messages; i += chunkSize) {
			long start = System.currentTimeMillis();
			System.out.print("s...\t");
			for (int j = 0; j < chunkSize; j++) {
				source.propagateReceivedMessage("yoosr", i + j);
			}
			System.out.print("s!\t");
			for (int j = 0; j < chunkSize; j++) {
				String expected = "\"received\":{\"eventId\":" + (i + j) + ",";
				for (CollectingWebSocketClient client: clients) {
					String msg = client.messages.poll(1, TimeUnit.SECONDS);
					assertThat(msg).contains(expected);
				}
			}
			System.out.println(i + "\t" + chunkSize * numberOfClients * 1000D / (System.currentTimeMillis() - start));
		}
	}
}

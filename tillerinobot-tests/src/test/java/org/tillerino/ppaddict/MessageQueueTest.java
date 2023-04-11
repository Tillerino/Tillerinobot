package org.tillerino.ppaddict;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.MDC;
import org.tillerino.ppaddict.chat.Sighted;
import org.tillerino.ppaddict.rabbit.RabbitMqConfiguration;
import org.tillerino.ppaddict.rabbit.RabbitMqContainerConnection;
import org.tillerino.ppaddict.rabbit.RemoteEventQueue;

public class MessageQueueTest {
	@ClassRule
	public static final RabbitMqContainerConnection rabbit = new RabbitMqContainerConnection(null);

	/**
	 * The largest burst of messages is when the bot gets an overview of all online players.
	 * We need to make sure that this works reasonably fast.
	 */
	@Test
	public void speed() throws Exception {
		RemoteEventQueue eventQueue = RabbitMqConfiguration.eventQueue(rabbit.getConnection());
		eventQueue.setup();

		AtomicInteger received = new AtomicInteger();
		eventQueue.subscribe(x -> received.incrementAndGet());

		for (long i = 0, event = System.currentTimeMillis(); i < 15000; i++) {
			MDC.put("eventId", "" + event++);
			MDC.put("pircbotx.id", "1");
			MDC.put("pircbotx.server", "irc.ppy.sh");
			MDC.put("pircbotx.port", "3306");
			eventQueue.onEvent(new Sighted(event, "nickname", System.currentTimeMillis()));
		}

		await().untilAtomic(received, equalTo(15000));
	}
}

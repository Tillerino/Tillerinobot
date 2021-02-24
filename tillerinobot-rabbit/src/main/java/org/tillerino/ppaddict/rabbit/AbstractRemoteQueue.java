package org.tillerino.ppaddict.rabbit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class AbstractRemoteQueue<T> {
	private final ObjectMapper mapper;
	private final Channel channel;
	private final String exchange;
	private final String queue;
	private final Logger log;
	private final Class<T> cls;
	private final Integer maxPriority;

	protected void send(T event) {
		send(event, null);
	}

	protected void send(T event, Integer priority) {
		if (priority != null) {
			if (priority < 0 || maxPriority == null || priority > maxPriority) {
				throw new IllegalArgumentException();
			}
		}
		try {
			BasicProperties properties = new BasicProperties.Builder().priority(priority).build();
			channel.basicPublish(exchange, queue, properties, mapper.writeValueAsBytes(event));
		} catch (IOException e) {
			log.warn("Unable to queue message. Dropping message {}", event, e);
		}
	}

	protected int size() {
		try {
			return (int) channel.messageCount(queue);
		} catch (IOException e) {
			log.warn("Unable to determine size of queue {}. Returning 0", queue, e);
			return 0;
		}
	}

	/**
	 * Must be called before the first call to {@link #send(Object, int)} or {@link #subscribe(Consumer)}.
	 */
	public void setup() throws IOException {
		if (StringUtils.isBlank(exchange) == StringUtils.isBlank(queue)) {
			throw new IllegalArgumentException("Exactly one of exchange and queue must be set.");
		}
		if (StringUtils.isNotBlank(exchange)) {
			channel.exchangeDeclare(exchange, BuiltinExchangeType.FANOUT);
		}
		if (StringUtils.isNotBlank(queue)) {
			Map<String, Object> arguments = new HashMap<>();
			arguments.put("x-message-ttl", 60000);
			if (maxPriority != null) {
				arguments.put("x-max-priority", maxPriority);
			}
			channel.queueDeclare(queue, true, false, false, arguments);
		}
	}

	/**
	 * Subscribe to this queue or exchange. You will be running on RabbitMQ's own thread pool until the VM is interrupted.
	 */
	public void subscribe(Consumer<T> consumer) throws IOException {
		ObjectReader reader = mapper.readerFor(cls);
		String key;
		if (StringUtils.isNotBlank(queue)) {
			key = queue;
		} else {
			DeclareOk declared = channel.queueDeclare("", false, true, true, null);
			key = declared.getQueue();
			channel.queueBind(key, exchange, queue);
		}
		channel.basicConsume(key, false, (consumerTag, message) -> {
			/* We ack manually to be sure that prefetched values are not acked. */
			channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
			/* The default exception handler (which appears to be ForgivingExceptionHandler)
			* closes the channel when an exception is thrown here and just never recovers.
			* We could mess around with other handlers or just catch everything here. */
			T parsed;
			try {
				parsed = reader.readValue(message.getBody());
			} catch (Exception e) {
				log.error("Error parsing payload from {}", key, e);
				return;
			}
			try {
				consumer.accept(parsed);
			} catch (Exception e) {
				log.error("Error handling payload from {}", key, e);
			}
		}, consumerTag -> log.error("Consumer has been cancelled: {}", consumerTag));
	}
}

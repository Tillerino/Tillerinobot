package org.tillerino.ppaddict.rabbit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
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

	protected void send(T event) {
		try {
			channel.basicPublish(exchange, queue, null, mapper.writeValueAsBytes(event));
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
	 * Must be called before the first call to {@link #send(Object)} or {@link #subscribe(Consumer)}.
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
		channel.basicConsume(key, (consumerTag, message) -> {
			consumer.accept(reader.readValue(message.getBody()));
		}, consumerTag -> log.error("Consumer has been cancelled: {}", consumerTag));
	}
}

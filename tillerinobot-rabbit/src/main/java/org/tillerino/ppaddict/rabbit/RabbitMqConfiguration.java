package org.tillerino.ppaddict.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMqConfiguration {

	public static ConnectionFactory connectionFactory(String hostName, int portNumber) {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(hostName);
		factory.setPort(portNumber);
		return factory;
	}

	public static ObjectMapper mapper() {
		return new ObjectMapper().registerModule(new ParameterNamesModule());
	}

	public static RemoteEventQueue eventQueue(Channel channel) {
		return new RemoteEventQueue(mapper(), channel, "", "game-chat-events");
	}

	public static RemoteResponseQueue responseQueue(Channel channel) {
		return new RemoteResponseQueue(mapper(), channel, "", "game-chat-responses");
	}

	public static RemoteLiveActivity liveActivity(Channel channel) {
		return new RemoteLiveActivity(mapper(), channel, "live-activity", "");
	}
}

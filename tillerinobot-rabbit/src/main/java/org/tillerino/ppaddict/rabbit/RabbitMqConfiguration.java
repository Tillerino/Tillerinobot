package org.tillerino.ppaddict.rabbit;

import org.tillerino.ppaddict.chat.GameChatEventQueue;
import org.tillerino.ppaddict.chat.GameChatResponseQueue;

import com.fasterxml.jackson.databind.ObjectMapper;
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
		return new ObjectMapper();
	}

	public static RemoteLiveActivity liveActivity(Channel channel) {
		return new RemoteLiveActivity(mapper(), channel, "live-activity", "");
	}
}

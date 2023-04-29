package org.tillerino.ppaddict.rabbit;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMqConfiguration {

	public static ConnectionFactory connectionFactory(String hostName, int portNumber) {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(hostName);
		factory.setPort(portNumber);
		return factory;
	}

	public static ObjectMapper mapper() {
		return new ObjectMapper()
			.registerModule(new ParameterNamesModule())
			.registerModule(new Jdk8Module());
	}

	public static RemoteEventQueue externalEventQueue(Connection connection) throws IOException {
		Channel channel = connection.createChannel();
		channel.basicQos(100); // completely uninformed value. adjust as needed.
		return new RemoteEventQueue(mapper(), channel, "", "irc-reader");
	}

	public static RemoteEventQueue internalEventQueue(Connection connection) throws IOException {
		Channel channel = connection.createChannel();
		channel.basicQos(100); // completely uninformed value. adjust as needed.
		return new RemoteEventQueue(mapper(), channel, "", "game-chat-events");
	}

	public static RemoteResponseQueue responseQueue(Connection connection) throws IOException {
		Channel channel = connection.createChannel();
		channel.basicQos(100); // completely uninformed value. adjust as needed.
		return new RemoteResponseQueue(mapper(), channel, "", "game-chat-responses");
	}

	public static RemoteLiveActivity liveActivity(Connection connection) throws IOException {
		return new RemoteLiveActivity(mapper(), connection.createChannel(), "live-activity", "");
	}
}

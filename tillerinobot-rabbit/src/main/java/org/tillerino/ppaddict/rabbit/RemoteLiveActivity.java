package org.tillerino.ppaddict.rabbit;

import org.tillerino.ppaddict.chat.IRCName;
import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.rabbit.RemoteLiveActivity.LiveActivityMessage.ReceivedMessage;
import org.tillerino.ppaddict.rabbit.RemoteLiveActivity.LiveActivityMessage.ReceivedMessageDetails;
import org.tillerino.ppaddict.rabbit.RemoteLiveActivity.LiveActivityMessage.SentMessage;

import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoteLiveActivity extends AbstractRemoteQueue<RemoteLiveActivity.LiveActivityMessage> implements LiveActivity {
	public RemoteLiveActivity(ObjectMapper mapper, Channel channel, String exchange, String queue) {
		super(mapper, channel, exchange, queue, log, LiveActivityMessage.class);
	}

	@Override
	public void propagateReceivedMessage(String ircUserName, long eventId) {
		final ReceivedMessage message = new ReceivedMessage();
		message.setIrcUserName(ircUserName);
		message.setEventId(eventId);
		send(message);
	}

	@Override
	public void propagateSentMessage(String ircUserName, long eventId) {
		final SentMessage message = new SentMessage();
		message.setIrcUserName(ircUserName);
		message.setEventId(eventId);
		send(message);
	}

	@Override
	public void propagateMessageDetails(long eventId, String text) {
		final ReceivedMessageDetails message = new ReceivedMessageDetails();
		message.setText(text);
		message.setEventId(eventId);
		send(message);
	}

	// older Java compiler needs the full qualifier :/
	@lombok.Data
	@com.fasterxml.jackson.annotation.JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME)
	@com.fasterxml.jackson.annotation.JsonSubTypes({
			@Type(ReceivedMessage.class),
			@Type(SentMessage.class),
			@Type(ReceivedMessageDetails.class)
	})
	public static abstract class LiveActivityMessage {
		private long eventId;

		public abstract void visit(LiveActivity live);

		@EqualsAndHashCode(callSuper = true)
		@JsonTypeName("RECEIVED")
		@Data
		public static class ReceivedMessage extends LiveActivityMessage {
			@IRCName
			private String ircUserName;

			@Override
			public void visit(LiveActivity live) {
				live.propagateReceivedMessage(getIrcUserName(), getEventId());
			}
		}

		@EqualsAndHashCode(callSuper = true)
		@JsonTypeName("SENT")
		@Data
		public static class SentMessage extends LiveActivityMessage {
			@IRCName
			private String ircUserName;

			@Override
			public void visit(LiveActivity live) {
				live.propagateSentMessage(getIrcUserName(), getEventId());
			}
		}

		@EqualsAndHashCode(callSuper = true)
		@JsonTypeName("RECEIVED_DETAILS")
		@Data
		public static class ReceivedMessageDetails extends LiveActivityMessage {
			private String text;

			@Override
			public void visit(LiveActivity live) {
				live.propagateMessageDetails(getEventId(), getText());
			}
		}
	}
}

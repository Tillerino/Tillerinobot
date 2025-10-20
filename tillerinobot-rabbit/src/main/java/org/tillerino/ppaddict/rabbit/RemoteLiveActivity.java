package org.tillerino.ppaddict.rabbit;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import javax.annotation.CheckForNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.tillerino.ppaddict.chat.IRCName;
import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.rabbit.RemoteLiveActivity.LiveActivityMessage.ReceivedMessage;
import org.tillerino.ppaddict.rabbit.RemoteLiveActivity.LiveActivityMessage.ReceivedMessageDetails;
import org.tillerino.ppaddict.rabbit.RemoteLiveActivity.LiveActivityMessage.SentMessage;

@Slf4j
public class RemoteLiveActivity extends AbstractRemoteQueue<RemoteLiveActivity.LiveActivityMessage>
        implements LiveActivity {
    public RemoteLiveActivity(ObjectMapper mapper, Channel channel, String exchange, String queue) {
        super(mapper, channel, exchange, queue, log, LiveActivityMessage.class, null);
    }

    @Override
    public void propagateReceivedMessage(String ircUserName, long eventId) {
        final ReceivedMessage message = new ReceivedMessage();
        message.setIrcUserName(ircUserName);
        message.setEventId(eventId);
        send(message);
    }

    @Override
    public void propagateSentMessage(String ircUserName, long eventId, Long ping) {
        final SentMessage message = new SentMessage();
        message.setIrcUserName(ircUserName);
        message.setEventId(eventId);
        message.setPing(ping);
        send(message);
    }

    @Override
    public void propagateMessageDetails(long eventId, String text) {
        final ReceivedMessageDetails message = new ReceivedMessageDetails();
        message.setText(text);
        message.setEventId(eventId);
        send(message);
    }

    @Data
    @JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME)
    @JsonSubTypes({@Type(ReceivedMessage.class), @Type(SentMessage.class), @Type(ReceivedMessageDetails.class)})
    public abstract static class LiveActivityMessage {
        private long eventId;

        @EqualsAndHashCode(callSuper = true)
        @JsonTypeName("RECEIVED")
        @Data
        public static class ReceivedMessage extends LiveActivityMessage {
            @IRCName
            private String ircUserName;
        }

        @EqualsAndHashCode(callSuper = true)
        @JsonTypeName("SENT")
        @Data
        public static class SentMessage extends LiveActivityMessage {
            @IRCName
            private String ircUserName;

            @CheckForNull
            private Long ping;
        }

        @EqualsAndHashCode(callSuper = true)
        @JsonTypeName("RECEIVED_DETAILS")
        @Data
        public static class ReceivedMessageDetails extends LiveActivityMessage {
            private String text;
        }
    }
}

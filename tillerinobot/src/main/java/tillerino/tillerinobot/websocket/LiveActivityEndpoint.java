package tillerino.tillerinobot.websocket;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.inject.Singleton;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.MDC;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.BotBackend.IRCName;

/**
 * A real-time view into Tillerinobot. All IDs are anonymized through a salted
 * hash where the salt is the connection. This makes IDs stable per session, but
 * the real IDs cannot be reversed.
 */
@Slf4j
@Singleton
@ServerEndpoint("/live/v0")
public class LiveActivityEndpoint extends Endpoint {
	@Value
	public static class Received {
		long eventId;
		int user;
	}

	@Value
	public static class Sent {
		long eventId;
		int user;
		Integer ping;
	}

	@Value
	public static class MessageDetails {
		Long eventId;
		String message;
	}

	@Value
	@Builder
	public static class Message {
		Received received;
		Sent sent;
		MessageDetails messageDetails;
	}

	final ObjectMapper mapper = new ObjectMapper();
	final ObjectWriter writer;
	{
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setSerializationInclusion(Include.NON_NULL);
		writer = mapper.writerFor(Message.class);
	}

	@Getter(AccessLevel.PACKAGE) // for unit tests
	private final Set<Session> sessions = new HashSet<>();

	@Override
	@OnOpen
	public synchronized void onOpen(Session session, EndpointConfig config) {
		sessions.add(session);
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		if (message.equalsIgnoreCase("ping")) {
			session.getAsyncRemote().sendText("PONG");
		}
	}

	@Override
	@OnClose
	public synchronized void onClose(Session session, CloseReason closeReason) {
		sessions.remove(session);
	}

	private synchronized void sendToEachSession(Function<Session, Message> action) {
		for (Session session : sessions) {
			try {
				session.getAsyncRemote().sendText(writer.writeValueAsString(action.apply(session)));
			} catch (Exception e) {
				log.error("Error sending on Websocket", e);
			}
		}
	}

	public void propagateReceivedMessage(@IRCName String ircUserName, long eventId) {
		sendToEachSession(session -> Message.builder().received(new Received(eventId, anonymizeHashCode(ircUserName, session))).build());
	}

	public void propagateSentMessage(@IRCName String ircUserName, long eventId) {
		Integer ping = Optional.ofNullable(MDC.get("ping")).map(Integer::valueOf).orElse(null);
		sendToEachSession(session -> Message.builder().sent(new Sent(eventId, anonymizeHashCode(ircUserName, session), ping)).build());
	}

	public void propagateMessageDetails(long eventId, String text) {
		sendToEachSession(session -> Message.builder().messageDetails(new MessageDetails(eventId, text)).build());
	}

	/**
	 * Hashes two objects through the hash code of their combined SHA-512 hash.
	 */
	static int anonymizeHashCode(Object object, Object salt) {
		MessageDigest sha512 = DigestUtils.getSha512Digest();
		digest(sha512, object.hashCode());
		digest(sha512, salt.hashCode());
		return Arrays.hashCode(sha512.digest());
	}

	private static void digest(MessageDigest digest, int integer) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.asIntBuffer().put(integer);
		buffer.position(4);
		buffer.flip();
		digest.update(buffer);
	}
}

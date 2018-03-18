package tillerino.tillerinobot.websocket;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Singleton;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler.Whole;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.codec.digest.DigestUtils;

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
	private static final String RECEIVED = "{\n\t\"received\": {\n\t\t\"user\": %d\n\t}\n}";
	private static final String SENT = "{\n\t\"sent\": {\n\t\t\"user\": %d\n\t}\n}";
	private final Set<Session> sessions = new HashSet<>();

	@Override
	@OnOpen
	public synchronized void onOpen(Session session, EndpointConfig config) {
		sessions.add(session);
		session.addMessageHandler(new Whole<String>() {
			@Override
			public void onMessage(String message) {
				if (message.equalsIgnoreCase("ping")) {
					session.getAsyncRemote().sendText("PONG");
				}
			}
		});
	}

	@Override
	@OnClose
	public synchronized void onClose(Session session, CloseReason closeReason) {
		sessions.remove(session);
	}

	private synchronized void forEachSession(Consumer<Session> action) {
		for (Session session : sessions) {
			try {
				action.accept(session);
			} catch (Exception e) {
				log.error("Error sending on Websocket", e);
			}
		}
	}

	public void propagateReceivedMessage(@IRCName String ircUserName) {
		forEachSession(session -> session.getAsyncRemote()
				.sendText(String.format(RECEIVED, anonymizeHashCode(ircUserName, session))));
	}

	public void propagateSentMessage(@IRCName String ircUserName) {
		forEachSession(session -> session.getAsyncRemote()
				.sendText(String.format(SENT, anonymizeHashCode(ircUserName, session))));
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

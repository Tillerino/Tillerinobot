package tillerino.tillerinobot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.tillerino.ppaddict.util.TestAppender.mdc;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.assertj.core.api.ListAssert;
import org.awaitility.Awaitility;
import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.internal.util.MockUtil;
import org.slf4j.MDC;
import org.tillerino.ppaddict.chat.GameChatEventConsumer;
import org.tillerino.ppaddict.chat.GameChatWriter;
import org.tillerino.ppaddict.chat.PrivateAction;
import org.tillerino.ppaddict.chat.PrivateMessage;
import org.tillerino.ppaddict.chat.local.LocalGameChatEventQueue;
import org.tillerino.ppaddict.chat.local.LocalGameChatResponseQueue;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.util.TestAppender;
import org.tillerino.ppaddict.util.TestAppender.LogEventWithMdc;
import org.tillerino.ppaddict.util.TestAppender.LogRule;
import org.tillerino.ppaddict.util.TestClock;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import tillerino.tillerinobot.lang.Default;
import tillerino.tillerinobot.testutil.ExecutorServiceRule;

/**
 * Tests that all the logging (specifically the MDC) actually works as expected.
 */
public class LoggingTest {
	@Rule
	public final LogRule logRule = TestAppender.rule();

	@Rule
	public final ExecutorServiceRule exec = ExecutorServiceRule.cachedThreadPool("bot-root").interruptOnShutdown();

	public TestClock clock = new TestClock();

	private GameChatEventConsumer in;

	private GameChatWriter out;

	private TestBackend backend;

	@Before
	public void setUp() throws Exception {
		MDC.clear(); // it might be that there's some garbage from other tests in the MDC
		Injector injector = Guice.createInjector(new LocalConsoleTillerinobot() {
			@Override
			protected Clock createClock() {
				return clock;
			}
		});

		in = injector.getInstance(Key.get(GameChatEventConsumer.class, Names.named("messagePreprocessor")));
		out = injector.getInstance(GameChatWriter.class);
		assertTrue(MockUtil.isMock(out));
		backend = (TestBackend) injector.getInstance(BotBackend.class);
		exec.submit(injector.getInstance(LocalGameChatEventQueue.class));
		exec.submit(injector.getInstance(LocalGameChatResponseQueue.class));

		backend.hintUser("irc-guy", false, 1000, 1000);
		backend.hintUser("other-guy", true, 1000, 1000);
		backend.setLastVisitedVersion("irc-guy", IRCBot.CURRENT_VERSION);

		doAnswer(x -> {
			MDC.put("ping", "14");
			return null;
		}).when(out).message(anyString(), any());
	}

	@Test
	public void testRecommendation() throws Exception {
		processMessage("irc-guy", "!r");
		assertThat(MDC.get("ping")).isNull();

		awaitOurLogSize(2);

		assertThatOurLog().first()
			.satisfies(received("!r"))
			.satisfies(mdc("duration", null))
			.satisfies(mdc("ping", null));

		assertThatOurLog().element(1)
			.satisfies(sent("[http://osu.ppy.sh/b/"))
			.satisfies(mdc("success", "true"))
			.satisfies(mdc("handler", "r"))
			.satisfies(mdc("duration", "15"))
			.satisfies(mdc("ping", "14"));
	}

	@Test
	public void testNonHandlerMessage() throws Exception {
		processMessage("irc-guy", "!not-a-command");

		awaitOurLogSize(2);

		assertThatOurLog().first()
			.satisfies(received("!not-a-command"))
			.satisfies(mdc("handler", null))
			.satisfies(mdc("duration", null))
			.satisfies(mdc("ping", null));

		assertThatOurLog().element(1)
			.satisfies(sent(new Default().unknownCommand("not-a-command")))
			.satisfies(not(mdc("success", "true")))
			.satisfies(mdc("handler", null))
			.satisfies(mdc("duration", null))
			.satisfies(mdc("ping", "14"));
	}

	@Test
	public void testNp() throws Exception {
		processAction("irc-guy", "is listening to [http://osu.ppy.sh/b/338 title]");

		awaitOurLogSize(2);

		assertThatOurLog().first()
			.satisfies(action("is listening to [http://osu.ppy.sh/b/338 title]"))
			.satisfies(mdc("duration", null))
			.satisfies(mdc("ping", null));

		assertThatOurLog().element(1)
			.satisfies(sent("DragonForce - Beatmap 338 [Hard]"))
			.satisfies(mdc("success", "true"))
			.satisfies(mdc("handler", "np"))
			.satisfies(mdc("duration", "25"))
			.satisfies(mdc("ping", "14"));
	}

	@Test
	public void testTwoMessages() throws Exception {
		processMessage("other-guy", "send the hug thing");

		verify(out, timeout(1000)).action(anyString(), any());

		awaitOurLogSize(3);

		assertThatOurLog().element(1)
			.satisfies(mdc("state", "sent"))
			.satisfies(mdc("ping", "14"))
			.satisfies(mdc("handler", null));

		assertThatOurLog().element(2)
			.satisfies(mdc("state", "sent"))
			.satisfies(mdc("ping", null))
			.satisfies(mdc("handler", null));
	}

	private void processMessage(String user, String message) throws InterruptedException, IOException {
		clock.advanceBy(456);
		PrivateMessage event = new PrivateMessage(123, user, 456, message);
		clock.advanceBy(15);
		in.onEvent(event);

		verify(out, timeout(1000)).message(anyString(), eq(event));

		assertThatOurLog()
			.allSatisfy(mdc("event", "123"))
			.allSatisfy(mdc("user", user));
	}

	private void processAction(String user, String action) throws InterruptedException, IOException {
		clock.advanceBy(456);
		PrivateAction event = new PrivateAction(123, user, 456, action);
		clock.advanceBy(25);
		in.onEvent(event);

		verify(out, timeout(1000)).message(anyString(), eq(event));

		assertThatOurLog()
			.allSatisfy(mdc("event", "123"))
			.allSatisfy(mdc("user", user));
	}

	private Consumer<LogEventWithMdc> sent(String messageStart) {
		return mdc("state", "sent")
				.andThen(e -> assertThat((e.getMessage().getFormattedMessage())).startsWith("sent: " + messageStart));
	}

	private Consumer<LogEventWithMdc> received(String message) {
		return mdc("state", "msg")
				.andThen(event -> assertThat(event.getMessage().getFormattedMessage()).isEqualTo("received: " + message));
	}

	private Consumer<LogEventWithMdc> action(String message) {
		return mdc("state", "action")
				.andThen(event -> assertThat(event.getMessage().getFormattedMessage()).isEqualTo("action: " + message));
	}

	private void awaitOurLogSize(long size) {
		Awaitility.await().until(() -> logRule.events().stream().filter(isOurEvent()).count(), new IsEqual<>(size));
	}

	private ListAssert<LogEventWithMdc> assertThatOurLog() {
		return logRule.assertThat().filteredOn(isOurEvent());
	}

	private Predicate<? super LogEventWithMdc> isOurEvent() {
		return event -> event.getLoggerName().startsWith("tillerino")
				|| event.getLoggerName().startsWith("org.tillerino");
	}

	private <T> Consumer<T> not(Consumer<T> condition) {
		return t -> assertThatThrownBy(() -> condition.accept(t)).isInstanceOf(AssertionError.class);
	}
}

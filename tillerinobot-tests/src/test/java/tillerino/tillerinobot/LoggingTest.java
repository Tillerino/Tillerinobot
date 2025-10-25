package tillerino.tillerinobot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.tillerino.ppaddict.util.Result.ok;
import static org.tillerino.ppaddict.util.TestAppender.mdc;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import nl.altindag.log.model.LogEvent;
import org.assertj.core.api.ListAssert;
import org.awaitility.Awaitility;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.internal.util.MockUtil;
import org.slf4j.MDC;
import org.tillerino.ppaddict.chat.GameChatEventConsumer;
import org.tillerino.ppaddict.chat.GameChatWriter;
import org.tillerino.ppaddict.chat.PrivateAction;
import org.tillerino.ppaddict.chat.PrivateMessage;
import org.tillerino.ppaddict.chat.impl.MessagePreprocessor;
import org.tillerino.ppaddict.chat.impl.ResponsePostprocessor;
import org.tillerino.ppaddict.util.ExecutorServiceRule;
import org.tillerino.ppaddict.util.TestAppender;
import org.tillerino.ppaddict.util.TestAppender.LogRule;
import org.tillerino.ppaddict.util.TestClock;
import tillerino.tillerinobot.LocalConsoleTillerinobot.ClockModule;
import tillerino.tillerinobot.LocalConsoleTillerinobot.Injector;
import tillerino.tillerinobot.MysqlContainer.MysqlDatabaseLifecycle;
import tillerino.tillerinobot.lang.Default;

/** Tests that all the logging (specifically the MDC) actually works as expected. */
public class LoggingTest {
    @RegisterExtension
    public final LogRule logRule = TestAppender.rule(MessagePreprocessor.class, ResponsePostprocessor.class);

    @RegisterExtension
    public final ExecutorServiceRule exec =
            ExecutorServiceRule.cachedThreadPool("bot-root").interruptOnShutdown();

    public TestClock clock = new TestClock();

    private GameChatEventConsumer in;

    private GameChatWriter out;

    @RegisterExtension
    public final MysqlDatabaseLifecycle lifecycle = new MysqlDatabaseLifecycle();

    @BeforeEach
    public void setUp() throws Exception {
        MDC.clear(); // it might be that there's some garbage from other tests in the MDC
        Injector injector = DaggerLocalConsoleTillerinobot_Injector.builder()
                .clockModule(new ClockModule(clock))
                .build();

        in = injector.messagePreprocessor();
        out = injector.gameChatWriter();
        assertTrue(MockUtil.isMock(out));
        TestBackend backend = (TestBackend) injector.botBackend();
        exec.submit(injector.localGameChatEventQueue());
        exec.submit(injector.localGameChatResponseQueue());

        backend.hintUser("irc-guy", false, 1000, 1000);
        backend.hintUser("other-guy", true, 1000, 1000);
        backend.setLastVisitedVersion("irc-guy", IRCBot.CURRENT_VERSION);

        doAnswer(x -> {
                    return ok(new GameChatWriter.Response(14L));
                })
                .when(out)
                .message(anyString(), any());
        doReturn(ok(new GameChatWriter.Response(null))).when(out).action(any(), any());
    }

    @Test
    public void testRecommendation() throws Exception {
        processMessage("irc-guy", "!r");
        assertThat(MDC.get("ping")).isNull();

        awaitOurLogSize(2);

        assertThatOurLog()
                .first()
                .satisfies(received("!r"))
                .satisfies(mdc("duration", null))
                .satisfies(mdc("ping", null));

        assertThatOurLog()
                .element(1)
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

        assertThatOurLog()
                .first()
                .satisfies(received("!not-a-command"))
                .satisfies(mdc("handler", null))
                .satisfies(mdc("duration", null))
                .satisfies(mdc("ping", null));

        assertThatOurLog()
                .element(1)
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

        assertThatOurLog()
                .first()
                .satisfies(action("is listening to [http://osu.ppy.sh/b/338 title]"))
                .satisfies(mdc("duration", null))
                .satisfies(mdc("ping", null));

        assertThatOurLog()
                .element(1)
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

        assertThatOurLog()
                .element(1)
                .satisfies(mdc("state", "sent"))
                .satisfies(mdc("ping", "14"))
                .satisfies(mdc("handler", null));

        assertThatOurLog()
                .element(2)
                .satisfies(mdc("state", "sent"))
                .satisfies(mdc("ping", null))
                .satisfies(mdc("handler", null));
    }

    private void processMessage(String user, String message) throws InterruptedException, IOException {
        clock.advanceBy(456);
        PrivateMessage event = new PrivateMessage(123, user, 456, message);
        clock.advanceBy(15);
        in.onEvent(event);

        verify(out, timeout(1000)).message(anyString(), eq(user));

        assertThatOurLog().allSatisfy(mdc("event", "123")).allSatisfy(mdc("user", user));
    }

    private void processAction(String user, String action) throws InterruptedException, IOException {
        clock.advanceBy(456);
        PrivateAction event = new PrivateAction(123, user, 456, action);
        clock.advanceBy(25);
        in.onEvent(event);

        verify(out, timeout(1000)).message(anyString(), eq(user));

        assertThatOurLog().allSatisfy(mdc("event", "123")).allSatisfy(mdc("user", user));
    }

    private Consumer<LogEvent> sent(String messageStart) {
        return mdc("state", "sent").andThen(e -> assertThat((e.getMessage())).startsWith("sent: " + messageStart));
    }

    private Consumer<LogEvent> received(String message) {
        return mdc("state", "msg")
                .andThen(event -> assertThat(event.getMessage()).isEqualTo("received: " + message));
    }

    private Consumer<LogEvent> action(String message) {
        return mdc("state", "action")
                .andThen(event -> assertThat(event.getMessage()).isEqualTo("action: " + message));
    }

    private void awaitOurLogSize(long size) {
        Awaitility.await()
                .until(() -> logRule.events().stream().filter(isOurEvent()).count(), new IsEqual<>(size));
    }

    private ListAssert<LogEvent> assertThatOurLog() {
        return logRule.assertThat().filteredOn(isOurEvent());
    }

    private Predicate<? super LogEvent> isOurEvent() {
        return event -> event.getLoggerName().startsWith("tillerino")
                || event.getLoggerName().startsWith("org.tillerino");
    }

    private <T> Consumer<T> not(Consumer<T> condition) {
        return t -> assertThatThrownBy(() -> condition.accept(t)).isInstanceOf(AssertionError.class);
    }
}

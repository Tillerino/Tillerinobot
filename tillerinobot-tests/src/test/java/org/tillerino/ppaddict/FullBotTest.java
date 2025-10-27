package org.tillerino.ppaddict;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.tillerino.ppaddict.live.LiveContainer.getLive;
import static org.tillerino.ppaddict.util.TestAppender.mdc;
import static tillerino.tillerinobot.MysqlContainer.mysql;

import com.rabbitmq.client.Connection;
import dagger.Component;
import dagger.Provides;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.engio.mbassy.listener.Handler;
import org.awaitility.core.ConditionTimeoutException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kitteh.irc.client.library.event.connection.ClientConnectionEstablishedEvent;
import org.kitteh.irc.client.library.event.user.PrivateMessageEvent;
import org.tillerino.WireMockDocker.Module;
import org.tillerino.ppaddict.chat.impl.MessageHandlerScheduler.MessageHandlerSchedulerModule;
import org.tillerino.ppaddict.chat.impl.MessagePreprocessor;
import org.tillerino.ppaddict.chat.impl.ProcessorsModule;
import org.tillerino.ppaddict.chat.impl.RabbitQueuesModule;
import org.tillerino.ppaddict.chat.impl.ResponsePostprocessor;
import org.tillerino.ppaddict.chat.irc.IrcContainer;
import org.tillerino.ppaddict.chat.irc.KittehForNgircd;
import org.tillerino.ppaddict.live.AbstractLiveActivityEndpointTest.GenericWebSocketClient;
import org.tillerino.ppaddict.rabbit.RabbitMqConfiguration;
import org.tillerino.ppaddict.rabbit.RabbitMqContainerConnection;
import org.tillerino.ppaddict.rabbit.RemoteEventQueue;
import org.tillerino.ppaddict.rest.AuthenticationService;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.util.ExecutorServiceRule;
import org.tillerino.ppaddict.util.TestAppender;
import org.tillerino.ppaddict.util.TestAppender.LogRule;
import tillerino.tillerinobot.*;
import tillerino.tillerinobot.diff.DiffEstimateProvider;
import tillerino.tillerinobot.recommendations.Recommender;
import tillerino.tillerinobot.rest.BotInfoService;
import tillerino.tillerinobot.rest.BotStatus;

@Slf4j
public class FullBotTest extends AbstractDatabaseTest {
    @Component(modules = {FullBotConfiguration.class})
    @Singleton
    interface Injector {
        void inject(FullBotTest test);
    }

    @SuppressWarnings({"unchecked"})
    private class Client implements Runnable {
        private final org.kitteh.irc.client.library.Client kitteh;

        private long lastReceivedRecommendation = 0;

        private int receivedRecommendations = 0;

        private boolean connected = false;

        private final int botNumber;

        private Client(int botNumber) {
            kitteh = KittehForNgircd.buildKittehClient("user" + botNumber);
            kitteh.getEventManager().registerEventListener(this);
            this.botNumber = botNumber;
        }

        @Handler
        public void onConnect(ClientConnectionEstablishedEvent event) {
            connected = true;
        }

        @Handler
        public void onPrivateMessage(PrivateMessageEvent event) throws Exception {
            log.debug(
                    "user{} received private message from {}: {}",
                    botNumber,
                    event.getActor().getNick(),
                    event.getMessage());
            if (event.getMessage().contains(IRCBot.VERSION_MESSAGE)) {
                return;
            }
            if (event.getMessage().contains("Beatmap")) {
                receivedRecommendations++;
                lastReceivedRecommendation = System.currentTimeMillis();
                recommendationCount.incrementAndGet();
            }
            if (receivedRecommendations < recommendationsPerUser) {
                Thread.sleep(10);
                r();
            } else {
                log.debug("user{} received {} recommendations. Quitting.", botNumber, recommendationsPerUser);
                kitteh.shutdown();
            }
        }

        @Override
        public void run() {
            try {
                kitteh.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void r() {
            kitteh.sendMessage("tbot", "!r");
        }
    }

    @dagger.Module(
            includes = {
                RabbitQueuesModule.class,
                DockeredMysqlModule.class,
                MessageHandlerSchedulerModule.class,
                ProcessorsModule.class,
                TestBaseModule.class,
                Clock.Module.class,
                Module.class
            })
    protected class FullBotConfiguration {
        @Provides
        @Named("coreSize")
        int provideCoreSize() {
            return 4;
        }

        @Provides
        AuthenticationService provideAuthenticationService() {
            return new FakeAuthenticationService();
        }

        @Provides
        Connection provideConnection() {
            return rabbit.getConnection();
        }

        @Provides
        BotStatus provideBotStatus(BotInfoService botInfoService) {
            return botInfoService;
        }
    }

    static {
        // make sure these are started
        mysql();
        getLive();
    }

    private final GenericWebSocketClient client = mock(GenericWebSocketClient.class);
    private final WebSocketClient webSocketClient = new WebSocketClient();

    private final ThreadGroup clients = new ThreadGroup("Clients");

    @RegisterExtension
    public final ExecutorServiceRule clientExec = new ExecutorServiceRule(() -> new ThreadPoolExecutor(
            0,
            Integer.MAX_VALUE,
            1L,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            r -> new Thread(clients, r, "Client")));

    @RegisterExtension
    @Order(1)
    public final ExecutorServiceRule exec = new ExecutorServiceRule(() -> new ThreadPoolExecutor(
            0, Integer.MAX_VALUE, 1L, TimeUnit.SECONDS, new SynchronousQueue<>(), r -> new Thread(r, "aux")));

    @RegisterExtension
    @Order(2)
    public final RabbitMqContainerConnection rabbit = new RabbitMqContainerConnection(exec);

    @Inject
    @Named("core")
    ThreadPoolExecutor coreWorkerPool;

    @RegisterExtension
    public final LogRule logRule = TestAppender.rule(MessagePreprocessor.class, ResponsePostprocessor.class);

    private final AtomicInteger recommendationCount = new AtomicInteger();
    protected final List<Future<?>> started = new ArrayList<>();

    protected final int users = 2;
    protected final int recommendationsPerUser = 10;

    @Inject
    BotStatus botInfoApi;

    @Inject
    MessagePreprocessor messagePreprocessor;

    @Inject
    BotBackend backend;

    @Inject
    OsuApi osuApi;

    @Inject
    Recommender recommender;

    @Inject
    DiffEstimateProvider diffEstimateProvider;

    @Override
    public void createEntityManager() {}

    @BeforeEach
    public void startBot() throws Exception {
        DaggerFullBotTest_Injector.builder()
                .fullBotConfiguration(new FullBotTest.FullBotConfiguration())
                .build()
                .inject(this);

        super.createEntityManager();

        webSocketClient.start();
        String wsUrl = "ws://" + getLive().getHost() + ":" + getLive().getMappedPort(8080) + "/live/v0";
        log.info("Connecting to websocket at {}", wsUrl);
        Future<Session> connect = webSocketClient.connect(client, new URI(wsUrl));
        connect.get(10, TimeUnit.SECONDS);

        for (int botNumber = 0; botNumber < users; botNumber++) {
            MockData.mockUser("user" + botNumber, false, 12, 1000, 1, backend, osuApi, recommender);
        }
        TestBase.mockRecommendations(recommender);
        TestBase.mockBeatmapMetas(diffEstimateProvider);
        RemoteEventQueue externalEventQueue = RabbitMqConfiguration.externalEventQueue(rabbit.getConnection());
        externalEventQueue.setup();
        externalEventQueue.subscribe(messagePreprocessor::onEvent);
        IrcContainer.TILLERINOBOT_IRC.start();
    }

    @AfterEach
    public void stopBot() throws Exception {
        started.forEach(fut -> fut.cancel(true));
        if (coreWorkerPool != null) {
            coreWorkerPool.shutdownNow();
        }
        webSocketClient.stop();
    }

    @Test
    public void testMultipleUsers() {
        List<Client> clients = IntStream.range(0, users).mapToObj(Client::new).toList();
        clients.forEach(client -> {
            try {
                // we have to spread out our connection attempts a bit or they might fail
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            clientExec.submit(client);
        });
        clients.forEach(client -> {
            await().until(() -> client.connected);
            client.r();
        });
        int total = users * recommendationsPerUser;
        Callable<Boolean> allRecommendationsReceived = () -> recommendationCount.get() == total;
        for (int i = 0; i < 100; i++) {
            try {
                log.info(
                        "Waiting for recommendation count to reach {}. Current: {}.", total, recommendationCount.get());
                await().atMost(Duration.ofSeconds(2)).until(allRecommendationsReceived);
            } catch (ConditionTimeoutException e) {
                log.info("Some clients got concurrent messages. Let's give 'em a push.");
                clients.stream()
                        .filter(client -> client.receivedRecommendations < recommendationsPerUser)
                        .filter(client -> client.lastReceivedRecommendation < System.currentTimeMillis() - 2000)
                        .forEach(Client::r);
                continue;
            }
            break;
        }
        await().atMost(Duration.ofSeconds(2)).until(allRecommendationsReceived);

        verify(client, timeout(10000).atLeast(total)).message(argThat(s -> s.contains("\"received\":")));
        verify(client, timeout(1000).atLeast(total)).message(argThat(s -> s.contains("\"sent\":")));
        verify(client, timeout(1000).atLeast(total)).message(argThat(s -> s.contains("\"messageDetails\":")));

        logRule.assertThat()
                .anySatisfy(mdc("handler", "r"))
                .allSatisfy(event -> assertThat(event.getDiagnosticContext()).containsKey("event"))
                .allSatisfy(event -> assertThat(event.getDiagnosticContext()).containsKey("user"));

        assertThat(botInfoApi.isReceiving()).isTrue();
        assertThat(botInfoApi.isSending()).isTrue();
        assertThat(botInfoApi.isRecommending()).isTrue();

        log.info("Received {} recommendations. Quitting.", recommendationCount.get());
    }
}

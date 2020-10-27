package org.tillerino.ppaddict;

import static org.tillerino.ppaddict.live.LiveContainer.getLive;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ForkJoinTask;

import javax.sql.DataSource;
import javax.websocket.DeploymentException;

import org.junit.ClassRule;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.chat.impl.RabbitQueuesModule;
import org.tillerino.ppaddict.live.RabbitMqContainer;
import org.tillerino.ppaddict.live.RabbitMqContainerConnection;
import org.tillerino.ppaddict.rabbit.RabbitMqConfiguration;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.AbstractDatabaseTest.CreateInMemoryDatabaseModule;

/**
 * Runs a full bot test with MySQL and ngIRCd.
 */
@Slf4j
public class FullBotIT extends AbstractFullBotTest {
	public static final String DOCKER_HOST = Optional.ofNullable(System.getenv("DOCKER_HOST"))
			.map(URI::create).map(URI::getHost).orElse("localhost");

	private static final MySQLContainer MYSQL = new MySQLContainer<>();

	private static final GenericContainer NGIRCD = new GenericContainer<>("linuxserver/ngircd:60428df3-ls19")
			.withClasspathResourceMapping("/irc/ngircd.conf", "/config/ngircd.conf", BindMode.READ_ONLY)
			.withClasspathResourceMapping("/irc/ngircd.motd", "/etc/ngircd/ngircd.motd", BindMode.READ_ONLY)
			.withExposedPorts(6667);

	@ClassRule
	public static RabbitMqContainerConnection rabbit = new RabbitMqContainerConnection();

	static {
		// these take a little longer to start, so we'll do that async
		ForkJoinTask<?> ngircd = ForkJoinTask.adapt((Runnable) NGIRCD::start).fork();
		ForkJoinTask<?> mysql = ForkJoinTask.adapt((Runnable) MYSQL::start).fork();
		RabbitMqContainer.getRabbitMq(); // make sure it's started
		getLive();
		ngircd.join();
		mysql.join();
	}

	public FullBotIT() {
		super(log);
		users = 2;
		recommendationsPerUser = 10;
	}

	@Override
	public void startBot() throws Exception {
		RabbitMqConfiguration.liveActivity(rabbit.getChannel()).setup();
		super.startBot();
	}

	@Override
	protected String getWsUrl(Injector injector) throws DeploymentException {
		return "ws://" + DOCKER_HOST + ":" + getLive().getMappedPort(8080) + "/live/v0";
	}

	@Override
	protected Injector createInjector() {
		return Guice.createInjector(new FullBotConfiguration(ircHost(), ircPort()) {
			@Override
			protected void installMore() {
				install(new CreateInMemoryDatabaseModule() {
					@Override
					protected DataSource dataSource() {
						MysqlDataSource dataSource = new MysqlDataSource();
						dataSource.setURL(MYSQL.getJdbcUrl());
						dataSource.setUser(MYSQL.getUsername());
						dataSource.setPassword(MYSQL.getPassword());
						return dataSource;
					}
				});

				bind(LiveActivity.class).toInstance(RabbitMqConfiguration.liveActivity(rabbit.getChannel()));

				bind(Channel.class).toInstance(rabbit.getChannel());
				install(new RabbitQueuesModule());
			}
		});
	}

	protected String ircHost() {
		return DOCKER_HOST;
	}

	protected int ircPort() {
		return NGIRCD.getMappedPort(6667);
	}
}

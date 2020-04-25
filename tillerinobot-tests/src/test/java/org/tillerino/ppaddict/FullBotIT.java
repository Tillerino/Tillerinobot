package org.tillerino.ppaddict;

import java.net.URI;
import java.util.Optional;

import javax.sql.DataSource;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import tillerino.tillerinobot.AbstractDatabaseTest.CreateInMemoryDatabaseModule;

/**
 * Runs a full bot test with MySQL and ngIRCd.
 */
public class FullBotIT extends FullBotTest {
	public static final String DOCKER_HOST = Optional.ofNullable(System.getenv("DOCKER_HOST"))
			.map(URI::create).map(URI::getHost).orElse("localhost");

	private static final MySQLContainer MYSQL = new MySQLContainer<>();

	private static final GenericContainer NGIRCD = new GenericContainer<>("linuxserver/ngircd:60428df3-ls19")
			.withClasspathResourceMapping("/irc/ngircd.conf", "/config/ngircd.conf", BindMode.READ_ONLY)
			.withClasspathResourceMapping("/irc/ngircd.motd", "/etc/ngircd/ngircd.motd", BindMode.READ_ONLY)
			.withExposedPorts(6667);

	static {
		MYSQL.start();
		NGIRCD.start();
	}

	{
		users = 2;
		recommendationsPerUser = 10;
	}

	@Override
	protected Injector createInjector() {
		return Guice.createInjector(new FullBotConfiguration(ircHost(), ircPort(), exec, coreWorkerPool) {
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

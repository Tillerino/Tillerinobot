package org.tillerino.ppaddict;

import java.net.URI;
import java.util.Optional;

import javax.sql.DataSource;

import org.testcontainers.containers.MySQLContainer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import tillerino.tillerinobot.AbstractDatabaseTest.CreateInMemoryDatabaseModule;

public class FullBotIT extends FullBotTest {
	public static final String DOCKER_HOST = Optional.ofNullable(System.getenv("DOCKER_HOST"))
			.map(URI::create).map(URI::getHost).orElse("localhost");

	private static final MySQLContainer mysql = new MySQLContainer<>();

	static {
		mysql.start();
	}

	@Override
	protected Injector createInjector() {
		return Guice.createInjector(new FullBotConfiguration(server.getPort(), exec, coreWorkerPool) {
			@Override
			protected void installMore() {
				install(new CreateInMemoryDatabaseModule() {
					@Override
					protected DataSource dataSource() {
						MysqlDataSource dataSource = new MysqlDataSource();
						dataSource.setURL(mysql.getJdbcUrl());
						dataSource.setUser(mysql.getUsername());
						dataSource.setPassword(mysql.getPassword());
						return dataSource;
					}
				});
			}
		});
	}
}

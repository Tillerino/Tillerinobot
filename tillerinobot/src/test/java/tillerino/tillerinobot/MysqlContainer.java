package tillerino.tillerinobot;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.testcontainers.containers.MySQLContainer;
import org.tillerino.ppaddict.util.DockerNetwork;
import org.tillerino.ppaddict.util.ReusableContainerInitializer;

public class MysqlContainer {

	private static final String databaseName = UUID.randomUUID().toString().replaceAll("-", "").replaceFirst("\\d", "x");

	private static final ReusableContainerInitializer<ContainerDef> initializer = new ReusableContainerInitializer<>(
		new ContainerDef()
			.withNetwork(DockerNetwork.NETWORK)
			.withNetworkAliases("mysql")
			.withReuse(true),
		container -> {
			container.withUsername("root");
			try (Connection connection = container.createConnection("");
					 Statement statement = connection.createStatement()) {
				assertThat(statement.executeUpdate("CREATE DATABASE " + databaseName)).isGreaterThan(0);
				// don't know how to check that this succeeded with return value, but it has thrown in the past
				statement.executeUpdate("GRANT ALL PRIVILEGES ON " + databaseName + ".* TO 'test'@'%';");
			}
			container.withUsername("test");
			container.withDatabaseName(databaseName);
		});

	private static final class ContainerDef extends MySQLContainer<ContainerDef> {
	}

	public static MySQLContainer mysql() {
		return initializer.start();
	}

	public static TestRule mysqlTruncate() {
		return new TestWatcher() {
			@Override
			protected void finished(Description description) {
				MySQLContainer container;
				try {
					container = mysql();
				} catch (Exception e) {
					return;
				}
				try(Connection connection = container.createConnection("");
						Statement statement = connection.createStatement();
						ResultSet resultSet = statement.executeQuery("SELECT TABLE_NAME, TABLE_TYPE from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA = '" + container.getDatabaseName() + "'");) {
					while(resultSet.next()) {
						if (resultSet.getString("TABLE_TYPE").equals("VIEW")) {
							continue;
						}
						statement.executeUpdate("truncate `" + container.getDatabaseName() + "`.`" + resultSet.getString("TABLE_NAME") + "`");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}
}

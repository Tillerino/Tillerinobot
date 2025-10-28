package tillerino.tillerinobot;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.MySQLContainer;
import org.tillerino.ppaddict.util.DockerNetwork;
import org.tillerino.ppaddict.util.ReusableContainerInitializer;

public class MysqlContainer {

    private static final String databaseName =
            UUID.randomUUID().toString().replaceAll("-", "").replaceFirst("\\d", "x");

    private static final ReusableContainerInitializer<ContainerDef> initializer = new ReusableContainerInitializer<>(
            new ContainerDef()
                    .withNetwork(DockerNetwork.NETWORK)
                    .withNetworkAliases("mysql")
                    .withExposedPorts(3306)
                    .withReuse(true),
            container -> {
                container.withUsername("root");
                try (Connection connection = container.createConnection("");
                        Statement statement = connection.createStatement()) {
                    assertThat(statement.executeUpdate("CREATE DATABASE " + databaseName))
                            .isGreaterThan(0);
                    // don't know how to check that this succeeded with return value, but it has thrown in the past
                    statement.executeUpdate("GRANT ALL PRIVILEGES ON " + databaseName + ".* TO 'test'@'%';");
                }
                container.withUsername("test");
                container.withDatabaseName(databaseName);
            });

    private static final class ContainerDef extends MySQLContainer<ContainerDef> {}

    public static MySQLContainer mysql() {
        return initializer.start();
    }

    public static class MysqlDatabaseLifecycle implements BeforeEachCallback, AfterEachCallback {
        private static boolean createdSchema = false;

        @Override
        public void beforeEach(ExtensionContext context) {
            if (!createdSchema) {
                try {
                    createSchema();
                } catch (RuntimeException | Error e) {
                    throw e;
                } catch (Exception e) {
                    throw new ContextedRuntimeException(e);
                }
                createdSchema = true;
            }
        }

        @Override
        public void afterEach(ExtensionContext context) {
            MySQLContainer container;
            try {
                container = mysql();
            } catch (Exception e) {
                return;
            }
            try (Connection connection = container.createConnection("");
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(
                            "SELECT TABLE_NAME, TABLE_TYPE from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA = '"
                                    + container.getDatabaseName() + "'");
                    Statement statement2 = connection.createStatement()) {
                while (resultSet.next()) {
                    if (resultSet.getString("TABLE_TYPE").equals("VIEW")) {
                        continue;
                    }
                    statement2.executeUpdate("truncate `" + container.getDatabaseName() + "`.`"
                            + resultSet.getString("TABLE_NAME") + "`");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void createSchema() throws IOException, SQLException {
            try (InputStream is = Validate.notNull(
                            AbstractDatabaseTest.class.getResourceAsStream("/structure.sql"),
                            "database structure file not in classpath");
                    Connection conn = mysql().createConnection("");
                    Statement statement = conn.createStatement()) {
                String string = IOUtils.toString(is, StandardCharsets.UTF_8);

                String[] split = string.split(";");
                for (String s : split) {
                    if (s.trim().isEmpty()) {
                        continue;
                    }
                    try {
                        statement.execute(s);
                    } catch (Exception e) {
                        System.out.println(s);
                        throw e;
                    }
                }
            }
        }
    }
}

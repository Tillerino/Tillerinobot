package org.tillerino.mormon;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tillerino.mormon.Persister.Action;
import org.tillerino.ppaddict.util.InjectionRunner;
import org.tillerino.ppaddict.util.TestModule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tillerino.tillerinobot.AbstractDatabaseTest.DockeredMysqlModule;

@RunWith(InjectionRunner.class)
@TestModule(value = { DockeredMysqlModule.class })
public class DatabaseTest {
	@Inject
	DatabaseManager dbm;

	Database db;

	@Before
	public void setup() {
		db = dbm.getDatabase();
	}

	@After
	public void tearDown() throws SQLException {
		db.close();
	}

	@Data
	@Table("composite_key")
	@KeyColumn({ "myKey", "additionalKey" })
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CompositeKey {
		int myKey;

		long additionalKey;

		String additionalProperty;

		static final String TABLE_DEF = "CREATE TABLE IF NOT EXISTS `composite_key` (`myKey` int, `additionalKey` int, `additionalProperty` text)";
	}

	@Test
	public void testDelete() throws SQLException {
		db.connection().createStatement().execute(CompositeKey.TABLE_DEF);
		db.delete(CompositeKey.class, true); // clean up from previous tests

		db.persist(new CompositeKey(1, 11, "abc"), Action.INSERT);
		db.persist(new CompositeKey(2, 12, "def"), Action.INSERT);

		try (Loader<CompositeKey> loader = db.loader(CompositeKey.class, "")) {
			assertThat(loader.query()).hasSize(2);
		}

		assertThat(db.delete(new CompositeKey(1, 11, "thisisignored"))).isOne();

		try (Loader<CompositeKey> loader = db.loader(CompositeKey.class, "")) {
			assertThat(loader.query()).containsExactly(new CompositeKey(2, 12, "def"));
		}

		assertThat(db.delete(new CompositeKey(1, 11, "thisisignored"))).isZero();
	}

}

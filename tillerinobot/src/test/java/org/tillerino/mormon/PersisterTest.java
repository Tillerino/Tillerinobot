package org.tillerino.mormon;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
public class PersisterTest {
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
	@Table("simple_key")
	@KeyColumn("myKey")
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SimpleKey {
		int myKey;

		String additionalProperty;

		static final String TABLE_DEF = "CREATE TABLE IF NOT EXISTS `simple_key` (`myKey` int, `additionalProperty` text)";
	}

	@Test
	public void testDelete() throws Exception {
		db.connection().createStatement().execute(SimpleKey.TABLE_DEF);
		db.truncate(SimpleKey.class); // clean up from previous tests

		assertEquals(0, (int) db.deleteFrom(SimpleKey.class)."where myKey = \{2070907}");

		db.persist(new SimpleKey(2070907, "more"), Action.INSERT);

		assertEquals(1, (int) db.deleteFrom(SimpleKey.class)."where myKey = \{2070907}");
	}

	@Test
	public void test() throws Exception {
		db.connection().createStatement().execute(SimpleKey.TABLE_DEF);
		db.truncate(SimpleKey.class); // clean up from previous tests

		SimpleKey player1 = new SimpleKey(1, "abc");

		SimpleKey player2 = new SimpleKey(2, "def");

		SimpleKey player3 = new SimpleKey(3, "ghi");

		try (Loader<SimpleKey> loader = db.loader(SimpleKey.class, " order by `myKey`")) {
			try (Persister<SimpleKey> persister = db.persister(SimpleKey.class, Action.INSERT);) {
				persister.persist(player1, 2);

				assertEquals(Collections.emptyList(), toList(loader.query()));

				persister.persist(player2, 2);

				assertEquals(Arrays.asList(player1, player2), toList(loader.query()));

				persister.persist(player3, 2);

				assertEquals(Arrays.asList(player1, player2), toList(loader.query()));
			}

			assertEquals(Arrays.asList(player1, player2, player3), toList(loader.query()));
		}
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

	<T> List<T> toList(Iterable<T> iterable) {
		List<T> list = new ArrayList<>();

		iterable.forEach(list::add);

		return list;
	}

}

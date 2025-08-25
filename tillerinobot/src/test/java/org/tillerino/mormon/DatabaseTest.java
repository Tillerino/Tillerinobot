package org.tillerino.mormon;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;

import javax.inject.Singleton;

import org.junit.jupiter.api.Test;
import org.tillerino.mormon.Persister.Action;

import dagger.Component;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tillerino.tillerinobot.AbstractDatabaseTest;
import tillerino.tillerinobot.DaggerAbstractDatabaseTest_Injector;

public class DatabaseTest extends AbstractDatabaseTest {
	{
		DaggerAbstractDatabaseTest_Injector.create().inject(this);
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
	public void testTruncate() throws SQLException {
		db.connection().createStatement().execute(CompositeKey.TABLE_DEF);
		db.truncate(CompositeKey.class); // clean up from previous tests

		db.persist(new CompositeKey(1, 11, "abc"), Action.INSERT);
		db.persist(new CompositeKey(2, 12, "def"), Action.INSERT);

		try (Loader<CompositeKey> loader = db.loader(CompositeKey.class, "")) {
			assertThat(loader.query()).hasSize(2);
		}

		db.truncate(CompositeKey.class);

		try (Loader<CompositeKey> loader = db.loader(CompositeKey.class, "")) {
			assertThat(loader.query()).isEmpty();
		}
	}

	@Test
	public void testDelete() throws SQLException {
		db.connection().createStatement().execute(CompositeKey.TABLE_DEF);
		db.truncate(CompositeKey.class); // clean up from previous tests

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

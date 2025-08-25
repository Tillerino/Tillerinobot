package org.tillerino.mormon;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import lombok.NoArgsConstructor;

import tillerino.tillerinobot.AbstractDatabaseTest;
import tillerino.tillerinobot.DaggerAbstractDatabaseTest_Injector;

public class LoaderTest extends AbstractDatabaseTest {
	{
		DaggerAbstractDatabaseTest_Injector.create().inject(this);
	}
	@Table("does_not_exist")
	@NoArgsConstructor
	static class DoesNotExist {
		
	}

	@Test
	public void wrongParameterCount() throws Exception {
		try(Database db = dbm.getDatabase();
				Loader<DoesNotExist> loader = db.loader(DoesNotExist.class, "where field_name = ?")) {
			assertThatThrownBy(() -> loader.query(1, 2)).hasMessage("Expected 1 parameters but received 2");
		}
	}
}

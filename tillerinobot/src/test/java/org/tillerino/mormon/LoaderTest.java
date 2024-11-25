package org.tillerino.mormon;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.inject.Inject;

import lombok.NoArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tillerino.ppaddict.util.InjectionRunner;
import org.tillerino.ppaddict.util.TestModule;

import tillerino.tillerinobot.AbstractDatabaseTest.DockeredMysqlModule;

@RunWith(InjectionRunner.class)
@TestModule(value = { DockeredMysqlModule.class })
public class LoaderTest {
	@Inject
	DatabaseManager dbm;

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

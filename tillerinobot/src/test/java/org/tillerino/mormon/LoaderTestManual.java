package org.tillerino.mormon;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tillerino.mormon.Persister.Action;
import org.tillerino.ppaddict.util.InjectionRunner;
import org.tillerino.ppaddict.util.TestModule;

import tillerino.tillerinobot.AbstractDatabaseTest.DockeredMysqlModule;

/**
 * Check if streaming works.
 */
@RunWith(InjectionRunner.class)
@TestModule(value = { DockeredMysqlModule.class })
public class LoaderTestManual {
	@Inject
	DatabaseManager dbm;

	@Table("byteArrays")
	public static class ByteArrays {
		public byte[] payload;
	}

	@Test
	public void testStreaming() throws Exception {
		Database db = dbm.getDatabase();
		db.connection().createStatement().execute("CREATE TABLE `byteArrays` (`payload` longblob NOT NULL)");
		for (int i = 0; i < 1024; i++) {
			ByteArrays b = new ByteArrays();
			b.payload = new byte[500 * 1024];
			db.persister(ByteArrays.class, Action.INSERT)
				.persist(b);
			System.out.println(i);
		}
		System.out.println();
		for (ByteArrays b : db.streamingLoader(ByteArrays.class, "").query()) {
			// put a breakpoint here, check memory consumption after GC
			System.out.println("x");
		}
	}
}

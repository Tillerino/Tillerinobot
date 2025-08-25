package org.tillerino.mormon;

import org.junit.jupiter.api.Test;
import org.tillerino.mormon.Persister.Action;

import tillerino.tillerinobot.AbstractDatabaseTest;
import tillerino.tillerinobot.DaggerAbstractDatabaseTest_Injector;

/**
 * Check if streaming works.
 */
public class LoaderTestManual extends AbstractDatabaseTest {
	{
		DaggerAbstractDatabaseTest_Injector.create().inject(this);
	}

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

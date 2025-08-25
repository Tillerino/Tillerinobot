package tillerino.tillerinobot.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;

import org.junit.Test;
import org.tillerino.mormon.Persister.Action;

import tillerino.tillerinobot.DaggerAbstractDatabaseTest_Injector;
import tillerino.tillerinobot.OsuApi;
import org.tillerino.osuApiModel.Downloader;
import tillerino.tillerinobot.AbstractDatabaseTest;
import tillerino.tillerinobot.OsuApiV1;

public class ApiBeatmapTest extends AbstractDatabaseTest {
	{
		DaggerAbstractDatabaseTest_Injector.create().inject(this);
	}

	protected static OsuApi downloader = spy(OsuApiV1.createTestApi(AbstractDatabaseTest.class));

	@Test
	public void testSchema() throws Exception {
		assertNotNull(ApiBeatmap.loadOrDownload(db, 53, 0L, 0, downloader));
	}

	@Test
	public void testStoring() throws Exception {
		ApiBeatmap original = newApiBeatmap();
		db.persister(ApiBeatmap.class, Action.INSERT).persist(original);
		assertThat(db.loader(ApiBeatmap.class, "").queryUnique()).hasValueSatisfying(original::equals);
	}

	public static ApiBeatmap newApiBeatmap() {
		ApiBeatmap apiBeatmap = new ApiBeatmap();
		apiBeatmap.setArtist("no artist");
		apiBeatmap.setTitle("no title");
		apiBeatmap.setVersion("no version");
		apiBeatmap.setCreator("no creator");
		apiBeatmap.setSource("no source");
		apiBeatmap.setFileMd5("no md5");
		return apiBeatmap;
	}
}

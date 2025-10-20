package tillerino.tillerinobot.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dagger.Component;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.tillerino.mormon.Persister.Action;
import tillerino.tillerinobot.*;

public class ApiBeatmapTest extends AbstractDatabaseTest {
    @Component(modules = {DockeredMysqlModule.class, OsuApiV1Test.Module.class})
    @Singleton
    interface Injector {
        void inject(ApiBeatmapTest t);
    }

    {
        DaggerApiBeatmapTest_Injector.create().inject(this);
    }

    @Inject
    protected OsuApiV1 downloader;

    @Test
    public void testSchema() throws Exception {
        assertNotNull(ApiBeatmap.loadOrDownload(db, 131891, 0L, 0, downloader));
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

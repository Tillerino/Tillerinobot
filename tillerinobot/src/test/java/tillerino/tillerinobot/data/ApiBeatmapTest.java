package tillerino.tillerinobot.data;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import tillerino.tillerinobot.TestBase;

public class ApiBeatmapTest extends TestBase {
    @Test
    public void testSchema() throws Exception {
        assertNotNull(ApiBeatmap.loadOrDownload(apiBeatmapRepo, db, 131891, 0L, 0, osuApiV1));
    }

    @Test
    public void testStoring() throws Exception {
        ApiBeatmap original = newApiBeatmap();
        apiBeatmapRepo.insert(db, original);
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

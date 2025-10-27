package tillerino.tillerinobot.rest;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verifyNoInteractions;

import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tillerino.mormon.Persister.Action;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import tillerino.tillerinobot.TestBase;
import tillerino.tillerinobot.data.ActualBeatmap;

/**
 * In this test, we want to make sure that the {@link AbstractBeatmapResource} handles downloading and saving beatmaps
 * files correctly with respect to the expected hash value.
 */
public class AbstractBeatmapResourceTest extends TestBase {

    final OsuApiBeatmap beatmap = new OsuApiBeatmap();

    AbstractBeatmapResource resource;

    @BeforeEach
    public void init() {
        resource = new AbstractBeatmapResource(dbm, beatmapDownloader, beatmap) {
            @Override
            public OsuApiBeatmap get() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Test
    public void testMigrations() throws Exception {
        String content = "hello";
        // save without hash for backwards compatibility
        dbm.persist(new ActualBeatmap(12, content.getBytes(), null, 1, ""), Action.INSERT);
        beatmap.setBeatmapId(12);
        beatmap.setFileMd5(md5Hex(content));
        assertEquals(content, resource.getFile());
        verifyNoInteractions(beatmapDownloader);
        // data was compressed after the fact
        assertThat(dbm.selectUnique(ActualBeatmap.class).execute("where beatmapid = ", 12))
                .hasValueSatisfying(ab -> assertThat(ab).hasFieldOrPropertyWithValue("content", null));
    }

    @Test
    public void testRedownloadOnWrongHash() throws Exception {
        String oldContent = "hello";
        String newContent = "world";
        dbm.persist(new ActualBeatmap(12, oldContent.getBytes(), null, 1, md5Hex(oldContent)), Action.INSERT);
        beatmap.setBeatmapId(12);
        beatmap.setFileMd5(md5Hex(newContent));
        doReturn(newContent).when(beatmapDownloader).getActualBeatmap(12);
        assertEquals("world", resource.getFile());
    }

    @Test
    public void testThrowAndReuploadOnWrongHash() {
        String correctContent = "correct";
        beatmap.setBeatmapId(12);
        beatmap.setFileMd5(md5Hex(correctContent));
        doReturn("wrong content").when(beatmapDownloader).getActualBeatmap(12);
        try {
            resource.getFile();
            fail("should have thrown");
        } catch (WebApplicationException e) {
            assertEquals(502, e.getResponse().getStatus());
        }
        try {
            resource.setFile("also wrong");
            fail("should have thrown");
        } catch (WebApplicationException e) {
            assertEquals(403, e.getResponse().getStatus());
        }
        resource.setFile(correctContent);
        assertEquals(correctContent, resource.getFile());
    }
}

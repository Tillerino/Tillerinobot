package tillerino.tillerinobot.rest;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import javax.inject.Inject;

import jakarta.ws.rs.WebApplicationException;

import org.junit.Before;
import org.junit.Test;
import org.tillerino.mormon.Persister.Action;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.ppaddict.util.TestModule;

import tillerino.tillerinobot.AbstractDatabaseTest;
import tillerino.tillerinobot.data.ActualBeatmap;
import tillerino.tillerinobot.rest.AbstractBeatmapResource.BeatmapDownloader;

/**
 * In this test, we want to make sure that the {@link AbstractBeatmapResource}
 * handles downloading and saving beatmaps files correctly with respect to the
 * expected hash value.
 */
@TestModule(value = {}, mocks = BeatmapDownloader.class)
public class AbstractBeatmapResourceTest extends AbstractDatabaseTest {
  @Inject
  BeatmapDownloader downloader;

  OsuApiBeatmap beatmap = new OsuApiBeatmap();

  AbstractBeatmapResource resource;

  @Before
  public void init() {
    resource = new AbstractBeatmapResource(dbm, downloader, beatmap) {
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
    verifyNoInteractions(downloader);
    // data was compressed after the fact
    assertThat(dbm.selectUnique(ActualBeatmap.class)."where beatmapid = \{12}").hasValueSatisfying(ab ->
        assertThat(ab).hasFieldOrPropertyWithValue("content", null));
  }

  @Test
  public void testRedownloadOnWrongHash() throws Exception {
    String oldContent = "hello";
    String newContent = "world";
    dbm.persist(new ActualBeatmap(12, oldContent.getBytes(), null, 1, md5Hex(oldContent)), Action.INSERT);
    beatmap.setBeatmapId(12);
    beatmap.setFileMd5(md5Hex(newContent));
    when(downloader.getActualBeatmap(12)).thenReturn(newContent);
    assertEquals("world", resource.getFile());
  }

  @Test
  public void testThrowAndReuploadOnWrongHash() throws Exception {
    String correctContent = "correct";
    beatmap.setBeatmapId(12);
    beatmap.setFileMd5(md5Hex(correctContent));
    when(downloader.getActualBeatmap(12)).thenReturn("wrong content");
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

package tillerino.tillerinobot.rest;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.tillerino.osuApiModel.OsuApiBeatmap;

import tillerino.tillerinobot.AbstractDatabaseTest;
import tillerino.tillerinobot.data.ActualBeatmap;
import tillerino.tillerinobot.rest.AbstractBeatmapResource.BeatmapDownloader;

/**
 * In this test, we want to make sure that the {@link AbstractBeatmapResource}
 * handles downloading and saving beatmaps files correctly with respect to the
 * expected hash value.
 */
public class AbstractBeatmapResourceTest extends AbstractDatabaseTest {
  @Mock
  BeatmapDownloader downloader;

  OsuApiBeatmap beatmap = new OsuApiBeatmap();

  AbstractBeatmapResource resource;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
    resource = new AbstractBeatmapResource(beatmapFilesRepo, downloader, beatmap) {
      @Override
      public OsuApiBeatmap get() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Test
  public void testCorrectHash() throws Exception {
    String content = "hello";
    // save without hash for backwards compatibility
    beatmapFilesRepo.save(new ActualBeatmap(12, content.getBytes(), 1, ""));
    beatmap.setBeatmapId(12);
    beatmap.setFileMd5(md5Hex(content));
    assertEquals(content, resource.getFile());
    verifyZeroInteractions(downloader);
  }

  @Test
  public void testRedownloadOnWrongHash() throws Exception {
    String oldContent = "hello";
    String newContent = "world";
    beatmapFilesRepo.save(new ActualBeatmap(12, oldContent.getBytes(), 1, md5Hex(oldContent)));
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

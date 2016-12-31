package tillerino.tillerinobot;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.tillerino.osuApiModel.OsuApiBeatmap;

import tillerino.tillerinobot.TestBackend.FakePercentageEstimates;
import tillerino.tillerinobot.diff.PercentageEstimates;

public class BestmapMetaTest {
	@Test
	public void testFuturePpSwitch() throws Exception {
		PercentageEstimates estimates = new FakePercentageEstimates(64, 100);
		OsuApiBeatmap beatmap = new OsuApiBeatmap();
		beatmap.setArtist("Artist");
		beatmap.setTitle("Title");
		beatmap.setVersion("Version");
		beatmap.setTotalLength(111);
		beatmap.setApproachRate(9);
		beatmap.setOverallDifficulty(7);
		beatmap.setBpm(420);
		beatmap.setBeatmapId(69);
		BeatmapMeta meta = new BeatmapMeta(beatmap, 101, estimates);
		assertEquals(
				"[http://osu.ppy.sh/b/69 Artist - Title [Version]] DT   future you: 100pp | 95%: 77pp | 98%: 90pp | 99%: 95pp | 100%: 100pp | 1:14 ★ 3.64 ♫ 630 AR10.33 OD9.08",
				meta.formInfoMessage(true, null, -1, null, null, null));

		meta = new BeatmapMeta(beatmap, 110, estimates);
		assertEquals(
				"[http://osu.ppy.sh/b/69 Artist - Title [Version]] DT   95%: 77pp | 98%: 90pp | 99%: 95pp | 100%: 100pp | 1:14 ★ 3.64 ♫ 630 AR10.33 OD9.08",
				meta.formInfoMessage(true, null, -1, null, null, null));
	}
}

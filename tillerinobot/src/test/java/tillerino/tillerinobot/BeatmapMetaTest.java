package tillerino.tillerinobot;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;

import tillerino.tillerinobot.diff.BeatmapImpl;
import tillerino.tillerinobot.diff.OsuApiBeatmapForDiff;
import tillerino.tillerinobot.diff.PercentageEstimates;
import tillerino.tillerinobot.diff.PercentageEstimatesImpl;

public class BeatmapMetaTest {
	@Test
	public void testFuturePpSwitch() throws Exception {
		BeatmapMeta meta = fakeBeatmapMeta(101);
		assertEquals(
				"[http://osu.ppy.sh/b/69 Artist - Title [Version]] DT   future you: 100pp | 95%: 29pp | 98%: 60pp | 99%: 77pp | 100%: 101pp | 1:14 ★ 2.68 ♫ 630 AR10.33 OD9.08",
				meta.formInfoMessage(true, null, -1, null, null, null));

		meta = fakeBeatmapMeta(110);
		assertEquals(
				"[http://osu.ppy.sh/b/69 Artist - Title [Version]] DT   95%: 29pp | 98%: 60pp | 99%: 77pp | 100%: 101pp | 1:14 ★ 2.68 ♫ 630 AR10.33 OD9.08",
				meta.formInfoMessage(true, null, -1, null, null, null));
	}

	public static BeatmapMeta fakeBeatmapMeta(Integer personalPp) {
		OsuApiBeatmap beatmap = new OsuApiBeatmap();
		beatmap.setArtist("Artist");
		beatmap.setTitle("Title");
		beatmap.setVersion("Version");
		beatmap.setTotalLength(111);
		beatmap.setApproachRate(9);
		beatmap.setOverallDifficulty(7);
		beatmap.setBpm(420);
		beatmap.setBeatmapId(69);
		beatmap.setMaxCombo(100);
		BeatmapImpl cBeatmap = new BeatmapImpl(OsuApiBeatmapForDiff.Mapper.INSTANCE.shrink(beatmap), 1.45, 1, 200, 250, 10, false);
		beatmap.setStarDifficulty(cBeatmap.getStarDiff());
		PercentageEstimates estimates = new PercentageEstimatesImpl(cBeatmap, Mods.getMask(Mods.DoubleTime));
		return new BeatmapMeta(beatmap, personalPp, estimates);
	}
}

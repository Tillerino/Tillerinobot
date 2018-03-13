package tillerino.tillerinobot;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;

import tillerino.tillerinobot.diff.CBeatmapImpl;
import tillerino.tillerinobot.diff.PercentageEstimates;
import tillerino.tillerinobot.diff.PercentageEstimatesImpl;

public class BestmapMetaTest {
	@Test
	public void testFuturePpSwitch() throws Exception {
		BeatmapMeta meta = fakeBeatmapMeta(101);
		assertEquals(
				"[http://osu.ppy.sh/b/69 Artist - Title [Version]] DT   future you: 100pp | 95%: 32pp | 98%: 61pp | 99%: 78pp | 100%: 100pp | 1:14 ★ 2.68 ♫ 630 AR10.33 OD9.08",
				meta.formInfoMessage(true, null, -1, null, null, null));

		meta = fakeBeatmapMeta(110);
		assertEquals(
				"[http://osu.ppy.sh/b/69 Artist - Title [Version]] DT   95%: 32pp | 98%: 61pp | 99%: 78pp | 100%: 100pp | 1:14 ★ 2.68 ♫ 630 AR10.33 OD9.08",
				meta.formInfoMessage(true, null, -1, null, null, null));
	}

	public static BeatmapMeta fakeBeatmapMeta(Integer personalPp) {
		OsuApiBeatmap beatmap = new OsuApiBeatmap();
		CBeatmapImpl cBeatmap = new CBeatmapImpl(beatmap, 1.45, 1, 200, 250, false, false, true);
		beatmap.setArtist("Artist");
		beatmap.setTitle("Title");
		beatmap.setVersion("Version");
		beatmap.setTotalLength(111);
		beatmap.setApproachRate(9);
		beatmap.setOverallDifficulty(7);
		beatmap.setBpm(420);
		beatmap.setBeatmapId(69);
		beatmap.setStarDifficulty(cBeatmap.getStarDiff());
		beatmap.setMaxCombo(100);
		PercentageEstimates estimates = new PercentageEstimatesImpl(cBeatmap, Mods.getMask(Mods.DoubleTime));
		return new BeatmapMeta(beatmap, personalPp, estimates);
	}
}

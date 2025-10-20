package tillerino.tillerinobot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;

import tillerino.tillerinobot.diff.BeatmapImpl;
import tillerino.tillerinobot.diff.PercentageEstimates;
import tillerino.tillerinobot.diff.PercentageEstimatesImpl;

public class BeatmapMetaTest {
	@Test
	public void testFuturePpSwitch() throws Exception {
		BeatmapMeta meta = fakeBeatmapMeta(101);
		assertEquals(
				"[http://osu.ppy.sh/b/69 Artist - Title [Version]] DT   future you: 101pp | 95%: 53pp | 98%: 110pp | 99%: 136pp | 100%: 181pp | 1:14 ★ 2.68 ♫ 630 AR10.33 OD9.08",
				meta.formInfoMessage(true, true, null, -1, null, null, null));

		meta = fakeBeatmapMeta(200);
		assertEquals(
				"[http://osu.ppy.sh/b/69 Artist - Title [Version]] DT   95%: 53pp | 98%: 110pp | 99%: 136pp | 100%: 181pp | 1:14 ★ 2.68 ♫ 630 AR10.33 OD9.08",
				meta.formInfoMessage(true, true, null, -1, null, null, null));
	}

	@Test
	public void testDontShowMeta() throws Exception {
		BeatmapMeta meta = fakeBeatmapMeta(101);
		assertEquals(
				"[http://osu.ppy.sh/b/69 Artist - Title [Version]] DT",
				meta.formInfoMessage(true, false, null, -1, null, null, null));
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
		BeatmapImpl cBeatmap = BeatmapImpl.builder()
				.modsUsed(64)
				.SpeedDifficulty(1.45f)
				.AimDifficulty(1.45f)
				.HitCircleCount(200)
				.SliderCount(40)
				.SpinnerCount(10)
				.ApproachRate(10.33f)
				.OverallDifficulty(9.08f)
				.MaxCombo(100)
				.StarDiff(2.68f)
				.build();
		beatmap.setStarDifficulty(cBeatmap.StarDiff());
		PercentageEstimates estimates = new PercentageEstimatesImpl(cBeatmap, Mods.getMask(Mods.DoubleTime));
		return new BeatmapMeta(beatmap, personalPp, estimates);
	}
}

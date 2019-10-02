package tillerino.tillerinobot.diff;

import static org.junit.Assert.*;

import org.junit.Test;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.OsuApiScore;

import lombok.Value;

public class OsuScoreTest {
	@Value
	static class CBeatmapImpl implements Beatmap {
		private int amountHitCircles;
		private double od;
		private double ar;
		private double aim;
		private double speed;
		private double maxMaxCombo;

		@Override
		public double DifficultyAttribute(long mods, int kind) {
			switch (kind) {
			case Beatmap.OD:
				return od;
			case Beatmap.AR:
				return ar;
			case Beatmap.Aim:
				return aim;
			case Beatmap.Speed:
				return speed;
			case Beatmap.MaxCombo:
				return maxMaxCombo;
			default:
				throw new RuntimeException("" + kind);
			}
		}

		@Override
		public int NumHitCircles() {
			return amountHitCircles;
		}
	}

	@Test
	public void testFreedomDiveCptnXn() throws Exception {
		long mods = 0l;
		OsuApiScore score = new OsuApiScore();
		score.setMaxCombo(2384);
		score.setCount300(1960);
		score.setCount100(23);
		score.setCount50(0);
		score.setCountMiss(0);
		score.setMods(mods);
		OsuApiBeatmap apiBeatmap = new OsuApiBeatmap();
		apiBeatmap.setMaxCombo(2385);
		apiBeatmap.setOverallDifficulty(8);
		apiBeatmap.setApproachRate(9);
		// aim and speed from API
		CBeatmapImpl beatmap = new CBeatmapImpl(1646,
				apiBeatmap.getOverallDifficulty(mods),
				apiBeatmap.getApproachRate(mods), 3.3820393085479736, 3.7348108291625977,
				apiBeatmap.getMaxCombo());
		OsuScore standardScore = new OsuScore(score);
		assertEquals(566.696, standardScore.getPP(beatmap), 2E-3);
	}

	@Test
	public void testFreedomDiveCookiezi() throws Exception {
		long mods = 24l;
		OsuApiScore score = new OsuApiScore();
		score.setMaxCombo(2385);
		score.setCount300(1978);
		score.setCount100(5);
		score.setCount50(0);
		score.setCountMiss(0);
		score.setMods(mods);
		OsuApiBeatmap apiBeatmap = new OsuApiBeatmap();
		apiBeatmap.setMaxCombo(2385);
		apiBeatmap.setOverallDifficulty(8);
		apiBeatmap.setApproachRate(9);
		// aim and speed from API
		CBeatmapImpl beatmap = new CBeatmapImpl(1646,
				apiBeatmap.getOverallDifficulty(mods),
				apiBeatmap.getApproachRate(mods), 3.6932241916656494, 3.9626119136810303,
				apiBeatmap.getMaxCombo());
		OsuScore standardScore = new OsuScore(score);
		assertEquals(894.388, standardScore.getPP(beatmap), 2E-3);
	}

	@Test
	public void testBestFriendsRafis() throws Exception {
		long mods = Mods.getMask(Mods.Hidden, Mods.HardRock, Mods.DoubleTime);
		OsuApiScore score = new OsuApiScore();
		score.setMaxCombo(404);
		score.setCount300(309);
		score.setCount100(4);
		score.setCount50(0);
		score.setCountMiss(0);
		score.setMods(mods);
		OsuApiBeatmap apiBeatmap = new OsuApiBeatmap();
		apiBeatmap.setMaxCombo(404);
		apiBeatmap.setOverallDifficulty(7.0);
		apiBeatmap.setApproachRate(9);
		// aim and speed from API
		CBeatmapImpl beatmap = new CBeatmapImpl(229,
				apiBeatmap.getOverallDifficulty(mods),
				apiBeatmap.getApproachRate(mods), 3.9397411346435547,
				2.8928098678588867, apiBeatmap.getMaxCombo());
		OsuScore standardScore = new OsuScore(score);
		assertEquals(595.042, standardScore.getPP(beatmap), 2); // we're off pretty far here, but it's better than before
	}
}

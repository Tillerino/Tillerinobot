package tillerino.tillerinobot.diff;

import static org.junit.Assert.*;

import org.junit.Test;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.OsuApiScore;

import lombok.Value;

public class CStandardScoreTest {
	@Value
	static class CBeatmapImpl implements CBeatmap {
		private int amountHitCircles;
		private double od;
		private double ar;
		private double aim;
		private double speed;
		private double maxMaxCombo;

		@Override
		public double DifficultyAttribute(long mods, int kind) {
			switch (kind) {
			case CBeatmap.OD:
				return od;
			case CBeatmap.AR:
				return ar;
			case CBeatmap.Aim:
				return aim;
			case CBeatmap.Speed:
				return speed;
			case CBeatmap.MaxCombo:
				return maxMaxCombo;
			default:
				throw new RuntimeException("" + kind);
			}
		}

		@Override
		public int AmountHitCircles() {
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
		// aim and speed from oppai
		CBeatmapImpl beatmap = new CBeatmapImpl(1646,
				apiBeatmap.getOverallDifficulty(mods),
				apiBeatmap.getApproachRate(mods), 3.297445, 3.613207,
				apiBeatmap.getMaxCombo());
		CStandardScore standardScore = new CStandardScore(score);
		assertEquals(520.72, standardScore.getPP(beatmap), 1E-2);
	}

	@Test
	public void testFreedomDiveCookiezi() throws Exception {
		long mods = 16l;
		OsuApiScore score = new OsuApiScore();
		score.setMaxCombo(2384);
		score.setCount300(1957);
		score.setCount100(26);
		score.setCount50(0);
		score.setCountMiss(0);
		score.setMods(mods);
		OsuApiBeatmap apiBeatmap = new OsuApiBeatmap();
		apiBeatmap.setMaxCombo(2385);
		apiBeatmap.setOverallDifficulty(8);
		apiBeatmap.setApproachRate(9);
		// aim and speed from oppai
		CBeatmapImpl beatmap = new CBeatmapImpl(1646,
				apiBeatmap.getOverallDifficulty(mods),
				apiBeatmap.getApproachRate(mods), 3.568212, 3.795921,
				apiBeatmap.getMaxCombo());
		CStandardScore standardScore = new CStandardScore(score);
		assertEquals(711.35, standardScore.getPP(beatmap), 1E-1);
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
		// aim and speed tweaked
		CBeatmapImpl beatmap = new CBeatmapImpl(229,
				apiBeatmap.getOverallDifficulty(mods),
				apiBeatmap.getApproachRate(mods), 3.870850244939327,
				2.8966311118954815, apiBeatmap.getMaxCombo());
		CStandardScore standardScore = new CStandardScore(score);
		assertEquals(614.43, standardScore.getPP(beatmap), 1E-2);
	}
}

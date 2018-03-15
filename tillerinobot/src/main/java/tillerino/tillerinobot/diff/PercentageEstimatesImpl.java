package tillerino.tillerinobot.diff;

import org.tillerino.osuApiModel.types.BitwiseMods;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor(onConstructor = @__(@Deprecated))
public class PercentageEstimatesImpl implements PercentageEstimates {
	private CBeatmapImpl beatmap;

	@Setter(onParam = @__(@BitwiseMods))
	private @BitwiseMods long mods;

	public PercentageEstimatesImpl(CBeatmapImpl beatmap, @BitwiseMods long mods) {
		super();
		this.beatmap = beatmap;
		this.mods = mods;
	}

	@Override
	public double getPP(double acc) {
		AccuracyDistribution dist = AccuracyDistribution.get(getBeatmap().getObjectCount(), 0, acc);

		CStandardScore score = new CStandardScore((int) getBeatmap().DifficultyAttribute(getMods(), CBeatmap.MaxCombo),
				dist.getX300(), dist.getX100(), dist.getX50(), dist.getMiss(), getMods());

		return score.getPP(getBeatmap());
	}

	@Override
	public double getPP(double acc, int combo, int misses) {
		AccuracyDistribution dist = AccuracyDistribution.get(getBeatmap().getObjectCount(), misses, acc);

		CStandardScore score = new CStandardScore(combo, dist.getX300(), dist.getX100(), dist.getX50(), dist.getMiss(),
				getMods());

		return score.getPP(getBeatmap());
	}

	@Override
	public double getPP(int x100, int x50, int combo, int misses) {
		int x300 = getBeatmap().getObjectCount() - x50 - x100;
		CStandardScore score = new CStandardScore(combo, x300, x100, x50, misses, getMods());

		return score.getPP(getBeatmap());
	}

	@Override
	public boolean isShaky() {
		return beatmap.isShaky();
	}

	@Override
	public Double getStarDiff() {
		if (!beatmap.isStable()) {
			return null;
		}
		return beatmap.getStarDiff();
	}

	@Override
	public boolean isOppaiOnly() {
		return beatmap.isOppaiOnly();
	}

	@Override
	public boolean isRanked() {
		return beatmap.getBeatmap().getApproved() > 0 && beatmap.getBeatmap().getApproved() != 3;
	}
}
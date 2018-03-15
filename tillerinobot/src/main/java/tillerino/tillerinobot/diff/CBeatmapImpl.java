package tillerino.tillerinobot.diff;

import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.types.BitwiseMods;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(onConstructor = @__(@Deprecated))
// suppress warning about case-insensitive field collision, because we cannot change the names in CBeatmap
@SuppressWarnings("squid:S1845")
public class CBeatmapImpl implements CBeatmap {
	private OsuApiBeatmap beatmap;
	private double speed;
	private double aim;
	private int circleCount;
	private int objectCount;
	private boolean oppaiOnly;
	private boolean shaky;
	private boolean stable;

	@Override
	@SuppressFBWarnings("NM")
	public double DifficultyAttribute(@BitwiseMods long mods, int kind) {
		switch (kind) {
		case CBeatmap.OD:
			return beatmap.getOverallDifficulty(mods);
		case CBeatmap.AR:
			return beatmap.getApproachRate(mods);
		case CBeatmap.Speed:
			return speed;
		case CBeatmap.Aim:
			return aim;
		case CBeatmap.MaxCombo:
			return beatmap.getMaxCombo();
		default:
			throw new RuntimeException("" + kind);
		}
	}

	@Override
	@SuppressFBWarnings("NM")
	public int AmountHitCircles() {
		return getCircleCount();
	}

	public double getStarDiff() {
		return getAim() + getSpeed() + .5 * Math.abs(getAim() - getSpeed());
	}
}
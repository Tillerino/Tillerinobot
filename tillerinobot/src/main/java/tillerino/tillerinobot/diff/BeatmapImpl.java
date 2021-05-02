package tillerino.tillerinobot.diff;

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
public class BeatmapImpl implements Beatmap {
	private OsuApiBeatmapForDiff beatmap;
	private double speed;
	private double aim;
	private int circleCount;
	private int objectCount;
	private int spinnerCount;
	private boolean oppaiOnly;
	private boolean shaky;
	private boolean stable;

	@Override
	@SuppressFBWarnings("NM")
	public double DifficultyAttribute(@BitwiseMods long mods, int kind) {
		switch (kind) {
		case Beatmap.OD:
			return beatmap.getOverallDifficulty(mods);
		case Beatmap.AR:
			return beatmap.getApproachRate(mods);
		case Beatmap.Speed:
			return speed;
		case Beatmap.Aim:
			return aim;
		case Beatmap.MaxCombo:
			return beatmap.getMaxCombo();
		default:
			throw new UnsupportedOperationException("" + kind);
		}
	}

	@Override
	@SuppressFBWarnings("NM")
	public int NumHitCircles() {
		return getCircleCount();
	}

	public double getStarDiff() {
		if (beatmap.getAimDifficulty() == aim && beatmap.getSpeedDifficulty() == speed) {
			// we got aim and speed from the API and we're precisely matching the mods
			// -> star difficulty is correct
			return beatmap.getStarDifficulty();
		}
		return getAim() + getSpeed() + .5 * Math.abs(getAim() - getSpeed());
	}

	@Override
	public int NumSpinners() {
		return spinnerCount;
	}
}
package tillerino.tillerinobot.diff;

import org.tillerino.osuApiModel.types.BitwiseMods;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Builder;
import lombok.Value;

/**
 * This class implements {@link Beatmap} which is the interface that the
 * translated pp calculation uses to get info about the beatmap.
 */
@Value
@Builder(toBuilder = true)
// suppress warning about case-insensitive field collision, because we cannot change the names in CBeatmap
@SuppressWarnings("squid:S1845")
@SuppressFBWarnings("NM")
public class BeatmapImpl implements Beatmap {
	@BitwiseMods
	private long modsUsed;

	private float starDiff;
	private float aim;
	private float speed;
	private float flashlight;

	private float sliderFactor;

	private float approachRate;
	private float overallDifficulty;

	private int maxCombo;

	private int circleCount;
	private int spinnerCount;
	private int sliderCount;

	@Override
	public float DifficultyAttribute(long mods, int kind) {
		if (Beatmap.getDiffMods(mods) != modsUsed) {
			throw new IllegalArgumentException("Unexpected mods " + mods + ". Was loaded with " + modsUsed);
		}

		return switch (kind) {
			case Beatmap.Aim -> aim;
			case Beatmap.AR -> approachRate;
			case Beatmap.Flashlight -> flashlight;
			case Beatmap.MaxCombo -> maxCombo;
			case Beatmap.OD -> overallDifficulty;
			case Beatmap.SliderFactor -> sliderFactor;
			case Beatmap.Speed -> speed;
			default -> throw new IllegalArgumentException("Unexpected kind: " + kind);
		};
	}

	@Override
	public int NumHitCircles() {
		return circleCount;
	}

	@Override
	public int NumSpinners() {
		return spinnerCount;
	}

	@Override
	public int NumSliders() {
		return sliderCount;
	}

	public int getObjectCount() {
		return circleCount + sliderCount + spinnerCount;
	}
}
package tillerino.tillerinobot.diff;

import static org.tillerino.osuApiModel.Mods.DoubleTime;
import static org.tillerino.osuApiModel.Mods.Easy;
import static org.tillerino.osuApiModel.Mods.HalfTime;
import static org.tillerino.osuApiModel.Mods.HardRock;
import static org.tillerino.osuApiModel.Mods.Nightcore;
import static org.tillerino.osuApiModel.Mods.getMask;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.types.BitwiseMods;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This interface imitates the Beatmap class used by {@link OsuScore}.
 */
//suppress all found Sonar warnings, since we are trying to copy C# code
@SuppressWarnings({ "squid:S00100", "squid:S1214", "squid:S00115" })
public interface Beatmap {
	public enum EScoreVersion {
		ScoreV1, ScoreV2
	}

	public static final int Aim = 0;
	public static final int Speed = 1;
	public static final int OD = 2;
	public static final int AR = 3;
	public static final int MaxCombo = 4;
	public static final int SliderFactor = 5;
	public static final int Flashlight = 6;

	@BitwiseMods
	static long diffMods = getMask(HalfTime, DoubleTime, Easy, HardRock, Mods.Flashlight);

	/**
	 * returns only HT, DT, EZ, HR, and FL, converting NC to DT
	 * @param mods
	 * @return
	 */
	@SuppressFBWarnings(value = "TQ", justification = "Producer")
	public static @BitwiseMods long getDiffMods(@BitwiseMods long mods) {
		if(Nightcore.is(mods)) {
			mods |= getMask(DoubleTime);
			mods ^= getMask(Nightcore);
		}

		mods = mods & diffMods;

		return mods;
	}

	float DifficultyAttribute(@BitwiseMods long mods, int kind);

	/**
	 * normal circles, not sliders or spinners
	 * 
	 * @return
	 */
	int NumHitCircles();

	default EScoreVersion ScoreVersion() {
		// as of now, we don't care about score versions.
		return null;
	}

	int NumSpinners();

	int NumSliders();

}
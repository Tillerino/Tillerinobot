package tillerino.tillerinobot.diff;

import static org.tillerino.osuApiModel.Mods.TouchDevice;
import static org.tillerino.osuApiModel.Mods.DoubleTime;
import static org.tillerino.osuApiModel.Mods.Easy;
import static org.tillerino.osuApiModel.Mods.HalfTime;
import static org.tillerino.osuApiModel.Mods.HardRock;
import static org.tillerino.osuApiModel.Mods.Nightcore;
import static org.tillerino.osuApiModel.Mods.Hidden;
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
	@BitwiseMods long diffMods = getMask(TouchDevice, HalfTime, DoubleTime, Easy, HardRock, Mods.Flashlight);

	/**
	 * returns only TD, HT, DT, EZ, HR, and FL, converting NC to DT. Also includes HD, but only if FL is present
	 * @param mods
	 * @return
	 */
	@SuppressFBWarnings(value = "TQ", justification = "Producer")
	public static @BitwiseMods long getDiffMods(@BitwiseMods long mods) {
		if(Nightcore.is(mods)) {
			mods |= getMask(DoubleTime);
			mods ^= getMask(Nightcore);
		}

		boolean hdfl = Mods.Flashlight.is(mods) && Hidden.is(mods);

		mods = mods & diffMods;

		if(hdfl) {
			// re-apply HD if used in combination with FL
			mods |= getMask(Hidden);
		}

		return mods;
	}
}
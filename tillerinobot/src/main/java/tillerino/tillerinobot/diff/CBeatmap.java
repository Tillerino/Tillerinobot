package tillerino.tillerinobot.diff;

/**
 * This interface imitates the CBeatmap class used by {@link CStandardScore}.
 */
//suppress all found Sonar warnings, since we are trying to copy C# code
@SuppressWarnings({ "squid:S00100", "squid:S1214", "squid:S00115" })
public interface CBeatmap {
	public enum EScoreVersion {
		ScoreV1, ScoreV2
	}

	public static final int Aim = 0;
	public static final int Speed = 1;
	public static final int OD = 2;
	public static final int AR = 3;
	public static final int MaxCombo = 4;

	double DifficultyAttribute(long mods, int kind);

	/**
	 * normal circles, not sliders or spinners
	 * 
	 * @return
	 */
	int AmountHitCircles();

	default EScoreVersion ScoreVersion() {
		// as of now, we don't care about score versions.
		return null;
	}

}
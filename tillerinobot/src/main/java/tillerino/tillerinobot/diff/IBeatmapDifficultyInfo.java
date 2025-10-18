// Copyright (c) ppy Pty Ltd <contact@ppy.sh>. Licensed under the MIT Licence.
// See the LICENCE file in the repository root for full licence text.

package tillerino.tillerinobot.diff;

/**
 * A representation of all top-level difficulty settings for a beatmap.
 */
public interface IBeatmapDifficultyInfo
{
    /**
     * The default value used for all difficulty settings except SliderMultiplier and SliderTickRate.
     */
    float DEFAULT_DIFFICULTY = 5;

    /**
     * The drain rate of the associated beatmap.
     */
    float getDrainRate();

    /**
     * The circle size of the associated beatmap.
     */
    float getCircleSize();

    /**
     * The overall difficulty of the associated beatmap.
     */
    float getOverallDifficulty();

    /**
     * The approach rate of the associated beatmap.
     */
    float getApproachRate();

    /**
     * The base slider velocity of the associated beatmap.
     * This was known as "SliderMultiplier" in the .osu format and stable editor.
     */
    double getSliderMultiplier();

    /**
     * The slider tick rate of the associated beatmap.
     */
    double getSliderTickRate();

    /**
     * Maps a difficulty value [0, 10] to a two-piece linear range of values.
     * @param difficulty The difficulty value to be mapped.
     * @param min Minimum of the resulting range which will be achieved by a difficulty value of 0.
     * @param mid Midpoint of the resulting range which will be achieved by a difficulty value of 5.
     * @param max Maximum of the resulting range which will be achieved by a difficulty value of 10.
     * @return Value to which the difficulty value maps in the specified range.
     */
    static double DifficultyRange(double difficulty, double min, double mid, double max)
    {
        if (difficulty > 5)
            return mid + (max - mid) * DifficultyRange(difficulty);
        if (difficulty < 5)
            return mid + (mid - min) * DifficultyRange(difficulty);

        return mid;
    }

    /**
     * Maps a difficulty value [0, 10] to a linear range of [-1, 1].
     * @param difficulty The difficulty value to be mapped.
     * @return Value to which the difficulty value maps in the specified range.
     */
    static double DifficultyRange(double difficulty)
    {
        return (difficulty - 5) / 5;
    }

    /**
     * Maps a difficulty value [0, 10] to a two-piece linear range of values.
     * @param difficulty The difficulty value to be mapped.
     * @param range The values that define the two linear ranges.
     * @return Value to which the difficulty value maps in the specified range.
     */
    static double DifficultyRange(double difficulty, DifficultyRange range)
    {
        return DifficultyRange(difficulty, range.Min, range.Mid, range.Max);
    }

    /**
     * Inverse function to DifficultyRange(double,double,double,double).
     * Maps a value returned by the function above back to the difficulty that produced it.
     * @param difficultyValue The difficulty-dependent value to be unmapped.
     * @param diff0 Minimum of the resulting range which will be achieved by a difficulty value of 0.
     * @param diff5 Midpoint of the resulting range which will be achieved by a difficulty value of 5.
     * @param diff10 Maximum of the resulting range which will be achieved by a difficulty value of 10.
     * @return Value to which the difficulty value maps in the specified range.
     */
    static double InverseDifficultyRange(double difficultyValue, double diff0, double diff5, double diff10)
    {
        return Math.signum(difficultyValue - diff5) == Math.signum(diff10 - diff5)
            ? (difficultyValue - diff5) / (diff10 - diff5) * 5 + 5
            : (difficultyValue - diff5) / (diff5 - diff0) * 5 + 5;
    }

    /**
     * Inverse function to DifficultyRange(double,DifficultyRange).
     * Maps a value returned by the function above back to the difficulty that produced it.
     * @param difficultyValue The difficulty-dependent value to be unmapped.
     * @param range Minimum of the resulting range which will be achieved by a difficulty value of 0.
     * @return Value to which the difficulty value maps in the specified range.
     */
    static double InverseDifficultyRange(double difficultyValue, DifficultyRange range)
    {
        return InverseDifficultyRange(difficultyValue, range.Min, range.Mid, range.Max);
    }

    /**
     * Represents a piecewise-linear difficulty curve for a given gameplay quantity.
     * @param Min Minimum of the resulting range which will be achieved by a difficulty value of 0.
     * @param Mid Midpoint of the resulting range which will be achieved by a difficulty value of 5.
     * @param Max Maximum of the resulting range which will be achieved by a difficulty value of 10.
     */
    record DifficultyRange(double Min, double Mid, double Max) {}
}
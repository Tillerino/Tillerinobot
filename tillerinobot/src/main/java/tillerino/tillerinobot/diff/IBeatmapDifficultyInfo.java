// This is AI-translated, stripped down, and manually adjusted from the C# source
// spotless:off

// Copyright (c) ppy Pty Ltd <contact@ppy.sh>. Licensed under the MIT Licence.
// See the LICENCE file in the repository root for full licence text.

package tillerino.tillerinobot.diff;

/**
 * A representation of all top-level difficulty settings for a beatmap.
 */
public interface IBeatmapDifficultyInfo
{
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
     * Represents a piecewise-linear difficulty curve for a given gameplay quantity.
     * @param Min Minimum of the resulting range which will be achieved by a difficulty value of 0.
     * @param Mid Midpoint of the resulting range which will be achieved by a difficulty value of 5.
     * @param Max Maximum of the resulting range which will be achieved by a difficulty value of 10.
     */
    record DifficultyRange(double Min, double Mid, double Max) {}
}

// spotless:on

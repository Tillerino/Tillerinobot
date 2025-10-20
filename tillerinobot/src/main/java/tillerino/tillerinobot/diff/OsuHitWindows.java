// This is AI-translated, stripped down, and manually adjusted from the C# source

// Copyright (c) ppy Pty Ltd <contact@ppy.sh>. Licensed under the MIT Licence.
// See the LICENCE file in the repository root for full licence text.

package tillerino.tillerinobot.diff;

import tillerino.tillerinobot.diff.IBeatmapDifficultyInfo.DifficultyRange;

public class OsuHitWindows
{
    public static final DifficultyRange GREAT_WINDOW_RANGE = new DifficultyRange(80, 50, 20);
    public static final DifficultyRange OK_WINDOW_RANGE = new DifficultyRange(140, 100, 60);
    public static final DifficultyRange MEH_WINDOW_RANGE = new DifficultyRange(200, 150, 100);

    /**
     * osu! ruleset has a fixed miss window regardless of difficulty settings.
     */
    public static final double MISS_WINDOW = 400;

    public double great;
    public double ok;
    public double meh;

    public void SetDifficulty(double difficulty)
    {
        great = Math.floor(IBeatmapDifficultyInfo.DifficultyRange(difficulty, GREAT_WINDOW_RANGE)) - 0.5;
        ok = Math.floor(IBeatmapDifficultyInfo.DifficultyRange(difficulty, OK_WINDOW_RANGE)) - 0.5;
        meh = Math.floor(IBeatmapDifficultyInfo.DifficultyRange(difficulty, MEH_WINDOW_RANGE)) - 0.5;
    }
}
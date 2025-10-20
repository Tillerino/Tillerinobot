// This is AI-translated, stripped down, and manually adjusted from the C# source

// Copyright (c) ppy Pty Ltd <contact@ppy.sh>. Licensed under the MIT Licence.
// See the LICENCE file in the repository root for full licence text.

package tillerino.tillerinobot.diff;

public abstract class OsuStrainSkill
{
    public static double DifficultyToPerformance(double difficulty)
    {
        return Math.pow(5.0 * Math.max(1.0, difficulty / 0.0675) - 4.0, 3.0) / 100000.0;
    }
}
// This is AI-translated, stripped down, and manually adjusted from the C# source

// Copyright (c) ppy Pty Ltd <contact@ppy.sh>. Licensed under the MIT Licence.
// See the LICENCE file in the repository root for full licence text.

package tillerino.tillerinobot.diff;

/**
 * Represents the skill required to memorise and hit every object in a map with the Flashlight mod enabled.
 */
public class Flashlight
{
    public static double DifficultyToPerformance(double difficulty) { return 25 * Math.pow(difficulty, 2); }
}
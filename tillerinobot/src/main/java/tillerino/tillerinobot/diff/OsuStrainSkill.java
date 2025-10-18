package tillerino.tillerinobot.diff;// Copyright (c) ppy Pty Ltd <contact@ppy.sh>. Licensed under the MIT Licence.
// See the LICENCE file in the repository root for full licence text.

public abstract class OsuStrainSkill
{
    /**
     * The number of sections with the highest strains, which the peak strain reductions will apply to.
     * This is done in order to decrease their impact on the overall difficulty of the map for this skill.
     */
    protected int getReducedSectionCount() { return 10; }

    /**
     * The baseline multiplier applied to the section with the biggest strain.
     */
    protected double getReducedStrainBaseline() { return 0.75; }

    public static double DifficultyToPerformance(double difficulty)
    {
        return Math.pow(5.0 * Math.max(1.0, difficulty / 0.0675) - 4.0, 3.0) / 100000.0;
    }
}
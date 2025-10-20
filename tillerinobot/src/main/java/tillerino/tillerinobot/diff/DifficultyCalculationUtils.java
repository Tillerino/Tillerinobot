// This is AI-translated, stripped down, and manually adjusted from the C# source

// Copyright (c) ppy Pty Ltd <contact@ppy.sh>. Licensed under the MIT Licence.
// See the LICENCE file in the repository root for full licence text.

package tillerino.tillerinobot.diff;

public final class DifficultyCalculationUtils
{

    /**
     * Reverse linear interpolation function (https://en.wikipedia.org/wiki/Linear_interpolation)
     * @param x Value to calculate the function for
     * @param start Value at which function returns 0
     * @param end Value at which function returns 1
     * @return The reverse lerp result
     */
    public static double ReverseLerp(double x, double start, double end)
    {
        return Math.max(0, Math.min(1, (x - start) / (end - start)));
    }

    public static double ErfInv(double pLowerBound) {
        return org.apache.commons.math3.special.Erf.erfInv(pLowerBound);
    }

    public static double Erf(double v) {
        return org.apache.commons.math3.special.Erf.erf(v);
    }
}
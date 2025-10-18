// Copyright (c) ppy Pty Ltd <contact@ppy.sh>. Licensed under the MIT Licence.
// See the LICENCE file in the repository root for full licence text.

package tillerino.tillerinobot.diff;

import java.util.Arrays;

public final class DifficultyCalculationUtils
{
    /**
     * Converts BPM value into milliseconds
     * @param bpm Beats per minute
     * @param delimiter Which rhythm delimiter to use, default is 1/4
     * @return BPM conveted to milliseconds
     */
    public static double BPMToMilliseconds(double bpm, int delimiter)
    {
        return 60000.0 / delimiter / bpm;
    }

    /**
     * Converts BPM value into milliseconds with default delimiter (1/4)
     * @param bpm Beats per minute
     * @return BPM conveted to milliseconds
     */
    public static double BPMToMilliseconds(double bpm)
    {
        return BPMToMilliseconds(bpm, 4);
    }

    /**
     * Converts milliseconds value into a BPM value
     * @param ms Milliseconds
     * @param delimiter Which rhythm delimiter to use, default is 1/4
     * @return Milliseconds conveted to beats per minute
     */
    public static double MillisecondsToBPM(double ms, int delimiter)
    {
        return 60000.0 / (ms * delimiter);
    }

    /**
     * Converts milliseconds value into a BPM value with default delimiter (1/4)
     * @param ms Milliseconds
     * @return Milliseconds conveted to beats per minute
     */
    public static double MillisecondsToBPM(double ms)
    {
        return MillisecondsToBPM(ms, 4);
    }

    /**
     * Calculates a S-shaped logistic function (https://en.wikipedia.org/wiki/Logistic_function)
     * @param x Value to calculate the function for
     * @param midpointOffset How much the function midpoint is offset from zero x
     * @param multiplier Growth rate of the function
     * @param maxValue Maximum value returnable by the function
     * @return The output of logistic function of x
     */
    public static double Logistic(double x, double midpointOffset, double multiplier, double maxValue)
    {
        return maxValue / (1 + Math.exp(multiplier * (midpointOffset - x)));
    }

    /**
     * Calculates a S-shaped logistic function with default maxValue (1)
     * @param x Value to calculate the function for
     * @param midpointOffset How much the function midpoint is offset from zero x
     * @param multiplier Growth rate of the function
     * @return The output of logistic function of x
     */
    public static double Logistic(double x, double midpointOffset, double multiplier)
    {
        return Logistic(x, midpointOffset, multiplier, 1);
    }

    /**
     * Calculates a S-shaped logistic function (https://en.wikipedia.org/wiki/Logistic_function)
     * @param exponent Exponent
     * @param maxValue Maximum value returnable by the function
     * @return The output of logistic function
     */
    public static double Logistic(double exponent, double maxValue)
    {
        return maxValue / (1 + Math.exp(exponent));
    }

    /**
     * Calculates a S-shaped logistic function with default maxValue (1)
     * @param exponent Exponent
     * @return The output of logistic function
     */
    public static double Logistic(double exponent)
    {
        return Logistic(exponent, 1);
    }

    /**
     * Returns the p-norm of an n-dimensional vector (https://en.wikipedia.org/wiki/Norm_(mathematics))
     * @param p The value of p to calculate the norm for
     * @param values The coefficients of the vector
     * @return The p-norm of the vector
     */
    public static double Norm(double p, double[] values)
    {
        return Math.pow(Arrays.stream(values).map(x -> Math.pow(x, p)).sum(), 1 / p);
    }

    /**
     * Calculates a Gaussian-based bell curve function (https://en.wikipedia.org/wiki/Gaussian_function)
     * @param x Value to calculate the function for
     * @param mean The mean (center) of the bell curve
     * @param width The width (spread) of the curve
     * @param multiplier Multiplier to adjust the curve's height
     * @return The output of the bell curve function of x
     */
    public static double BellCurve(double x, double mean, double width, double multiplier)
    {
        return multiplier * Math.exp(Math.E * -(Math.pow(x - mean, 2) / Math.pow(width, 2)));
    }

    /**
     * Calculates a Gaussian-based bell curve function with default multiplier (1.0)
     * @param x Value to calculate the function for
     * @param mean The mean (center) of the bell curve
     * @param width The width (spread) of the curve
     * @return The output of the bell curve function of x
     */
    public static double BellCurve(double x, double mean, double width)
    {
        return BellCurve(x, mean, width, 1.0);
    }

    /**
     * Smoothstep function (https://en.wikipedia.org/wiki/Smoothstep)
     * @param x Value to calculate the function for
     * @param start Value at which function returns 0
     * @param end Value at which function returns 1
     * @return The smoothstep result
     */
    public static double Smoothstep(double x, double start, double end)
    {
        x = Math.max(0, Math.min(1, (x - start) / (end - start)));
        return x * x * (3.0 - 2.0 * x);
    }

    /**
     * Smootherstep function (https://en.wikipedia.org/wiki/Smoothstep#Variations)
     * @param x Value to calculate the function for
     * @param start Value at which function returns 0
     * @param end Value at which function returns 1
     * @return The smootherstep result
     */
    public static double Smootherstep(double x, double start, double end)
    {
        x = Math.max(0, Math.min(1, (x - start) / (end - start)));
        return x * x * x * (x * (6.0 * x - 15.0) + 10.0);
    }

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

    private DifficultyCalculationUtils()
    {
        // Private constructor to prevent instantiation
    }

    public static double ErfInv(double pLowerBound) {
        return org.apache.commons.math3.special.Erf.erfInv(pLowerBound);
    }

    public static double Erf(double v) {
        return org.apache.commons.math3.special.Erf.erf(v);
    }
}
package tillerino.tillerinobot.diff;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class MathHelper {
	static double static_cast(int x) {
		return x;
	}

	static double static_cast(double x) {
		return x;
	}

	static double clamp(double x, double min, double max) {
		return max(min, min(max, x));
	}
}

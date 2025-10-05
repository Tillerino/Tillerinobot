package tillerino.tillerinobot.diff;

import static java.lang.Math.max;
import static java.lang.Math.min;

@SuppressWarnings( "squid:S00100" )
public final class MathHelper {
	private MathHelper() {
		// utility class
	}

	static float static_cast_f32(int x) {
		return x;
	}

	static float static_cast_f32(double x) {
		return (float) x;
	}

	static int static_cast_s32(double x) {
		return (int) x;
	}

	static float std_pow(float b, float e) {
		return (float) Math.pow(b, e);
	}

	static float pow(float b, float e) {
		return (float) Math.pow(b, e);
	}

	static float Clamp(float x, float min, float max) {
		return max(min, min(max, x));
	}

	static float log10(float x) {
		return (float) Math.log10(x);
	}

	static float std_log(float x) {
		return (float) Math.log(x);
	}

	static float std_max(float x, float y) {
		return Math.max(x, y);
	}

	static float std_min(float x, float y) {
		return Math.min(x, y);
	}

	static int std_max(int x, int y) {
		return Math.max(x, y);
	}
}

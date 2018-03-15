package tillerino.tillerinobot.lang;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/*
 * 20 bytes
 */
/**
 * Device to permanently shuffle an array of Strings while the size of this
 * object is independent of the number of strings. This is accomplished by
 * saving the seed for shuffling the array and shuffling it every time a String
 * is requested instead of shuffling it once.
 */
public class StringShuffler implements Serializable {
	private static final long serialVersionUID = 1L;
	/*
	 * 8 bytes;
	 */
	final long seed;

	public StringShuffler(Random globalRandom) {
		seed = globalRandom.nextLong();
	}

	/*
	 * 4 bytes
	 */
	int index = 0;

	public String get(String... strings) {
		Random random = new Random(seed);

		String[] forShuffling = strings.clone();

		Collections.shuffle(Arrays.asList(forShuffling), random);

		return forShuffling[(index++) % forShuffling.length];
	}
}

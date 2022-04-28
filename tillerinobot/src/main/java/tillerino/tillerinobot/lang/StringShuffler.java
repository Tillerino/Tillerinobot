package tillerino.tillerinobot.lang;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonCreator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Getter;

/*
 * 20 bytes
 */
/**
 * Device to permanently shuffle an array of Strings while the size of this
 * object is independent of the number of strings. This is accomplished by
 * saving the seed for shuffling the array and shuffling it every time a String
 * is requested instead of shuffling it once.
 */
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
@Getter
public class StringShuffler {
	/*
	 * 8 bytes;
	 */
	private final long seed;

	@SuppressFBWarnings(value = "DMI_RANDOM_USED_ONLY_ONCE", justification = "false positive")
	public StringShuffler(Random globalRandom) {
		seed = globalRandom.nextLong();
	}

	/*
	 * 4 bytes
	 */
	private int index = 0;

	public String get(String... strings) {
		Random random = new Random(seed);

		String[] forShuffling = strings.clone();

		Collections.shuffle(Arrays.asList(forShuffling), random);

		return forShuffling[(index++) % forShuffling.length];
	}
}

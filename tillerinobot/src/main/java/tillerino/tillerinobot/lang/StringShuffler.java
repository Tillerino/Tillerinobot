package tillerino.tillerinobot.lang;

import java.util.Arrays;
import java.util.Collections;

public class StringShuffler {
	String[] strings;
	int index = 0;
	
	public StringShuffler(String... strings) {
		this.strings = strings;
		Collections.shuffle(Arrays.asList(strings));
	}
	
	public String get() {
		return strings[index++];
	}
}

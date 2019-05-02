package org.tillerino.ppaddict.util;

import java.util.Random;

public class LoggingUtils {

	public static String getRandomString(int length) {
		Random r = new Random();
		char[] chars = new char[length];
		for (int j = 0; j < chars.length; j++) {
			chars[j] = (char) ('A' + r.nextInt(26));
		}
		return new String(chars);
	}

}

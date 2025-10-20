package org.tillerino.ppaddict.util;

import java.util.concurrent.ThreadLocalRandom;

public class LoggingUtils {
    public static String getRandomString(int length) {
        char[] chars = new char[length];
        for (int j = 0; j < chars.length; j++) {
            chars[j] = (char) ('A' + ThreadLocalRandom.current().nextInt(26));
        }
        return new String(chars);
    }
}

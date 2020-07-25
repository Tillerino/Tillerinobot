package org.tillerino.ppaddict.util;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.apache.commons.lang3.StringUtils;

import tillerino.tillerinobot.handlers.LinkPpaddictHandler;

/**
 * Used to generate 32-char tokens for linking accounts across platforms (e.g. osu<>ppaddict or Patreon<>Tillerinobot).
 * These tokens should match {@link LinkPpaddictHandler#TOKEN_PATTERN}.
 */
public class KeyGenerator {
	private final SecureRandom random = new SecureRandom();

	public synchronized String get() {
		return StringUtils.leftPad(new BigInteger(165, random).toString(36), 32, '0');
	}
}

package org.tillerino.ppaddict.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import tillerino.tillerinobot.handlers.LinkPpaddictHandler;

public class KeyGeneratorTest {
	KeyGenerator generator = new KeyGenerator();

	@Test
	public void testPattern() throws Exception {
		assertTrue(LinkPpaddictHandler.TOKEN_PATTERN.matcher(generator.get()).matches());
	}
}
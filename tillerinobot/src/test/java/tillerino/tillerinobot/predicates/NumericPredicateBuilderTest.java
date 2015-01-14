package tillerino.tillerinobot.predicates;

import static org.junit.Assert.*;

import org.junit.Test;

import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Default;

public class NumericPredicateBuilderTest {
	NumericPredicateBuilder<TitleLength> builder = new NumericPredicateBuilder<>(
			new TitleLength());

	@Test
	public void testEquals() throws Exception {
		NumericPropertyPredicate<TitleLength> build = builder
				.build("tl=12", null);

		assertEquals(new NumericPropertyPredicate<>(
				"tl=12", new TitleLength(), 12, true, 12, true), build);
	}

	@Test
	public void testGEQ() throws Exception {
		NumericPropertyPredicate<TitleLength> build = builder
				.build("tl>=12", null);

		assertEquals(new NumericPropertyPredicate<>(
				"tl>=12", new TitleLength(), 12, true, Double.POSITIVE_INFINITY, true),
				build);
	}

	@Test
	public void testG() throws Exception {
		NumericPropertyPredicate<TitleLength> build = builder
				.build("tl>12", null);

		assertEquals(new NumericPropertyPredicate<>(
				"tl>12", new TitleLength(), 12, false, Double.POSITIVE_INFINITY, true),
				build);
	}

	@Test
	public void testLEQ() throws Exception {
		NumericPropertyPredicate<TitleLength> build = builder
				.build("tl<=12", null);

		assertEquals(new NumericPropertyPredicate<>(
				"tl<=12", new TitleLength(), Double.NEGATIVE_INFINITY, true, 12, true),
				build);
	}

	@Test
	public void testL() throws Exception {
		NumericPropertyPredicate<TitleLength> build = builder
				.build("tl<12", null);

		assertEquals(new NumericPropertyPredicate<>(
				"tl<12", new TitleLength(), Double.NEGATIVE_INFINITY, true, 12, false),
				build);
	}

	@Test
	public void testUndetected() throws Exception {
		assertNull(builder.build("tx<=12", null));
	}

	@Test(expected = UserException.class)
	public void testWrongSign() throws Exception {
		builder.build("tl~12", new Default());
	}

	@Test(expected = UserException.class)
	public void testWrongNumberFormat() throws Exception {
		builder.build("tl=twelve", new Default());
	}
}

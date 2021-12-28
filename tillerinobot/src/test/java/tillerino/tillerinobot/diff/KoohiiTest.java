package tillerino.tillerinobot.diff;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Collections;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

public class KoohiiTest {
	@Rule
	public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

	@Test
	public void testSimple() throws Exception {
		try (InputStream is = getClass().getResourceAsStream(
				"/Fujijo Seitokai Shikkou-bu - Best FriendS -TV Size- (Flask) [Fycho's Insane].osu")) {
			DifficultyProperties diff = new Koohii().calculate(is, Collections.emptyList());
			assertEquals(2.02832, diff.getSpeed(), 3E-3);
			assertEquals(2.62268, diff.getAim(), 3E-3);
			assertEquals(313, diff.getAllObjectsCount());
			assertEquals(229, diff.getCircleCount());
			assertEquals(404, diff.getMaxCombo());
		}
	}

	@Test
	public void testFourDimensions() throws Exception {
		try (InputStream is = getClass().getResourceAsStream(
				"/xi - FREEDOM DiVE (Nakagawa-Kanon) [FOUR DIMENSIONS].osu")) {
			DifficultyProperties diff = new Koohii().calculate(is, Collections.emptyList());
			softly.assertThat(diff.getSpeed()).as("speed").isEqualTo(3.73481, offset(2E-4));
			softly.assertThat(diff.getAim()).as("aim").isEqualTo(3.38204, offset(1E-3));
			softly.assertThat(diff.getAllObjectsCount()).as("object count").isEqualTo(1983);
			softly.assertThat(diff.getCircleCount()).as("circle count").isEqualTo(1646);
			softly.assertThat(diff.getMaxCombo()).as("max max combo").isEqualTo(2385);
		}
	}

	@Test
	public void testEmptyMap() throws Exception {
		try (InputStream is = getClass().getResourceAsStream(
				"/MOSAIC.WAV - Magical Pants (Short Ver.) (Imaginative) [look at bg].osu")) {
			DifficultyProperties diff = new Koohii().calculate(is, Collections.emptyList());
			assertThat(diff.getAim()).isEqualTo(0D);
		}
	}
}

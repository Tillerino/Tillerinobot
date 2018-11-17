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
		try (InputStream is = ClassLoader.getSystemResourceAsStream(
				"Fujijo Seitokai Shikkou-bu - Best FriendS -TV Size- (Flask) [Fycho's Insane].osu")) {
			DifficultyProperties diff = new Koohii().calculate(is, Collections.emptyList());
			assertEquals(2.0308, diff.getSpeed(), 1E-4);
			assertEquals(2.5894, diff.getAim(), 1E-4);
			assertEquals(313, diff.getAllObjectsCount());
			assertEquals(229, diff.getCircleCount());
			assertEquals(404, diff.getMaxCombo());
		}
	}

	@Test
	public void testFourDimensions() throws Exception {
		try (InputStream is = ClassLoader.getSystemResourceAsStream(
				"xi - FREEDOM DiVE (Nakagawa-Kanon) [FOUR DIMENSIONS].osu")) {
			DifficultyProperties diff = new Koohii().calculate(is, Collections.emptyList());
			softly.assertThat(diff.getSpeed()).as("speed").isEqualTo(3.613207, offset(1E-4));
			softly.assertThat(diff.getAim()).as("aim").isEqualTo(3.297445, offset(1E-4));
			softly.assertThat(diff.getAllObjectsCount()).as("object count").isEqualTo(1983);
			softly.assertThat(diff.getCircleCount()).as("circle count").isEqualTo(1646);
			softly.assertThat(diff.getMaxCombo()).as("max max combo").isEqualTo(2385);
		}
	}
}

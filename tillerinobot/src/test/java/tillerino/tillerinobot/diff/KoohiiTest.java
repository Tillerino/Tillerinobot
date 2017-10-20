package tillerino.tillerinobot.diff;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Collections;

import org.junit.Test;

public class KoohiiTest {
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
}

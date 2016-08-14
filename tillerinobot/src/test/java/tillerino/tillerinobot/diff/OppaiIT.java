package tillerino.tillerinobot.diff;

import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import tillerino.tillerinobot.diff.Oppai;
import tillerino.tillerinobot.diff.Oppai.OppaiResults;

/**
 * This class requires oppai to be installed.
 */
public class OppaiIT {
	@Test
	public void testBasic() throws Exception {
		byte[] content = IOUtils
				.toByteArray(ClassLoader
						.getSystemResource("Fujijo Seitokai Shikkou-bu - Best FriendS -TV Size- (Flask) [Fycho's Insane].osu"));
		OppaiResults results = new Oppai().runOppai(content, Collections.emptyList());
		System.out.println(results);
	}
}

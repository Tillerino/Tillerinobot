package tillerino.tillerinobot.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static tillerino.tillerinobot.websocket.LiveActivityEndpoint.anonymizeHashCode;

import org.junit.Test;

public class LiveActivityEndpointTest {
	@Test
	public void testAnonymize() throws Exception {
		// deterministic
		assertThat(anonymizeHashCode("a", "b")).isEqualTo(anonymizeHashCode("a", "b"));
		// change salt
		assertThat(anonymizeHashCode("a", "b")).isNotEqualTo(anonymizeHashCode("a", "c"));
		// change object
		assertThat(anonymizeHashCode("a", "b")).isNotEqualTo(anonymizeHashCode("d", "b"));
		// swap object and salt
		assertThat(anonymizeHashCode("a", "b")).isNotEqualTo(anonymizeHashCode("b", "a"));
	}
}

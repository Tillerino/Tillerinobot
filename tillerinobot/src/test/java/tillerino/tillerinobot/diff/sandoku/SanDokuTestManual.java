package tillerino.tillerinobot.diff.sandoku;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.assertj.core.data.Offset;
import org.junit.Test;

/**
 * Manual test against a locally running instance at port 8080.
 * If you want to run this, go
 *
 * clone https://github.com/omkelderman/SanDoku.git
 * docker build -t san-doku SanDoku
 * docker run --rm -it -p 8080:80 san-doku
 *
 * Ctrl-C to stop SanDoku.
 */
public class SanDokuTestManual {
	@Test
	public void bestFriends() throws Exception {
		SanDoku sanDoku = SanDoku.defaultClient(URI.create("http://localhost:8080"));
		byte[] beatmap = IOUtils.resourceToByteArray("/Fujijo Seitokai Shikkou-bu - Best FriendS -TV Size- (Flask) [Fycho's Insane].osu");
		SanDokuResponse diff = sanDoku.getDiff(0, 0, beatmap);
		assertThat(diff.getDiffCalcResult()).satisfies(result -> {
			Offset<Double> apiRounding = Offset.offset(1E-5); // API rounds to 5 decimals
			assertThat(result.getStarRating()).isCloseTo(4.70888, apiRounding);
			assertThat(result.getAimStrain()).isCloseTo(2.53559, apiRounding);
			assertThat(result.getSpeedStrain()).isCloseTo(1.89119, apiRounding);
		});
	}
}

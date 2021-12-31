package tillerino.tillerinobot.diff.sandoku;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.assertj.core.data.Offset;
import org.junit.Test;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiScore;

import tillerino.tillerinobot.diff.OsuScore;

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
	SanDoku sanDoku = SanDoku.defaultClient(URI.create("http://localhost:8080"));
	Offset<Float> webRounding = Offset.offset(2E-3f); // Website rounds to 3 decimals. We get close to double that.
	Offset<Double> apiRounding = Offset.offset(1E-5); // API rounds to 5 decimals

	@Test
	public void bestFriendsParameters() throws Exception {
		byte[] beatmap = IOUtils.resourceToByteArray("/Fujijo Seitokai Shikkou-bu - Best FriendS -TV Size- (Flask) [Fycho's Insane].osu");

		assertThat(sanDoku.getDiff(0, 0, beatmap).diffCalcResult()).satisfies(result -> {
			assertThat(result.starRating()).isCloseTo(4.70888, apiRounding);
			assertThat(result.aimStrain()).isCloseTo(2.53559, apiRounding);
			assertThat(result.speedStrain()).isCloseTo(1.89119, apiRounding);
		});

		// Flashlight
		assertThat(sanDoku.getDiff(0, 1024, beatmap).diffCalcResult()).satisfies(result -> {
			assertThat(result.starRating()).isCloseTo(5.23327, apiRounding); // star rating is different!
			assertThat(result.aimStrain()).isCloseTo(2.53559, apiRounding);
			assertThat(result.speedStrain()).isCloseTo(1.89119, apiRounding);
		});

		assertThat(sanDoku.getDiff(0, 64, beatmap).diffCalcResult()).satisfies(result -> {
			assertThat(result.starRating()).isCloseTo(6.62106, apiRounding);
			assertThat(result.aimStrain()).isCloseTo(3.52118, apiRounding);
			assertThat(result.speedStrain()).isCloseTo(2.7438, apiRounding);
		});

		// now with HR DT
		assertThat(sanDoku.getDiff(0, 80, beatmap).diffCalcResult()).satisfies(result -> {
			assertThat(result.starRating()).isCloseTo(7.03281, apiRounding);
			assertThat(result.aimStrain()).isCloseTo(3.82381, apiRounding);
			assertThat(result.speedStrain()).isCloseTo(2.75015, apiRounding);
		});
	}

	@Test
	public void testBestFriendsRafis() throws Exception {
		// https://osu.ppy.sh/scores/osu/2111593239
		byte[] beatmap = IOUtils.resourceToByteArray("/Fujijo Seitokai Shikkou-bu - Best FriendS -TV Size- (Flask) [Fycho's Insane].osu");
		SanDokuResponse diff = sanDoku.getDiff(0, Mods.getMask(Mods.HardRock, Mods.DoubleTime), beatmap);

		OsuApiScore score = new OsuApiScore();
		score.setMaxCombo(404);
		score.setCount300(309);
		score.setCount100(4);
		score.setCount50(0);
		score.setCountMiss(0);
		score.setMods(Mods.getMask(Mods.Hidden, Mods.HardRock, Mods.DoubleTime));
		OsuScore standardScore = new OsuScore(score);
		assertThat(standardScore.getPP(diff.toBeatmap())).isCloseTo(552.222f, webRounding);
	}

	@Test
	public void testBestFriendsTemaZpro() throws Exception {
		// https://osu.ppy.sh/scores/osu/2111593239
		byte[] beatmap = IOUtils.resourceToByteArray("/Fujijo Seitokai Shikkou-bu - Best FriendS -TV Size- (Flask) [Fycho's Insane].osu");
		SanDokuResponse diff = sanDoku.getDiff(0, Mods.getMask(Mods.DoubleTime), beatmap);

		OsuApiScore score = new OsuApiScore();
		score.setMaxCombo(404);
		score.setCount300(313);
		score.setCount100(0);
		score.setCount50(0);
		score.setCountMiss(0);
		score.setMods(Mods.getMask(Mods.Hidden, Mods.Nightcore));
		OsuScore standardScore = new OsuScore(score);
		assertThat(standardScore.getPP(diff.toBeatmap())).isCloseTo(373.611f, webRounding);
	}

	@Test
	public void freedomDiveRafis() throws Exception {
		byte[] beatmap = IOUtils.resourceToByteArray("/xi - FREEDOM DiVE (Nakagawa-Kanon) [FOUR DIMENSIONS].osu");
		SanDokuResponse diff = sanDoku.getDiff(0, 0, beatmap);

		assertThat(diff.diffCalcResult()).satisfies(result -> {
			assertThat(result.starRating()).isCloseTo(7.52382, apiRounding);
			assertThat(result.aimStrain()).isCloseTo(3.45227, apiRounding);
			assertThat(result.speedStrain()).isCloseTo(3.77577, apiRounding);
		});

		OsuApiScore score = new OsuApiScore();
		score.setMaxCombo(2385);
		score.setCount300(1981);
		score.setCount100(2);
		score.setCount50(0);
		score.setCountMiss(0);
		score.setMods(0);
		OsuScore standardScore = new OsuScore(score);
		assertThat(standardScore.getPP(diff.toBeatmap())).isCloseTo(612.754f, webRounding);
	}
}

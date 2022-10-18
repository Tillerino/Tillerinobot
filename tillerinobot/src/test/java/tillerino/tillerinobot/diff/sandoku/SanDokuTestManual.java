package tillerino.tillerinobot.diff.sandoku;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.BadRequestException;

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
			// https://osu.ppy.sh/api/get_beatmaps?k=***&m=0&b=328472&mods=0
			assertThat(result.starRating()).isCloseTo(4.73447, apiRounding);
			assertThat(result.aim()).isCloseTo(2.53878, apiRounding);
			assertThat(result.speed()).isCloseTo(1.88141, apiRounding);
			assertThat(result.maxCombo()).isEqualTo(404);
		});

		// Flashlight
		assertThat(sanDoku.getDiff(0, 1024, beatmap).diffCalcResult()).satisfies(result -> {
			// https://osu.ppy.sh/api/get_beatmaps?k=***&m=0&b=328472&mods=1024
			assertThat(result.starRating()).isCloseTo(5.28149, apiRounding); // star rating is different!
			assertThat(result.aim()).isCloseTo(2.53878, apiRounding);
			assertThat(result.speed()).isCloseTo(1.88141, apiRounding);
			assertThat(result.maxCombo()).isEqualTo(404);
		});

		assertThat(sanDoku.getDiff(0, 64, beatmap).diffCalcResult()).satisfies(result -> {
			// https://osu.ppy.sh/api/get_beatmaps?k=***&m=0&b=328472&mods=64
			assertThat(result.starRating()).isCloseTo(6.65472, apiRounding);
			assertThat(result.aim()).isCloseTo(3.52504, apiRounding);
			assertThat(result.speed()).isCloseTo(2.72927, apiRounding);
			assertThat(result.maxCombo()).isEqualTo(404);
		});

		// now with HR DT
		assertThat(sanDoku.getDiff(0, 80, beatmap).diffCalcResult()).satisfies(result -> {
			// https://osu.ppy.sh/api/get_beatmaps?k=***&m=0&b=328472&mods=80
			assertThat(result.starRating()).isCloseTo(7.06681, apiRounding);
			assertThat(result.aim()).isCloseTo(3.82241, apiRounding);
			assertThat(result.speed()).isCloseTo(2.74129, apiRounding);
			assertThat(result.maxCombo()).isEqualTo(404);
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
		assertThat(standardScore.getPP(diff.toBeatmap())).isCloseTo(560.049f, webRounding);
	}

	@Test
	public void testBestFriendsTemaZpro() throws Exception {
		// https://osu.ppy.sh/scores/osu/3939515677
		byte[] beatmap = IOUtils.resourceToByteArray("/Fujijo Seitokai Shikkou-bu - Best FriendS -TV Size- (Flask) [Fycho's Insane].osu");
		SanDokuResponse diff = sanDoku.getDiff(0, Mods.getMask(Mods.DoubleTime), beatmap);

		OsuApiScore score = new OsuApiScore();
		score.setMaxCombo(404);
		score.setCount300(313);
		score.setCount100(0);
		score.setCount50(0);
		score.setCountMiss(0);
		score.setMods(Mods.getMask(Mods.Hidden, Mods.DoubleTime, Mods.Nightcore));
		OsuScore standardScore = new OsuScore(score);
		assertThat(standardScore.getPP(diff.toBeatmap())).isCloseTo(379.488f, webRounding);
	}

	@Test
	public void freedomDiveRafis() throws Exception {
		byte[] beatmap = IOUtils.resourceToByteArray("/xi - FREEDOM DiVE (Nakagawa-Kanon) [FOUR DIMENSIONS].osu");
		SanDokuResponse diff = sanDoku.getDiff(0, 0, beatmap);

		assertThat(diff.diffCalcResult()).satisfies(result -> {
			// https://osu.ppy.sh/api/get_beatmaps?k=***&m=0&b=129891&mods=0
			assertThat(result.starRating()).isCloseTo(7.58325, apiRounding);
			assertThat(result.aim()).isCloseTo(3.47299, apiRounding);
			assertThat(result.speed()).isCloseTo(3.77182, apiRounding);
			assertThat(result.maxCombo()).isEqualTo(2385);
		});

		// https://osu.ppy.sh/scores/osu/2355338302
		OsuApiScore score = new OsuApiScore();
		score.setMaxCombo(2385);
		score.setCount300(1981);
		score.setCount100(2);
		score.setCount50(0);
		score.setCountMiss(0);
		score.setMods(0);
		OsuScore standardScore = new OsuScore(score);
		assertThat(standardScore.getPP(diff.toBeatmap())).isCloseTo(626.636f, webRounding);
	}

	@Test
	public void emptyBeatmap() throws Exception {
		assertThatThrownBy(() -> sanDoku.getDiff(0, 0, new byte[0]))
			.isInstanceOfSatisfying(BadRequestException.class, e -> assertThat(SanDoku.unwrapError(e))
					.hasValueSatisfying(error -> assertThat(error)
							.hasFieldOrPropertyWithValue("title", "One or more validation errors occurred.")
							.hasFieldOrPropertyWithValue("errors", Map.of("beatmap", List.of("Empty input not valid")))));
	}

	@Test
	public void invalidModsDTHT() throws Exception {
		byte[] beatmap = IOUtils.resourceToByteArray("/xi - FREEDOM DiVE (Nakagawa-Kanon) [FOUR DIMENSIONS].osu");
		assertThatThrownBy(() -> sanDoku.getDiff(0, Mods.getMask(Mods.DoubleTime, Mods.HalfTime), beatmap))
				.isInstanceOfSatisfying(BadRequestException.class, e -> assertThat(SanDoku.unwrapError(e))
						.hasValueSatisfying(error -> assertThat(error)
								.hasFieldOrPropertyWithValue("title", "One or more validation errors occurred.")
								.hasFieldOrPropertyWithValue("errors", Map.of("mods", List.of("invalid mod combination: HT,DT")))));
	}

	@Test
	public void invalidModsEZHR() throws Exception {
		byte[] beatmap = IOUtils.resourceToByteArray("/xi - FREEDOM DiVE (Nakagawa-Kanon) [FOUR DIMENSIONS].osu");
		assertThatThrownBy(() -> sanDoku.getDiff(0, Mods.getMask(Mods.Easy, Mods.HardRock), beatmap))
				.isInstanceOfSatisfying(BadRequestException.class, e -> assertThat(SanDoku.unwrapError(e))
						.hasValueSatisfying(error -> assertThat(error)
								.hasFieldOrPropertyWithValue("title", "One or more validation errors occurred.")
								.hasFieldOrPropertyWithValue("errors", Map.of("mods", List.of("invalid mod combination: HR,EZ")))));
	}
}

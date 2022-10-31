package tillerino.tillerinobot.recommendations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Default;
import tillerino.tillerinobot.predicates.ExcludeMod;
import tillerino.tillerinobot.predicates.MapLength;
import tillerino.tillerinobot.predicates.NumericPropertyPredicate;
import tillerino.tillerinobot.predicates.StarDiff;

@RunWith(MockitoJUnitRunner.class)
public class RecommendationRequestParserTest {
	@Mock
	private BotBackend backend;

	@InjectMocks
	private RecommendationRequestParser recommendationRequestParser;

	private RecommendationRequest parse(String settings) throws Exception {
		OsuApiUser user = new OsuApiUser();
		user.setUserId(1);
		return recommendationRequestParser.parseSamplerSettings(user, settings, new Default());
	}

	@Test
	public void testModContradiction() throws Exception {
		when(backend.getDonator(anyInt())).thenReturn(1);
		assertThat(parse("dt")).hasFieldOrPropertyWithValue("requestedMods", 64L);
		assertThat(parse("-dt").predicates()).containsExactly(new ExcludeMod(Mods.DoubleTime));
		assertThatThrownBy(() -> parse("dt -dt")).isInstanceOfAny(UserException.class).hasMessageContaining("DT -DT");
	}

	@Test
	public void testModVsStar() throws Exception {
		when(backend.getDonator(anyInt())).thenReturn(1);
		assertThat(parse("dt")).hasFieldOrPropertyWithValue("requestedMods", 64L);
		assertThat(parse("STAR=5").predicates()).containsExactly(new NumericPropertyPredicate<>("STAR=5", new StarDiff(), 5, true, 5, true));
		assertThatThrownBy(() -> parse("dt STAR=5")).isInstanceOfAny(UserException.class).hasMessageContaining("DT STAR");
	}

	@Test
	public void testGamma5Dt() throws Exception {
		assertThat(parse("gamma5 dt"))
			.hasFieldOrPropertyWithValue("model", Model.GAMMA5)
			.hasFieldOrPropertyWithValue("requestedMods", 64L);
	}

	@Test
	public void testGamma6Dt() throws Exception {
		assertThat(parse("gamma7 dt"))
			.hasFieldOrPropertyWithValue("model", Model.GAMMA7)
			.hasFieldOrPropertyWithValue("requestedMods", 64L);
	}

	@Test
	public void testGamma5Len() throws Exception {
		when(backend.getDonator(anyInt())).thenReturn(1);
		RecommendationRequest request = parse("gamma5 LEN<=150");
		assertThat(request)
			.hasFieldOrPropertyWithValue("model", Model.GAMMA5);
		assertThat(request.predicates())
			.containsExactly(new NumericPropertyPredicate<>("LEN<=150", new MapLength(), Double.NEGATIVE_INFINITY, true, 150D, true));
	}

	@Test
	public void parseNap() throws Exception {
		RecommendationRequest request = parse("nap dt");
		assertThat(request)
			.hasFieldOrPropertyWithValue("model", Model.NAP)
			.hasFieldOrPropertyWithValue("requestedMods", 64L);
	}
}

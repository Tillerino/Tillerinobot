package tillerino.tillerinobot.recommendations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Default;
import tillerino.tillerinobot.predicates.ExcludeMod;
import tillerino.tillerinobot.predicates.NumericPropertyPredicate;
import tillerino.tillerinobot.predicates.StarDiff;

@RunWith(MockitoJUnitRunner.class)
public class RecommendationRequestParserTest {
	@Mock
	private BotBackend backend;

	@InjectMocks
	private RecommendationRequestParser recommendationRequestParser;

	private RecommendationRequest parse(String settings) throws Exception {
		return recommendationRequestParser.parseSamplerSettings(new OsuApiUser(), settings, new Default());
	}

	@Test
	public void testModContradiction() throws Exception {
		when(backend.getDonator(any())).thenReturn(1);
		assertThat(parse("dt")).hasFieldOrPropertyWithValue("requestedMods", 64L);
		assertThat(parse("-dt").getPredicates()).containsExactly(new ExcludeMod(Mods.DoubleTime));
		assertThatThrownBy(() -> parse("dt -dt")).isInstanceOfAny(UserException.class).hasMessageContaining("DT -DT");
	}

	@Test
	public void testModVsStar() throws Exception {
		when(backend.getDonator(any())).thenReturn(1);
		assertThat(parse("dt")).hasFieldOrPropertyWithValue("requestedMods", 64L);
		assertThat(parse("STAR=5").getPredicates()).containsExactly(new NumericPropertyPredicate<>("STAR=5", new StarDiff(), 5, true, 5, true));
		assertThatThrownBy(() -> parse("dt STAR=5")).isInstanceOfAny(UserException.class).hasMessageContaining("DT STAR");
	}
}

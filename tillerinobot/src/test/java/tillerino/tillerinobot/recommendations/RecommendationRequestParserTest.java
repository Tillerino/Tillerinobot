package tillerino.tillerinobot.recommendations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Default;
import tillerino.tillerinobot.predicates.ExcludeMod;
import tillerino.tillerinobot.predicates.MapLength;
import tillerino.tillerinobot.predicates.NumericPropertyPredicate;
import tillerino.tillerinobot.predicates.StarDiff;
import tillerino.tillerinobot.recommendations.RecommendationRequest.Shift;

public class RecommendationRequestParserTest {
    private BotBackend backend = mock(BotBackend.class);

    private RecommendationRequestParser recommendationRequestParser = new RecommendationRequestParser(backend);

    private RecommendationRequest parse(String settings) throws Exception {
        OsuApiUser user = new OsuApiUser();
        user.setUserId(1);
        return recommendationRequestParser.parseSamplerSettings(user, settings, new Default());
    }

    @Test
    public void defaultSettings() throws Exception {
        RecommendationRequest request = parse("");
        assertThat(request).returns(Shift.NONE, RecommendationRequest::difficultyShift);
    }

    @Test
    public void testModContradiction() throws Exception {
        when(backend.getDonator(anyInt())).thenReturn(1);
        assertThat(parse("dt")).hasFieldOrPropertyWithValue("requestedMods", 64L);
        assertThat(parse("-dt").predicates()).containsExactly(new ExcludeMod(Mods.DoubleTime));
        assertThatThrownBy(() -> parse("dt -dt"))
                .isInstanceOfAny(UserException.class)
                .hasMessageContaining("DT -DT");
    }

    @Test
    public void testModVsStar() throws Exception {
        when(backend.getDonator(anyInt())).thenReturn(1);
        assertThat(parse("dt")).hasFieldOrPropertyWithValue("requestedMods", 64L);
        assertThat(parse("STAR=5").predicates())
                .containsExactly(new NumericPropertyPredicate<>("STAR=5", new StarDiff(), 5, true, 5, true));
        assertThatThrownBy(() -> parse("dt STAR=5"))
                .isInstanceOfAny(UserException.class)
                .hasMessageContaining("DT STAR");
    }

    @Test
    public void testGamma10Dt() throws Exception {
        assertThat(parse("gamma10 dt"))
                .hasFieldOrPropertyWithValue("model", Model.GAMMA10)
                .hasFieldOrPropertyWithValue("requestedMods", 64L);
    }

    @Test
    public void testGamma8Dt() throws Exception {
        assertThat(parse("gamma8 dt"))
                .hasFieldOrPropertyWithValue("model", Model.GAMMA8)
                .hasFieldOrPropertyWithValue("requestedMods", 64L);
    }

    @Test
    public void testGamma10Len() throws Exception {
        when(backend.getDonator(anyInt())).thenReturn(1);
        RecommendationRequest request = parse("gamma10 LEN<=150");
        assertThat(request).hasFieldOrPropertyWithValue("model", Model.GAMMA10);
        assertThat(request.predicates())
                .containsExactly(new NumericPropertyPredicate<>(
                        "LEN<=150", new MapLength(), Double.NEGATIVE_INFINITY, true, 150D, true));
    }

    @Test
    public void parseNap() throws Exception {
        RecommendationRequest request = parse("nap dt");
        assertThat(request)
                .hasFieldOrPropertyWithValue("model", Model.NAP)
                .hasFieldOrPropertyWithValue("requestedMods", 64L);
    }

    @Test
    public void testSucc() throws Exception {
        assertThatThrownBy(() -> parse("succ")).isInstanceOf(UserException.class);

        doReturn(1).when(backend).getDonator(1);
        assertThat(parse("succ")).returns(Shift.SUCC, RecommendationRequest::difficultyShift);
        assertThat(parse("succer")).returns(Shift.SUCCER, RecommendationRequest::difficultyShift);
        // typo; long word
        assertThat(parse("sucerberg")).returns(Shift.SUCCERBERG, RecommendationRequest::difficultyShift);
    }
}

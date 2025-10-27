package tillerino.tillerinobot.lang;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.LiveActivity;
import tillerino.tillerinobot.*;
import tillerino.tillerinobot.handlers.RecommendHandler;
import tillerino.tillerinobot.recommendations.RecommendationRequestParser;
import tillerino.tillerinobot.recommendations.RecommendationsManager;
import tillerino.tillerinobot.recommendations.Recommender;

public class TsundereTest {

    @Test
    public void testInvalidChoice() throws Exception {
        // spy on a fresh tsundere object
        TsundereEnglish tsundere = spy(new TsundereEnglish());

        // mock backend and create RecommendationsManager and RecommendHandler based on mocked backend
        BotBackend backend = mock(BotBackend.class);
        BeatmapsLoader beatmapsLoader = mock(BeatmapsLoader.class);
        TestBase.mockApiBeatmaps(beatmapsLoader);
        RecommendHandler handler = new RecommendHandler(
                new RecommendationsManager(
                        null,
                        new RecommendationRequestParser(backend),
                        beatmapsLoader,
                        mock(Recommender.class),
                        null,
                        null,
                        null,
                        null),
                mock(LiveActivity.class));

        // make a bullshit call to the handler four times
        for (int i = 0; i < 4; i++) {
            try {
                handler.handle("r bullshit", mock(OsuApiUser.class), null, tsundere);
                // we should not get this far because we're expecting an exception
                fail("there should be an exception");
            } catch (UserException e) {
                // good, we're expecting this
            }
        }

        // invalid choice should have been called all four times
        verify(tsundere, times(4)).invalidChoice(anyString(), anyString());
        // three of those times, unknownRecommendationParameter should have been called as well
        verify(tsundere, times(3)).unknownRecommendationParameter();
    }
}

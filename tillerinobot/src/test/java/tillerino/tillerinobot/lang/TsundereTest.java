package tillerino.tillerinobot.lang;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.LiveActivity;

import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.TestBackend;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.handlers.RecommendHandler;
import tillerino.tillerinobot.recommendations.RecommendationRequestParser;
import tillerino.tillerinobot.recommendations.RecommendationsManager;

public class TsundereTest {

	@Test
	public void testInvalidChoice() throws Exception {
		// spy on a fresh tsundere object
		TsundereEnglish tsundere = spy(new TsundereEnglish());

		// mock backend and create RecommendationsManager and RecommendHandler based on mocked backend
		BotBackend backend = mock(BotBackend.class);
		RecommendHandler handler = new RecommendHandler(new RecommendationsManager(backend, null, null,
				new RecommendationRequestParser(backend), new TestBackend.TestBeatmapsLoader()),
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

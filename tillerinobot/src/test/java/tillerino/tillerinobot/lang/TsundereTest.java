package tillerino.tillerinobot.lang;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.UserDataManager.UserData;
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
		RecommendHandler handler = new RecommendHandler(
				new RecommendationsManager(backend, null, null, new RecommendationRequestParser(backend)));

		// mock a user data object and make it return the tsundere object that we're spying on
		UserData userData = mock(UserData.class);
		when(userData.getLanguage()).thenReturn(tsundere);
		
		// make a bullshit call to the handler four times
		for (int i = 0; i < 4; i++) {
			try {
				handler.handle("r bullshit", mock(OsuApiUser.class), userData);
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

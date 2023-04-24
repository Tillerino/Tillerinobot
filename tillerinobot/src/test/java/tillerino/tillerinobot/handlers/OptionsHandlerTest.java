package tillerino.tillerinobot.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.function.FailableFunction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Message;

import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Default;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.lang.LanguageIdentifier;
import tillerino.tillerinobot.lang.Vietnamese;
import tillerino.tillerinobot.recommendations.RecommendationRequestParser;

@RunWith(MockitoJUnitRunner.class)
public class OptionsHandlerTest {
	enum E {
		AA,
		BB
	}

	@Mock
	UserData userData;

	@Test
	public void test() throws Exception {
		assertEquals(E.AA, OptionsHandler.find(E.values(), E::name, "a"));
		assertEquals(E.AA, OptionsHandler.find(E.values(), E::name, "ac"));
		assertEquals(E.BB, OptionsHandler.find(E.values(), E::name, "b"));
		assertEquals(E.BB, OptionsHandler.find(E.values(), E::name, "cb"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testEmpty() throws Exception {
		// this will match nothing
		OptionsHandler.find(E.values(), E::name, "");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCenter() throws Exception {
		// this will match both AA and BB
		OptionsHandler.find(E.values(), E::name, "ab");
	}
	
	@Test
	public void testSetLanguage() throws Exception {
		OptionsHandler handler = new OptionsHandler(null);

		handler.handle("set languge tsundre", null, userData, new Default());
		
		verify(userData).setLanguage(LanguageIdentifier.Tsundere);
	}

	@Test
	public void testGetTsundere() throws Exception {
		when(userData.getLanguageIdentifier()).thenReturn(LanguageIdentifier.Tsundere);

		assertThat(new OptionsHandler(null).handle("get language", null, userData, new Default()))
			.isEqualTo(new Message("Language: Tsundere"));
	}

	@Test
	public void testSetVietnamese() throws Exception {
		doAnswer(invocationOnMock -> {
			FailableFunction<Language, GameChatResponse, RuntimeException> func = invocationOnMock.getArgument(0);
			return func.apply(new Vietnamese());
		}).when(userData).usingLanguage(any(FailableFunction.class));

		GameChatResponse response = new OptionsHandler(null).handle("set languge Tiếng Việt", null, userData, new Default());

		verify(userData).setLanguage(LanguageIdentifier.Vietnamese);
		assertThat(response).isEqualTo(new Vietnamese().optionalCommentOnLanguage(null));
	}

	@Test
	public void testGetVietnamese() throws Exception {
		when(userData.getLanguageIdentifier()).thenReturn(LanguageIdentifier.Vietnamese);

		assertThat(new OptionsHandler(null).handle("get language", null, userData, new Default()))
			.isEqualTo(new Message("Language: Tiếng Việt"));
	}

	@Test
	public void testSetUnknownLanguage() throws Exception {
		OptionsHandler handler = new OptionsHandler(null);
		
		assertThatThrownBy(() -> handler.handle("set language defflt", null, userData, new Default()))
			.isInstanceOf(UserException.class)
			.hasMessageContaining("Tiếng Việt");
	}

    @Test
    public void testDefaultSettings() throws Exception {
      OptionsHandler handler = new OptionsHandler(new RecommendationRequestParser(mock(BotBackend.class)));

      handler.handle("set default hd hr", null, userData, new Default());

      verify(userData).setDefaultRecommendationOptions("hd hr");
    }

    @Test
    public void testClearDefaultSettings() throws Exception {
      OptionsHandler handler = new OptionsHandler(null);

      handler.handle("set default", null, userData, new Default());

      verify(userData).setDefaultRecommendationOptions(null);
    }

    @Test(expected = UserException.class)
    public void testInvalidDefaultSettings() throws Exception {
      OptionsHandler handler = new OptionsHandler(new RecommendationRequestParser(mock(BotBackend.class)));

      OsuApiUser user = new OsuApiUser();
      user.setUserId(1);
      handler.handle("set default invalid", user , userData, new Default());
    }

    @Test
    public void allLanguageNamesNeedToBeParsable() throws Exception {
      for (LanguageIdentifier language : LanguageIdentifier.values()) {
        assertThat(OptionsHandler.find(LanguageIdentifier.values(), i -> i.token, language.token))
          .isNotNull();
      }
    }
}

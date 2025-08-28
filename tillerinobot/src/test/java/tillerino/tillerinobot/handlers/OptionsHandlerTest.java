package tillerino.tillerinobot.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;

import org.apache.commons.lang3.function.FailableFunction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Message;

import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.UserDataManager;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Default;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.lang.LanguageIdentifier;
import tillerino.tillerinobot.lang.Vietnamese;
import tillerino.tillerinobot.recommendations.RecommendationRequestParser;
import tillerino.tillerinobot.recommendations.RecommendationsManager;

public class OptionsHandlerTest {
	enum E {
		AA,
		BB
	}

	private void mockUserDataUsingLanguage(LanguageIdentifier langIdent) {
		doAnswer(invocationOnMock -> {
			FailableFunction<Language, GameChatResponse, RuntimeException> func = invocationOnMock.getArgument(0);
			return func.apply(langIdent.cls.getDeclaredConstructor().newInstance());
		}).when(userData).usingLanguage(any(FailableFunction.class));
	}

	UserData userData = mock(UserData.class);

	OptionsHandler handler = new OptionsHandler(new RecommendationRequestParser(mock(BotBackend.class)), mock(
			UserDataManager.class), mock(RecommendationsManager.class));

	@BeforeEach
	public void setup() throws Exception {
		// Make the mock return the last set value
		doAnswer(invocation -> {
			boolean value = invocation.getArgument(0);
			when(userData.isV2()).thenReturn(value);
			return null;
		}).when(userData).setV2(any(boolean.class));
	}

	@Test
	public void test() throws Exception {
		assertEquals(E.AA, OptionsHandler.find(E.values(), E::name, "a"));
		assertEquals(E.AA, OptionsHandler.find(E.values(), E::name, "ac"));
		assertEquals(E.BB, OptionsHandler.find(E.values(), E::name, "b"));
		assertEquals(E.BB, OptionsHandler.find(E.values(), E::name, "cb"));
	}
	
	@Test
	public void testEmpty() throws Exception {
		// this will match nothing
		assertThrows(IllegalArgumentException.class, () -> OptionsHandler.find(E.values(), E::name, ""));
	}
	
	@Test
	public void testCenter() throws Exception {
		// this will match both AA and BB
		assertThrows(IllegalArgumentException.class, () -> OptionsHandler.find(E.values(), E::name, "ab"));
	}
	
	@Test
	public void testSetLanguage() throws Exception {
		mockUserDataUsingLanguage(LanguageIdentifier.Tsundere);

		handler.handle("set languge tsundre", null, userData, new Default());
		
		verify(userData).setLanguage(LanguageIdentifier.Tsundere);
	}

	@Test
	public void testGetTsundere() throws Exception {
		when(userData.getLanguageIdentifier()).thenReturn(LanguageIdentifier.Tsundere);

		assertThat(handler.handle("get language", null, userData, new Default()))
			.isEqualTo(new Message("Language: Tsundere"));
	}

	@Test
	public void testSetVietnamese() throws Exception {
		mockUserDataUsingLanguage(LanguageIdentifier.Vietnamese);

		GameChatResponse response = handler.handle("set languge Tiếng Việt", null, userData, new Default());

		verify(userData).setLanguage(LanguageIdentifier.Vietnamese);
		assertThat(response).isEqualTo(new Vietnamese().optionalCommentOnLanguage(null));
	}

	@Test
	public void testGetVietnamese() throws Exception {
		when(userData.getLanguageIdentifier()).thenReturn(LanguageIdentifier.Vietnamese);

		assertThat(handler.handle("get language", null, userData, new Default()))
			.isEqualTo(new Message("Language: Tiếng Việt"));
	}

	@Test
	public void testSetUnknownLanguage() throws Exception {
		assertThatThrownBy(() -> handler.handle("set language defflt", null, userData, new Default()))
			.isInstanceOf(UserException.class)
			.hasMessageContaining("Tiếng Việt");
	}

    @Test
    public void testDefaultSettings() throws Exception {

      handler.handle("set default hd hr", null, userData, new Default());

      verify(userData).setDefaultRecommendationOptions("hd hr");
    }

    @Test
    public void testClearDefaultSettings() throws Exception {
      handler.handle("set default", null, userData, new Default());

      verify(userData).setDefaultRecommendationOptions(null);
    }

    @Test
    public void testInvalidDefaultSettings() throws Exception {
      OsuApiUser user = new OsuApiUser();
      user.setUserId(1);
      assertThrows(UserException.class, () -> handler.handle("set default invalid", user , userData, new Default()));
    }

    @Test
    public void allLanguageNamesNeedToBeParsable() throws Exception {
      for (LanguageIdentifier language : LanguageIdentifier.values()) {
        assertThat(OptionsHandler.find(LanguageIdentifier.values(), i -> i.token, language.token))
          .isNotNull();
      }
    }

    @Test
    public void testSetV2On() throws Exception {
      GameChatResponse response = handler.handle("set v2 on", null, userData, new Default());
      
      verify(userData).setV2(true);
      assertThat(response).isEqualTo(new Message("v2 API: ON"));
    }

    @Test
    public void testSetV2Off() throws Exception {
      GameChatResponse response = handler.handle("set v2 false", null, userData, new Default());
      
      verify(userData).setV2(false);
      assertThat(response).isEqualTo(new Message("v2 API: OFF"));
    }

    @Test
    public void testGetV2On() throws Exception {
      when(userData.isV2()).thenReturn(true);
      
      assertThat(handler.handle("get v2", null, userData, new Default()))
        .isEqualTo(new Message("v2 API: ON"));
    }

    @Test
    public void testGetV2Off() throws Exception {
      when(userData.isV2()).thenReturn(false);
      
      assertThat(handler.handle("get v2", null, userData, new Default()))
        .isEqualTo(new Message("v2 API: OFF"));
    }

    @Test
    public void testSetV2InvalidValue() throws Exception {
      assertThatThrownBy(() -> handler.handle("set v2 maybe", null, userData, new Default()))
        .isInstanceOf(UserException.class)
        .hasMessageContaining("on|true|yes|1|off|false|no|0");
    }
}

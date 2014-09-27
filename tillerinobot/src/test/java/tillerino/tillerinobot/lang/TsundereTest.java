package tillerino.tillerinobot.lang;

import org.junit.Test;

import tillerino.tillerinobot.IRCBot.IRCBotUser;
import static org.mockito.Mockito.*;

public class TsundereTest {
	@Test
	public void testNRecommendations() {
		Tsundere tsundere = new Tsundere();
		
		IRCBotUser user = mock(IRCBotUser.class);
		
		for(int i = 0; i < Tsundere.COMMENT_ON_R_INTERVAL * 5; i++) {
			tsundere.optionalCommentOnRecommendation(user, null, null);
		}
		
		verify(user, times(5)).message(anyString());
	}
}

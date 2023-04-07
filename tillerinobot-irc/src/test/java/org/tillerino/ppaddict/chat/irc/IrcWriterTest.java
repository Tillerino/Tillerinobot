package org.tillerino.ppaddict.chat.irc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.tillerino.ppaddict.util.Result.err;

import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.UserChannelDao;
import org.pircbotx.output.OutputUser;
import org.tillerino.ppaddict.chat.GameChatClientMetrics;
import org.tillerino.ppaddict.chat.GameChatWriter;
import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.chat.irc.BotRunnerImpl.CloseableBot;

@RunWith(MockitoJUnitRunner.class)
public class IrcWriterTest {
	@Mock
	LiveActivity liveActivity;

	@Mock
	ExecutorService exec;

	@Mock
	Pinger pinger;

	@Spy
	GameChatClientMetrics botInfo = new GameChatClientMetrics();

	@InjectMocks
	IrcWriter writer;

	@Mock
	CloseableBot bot;

	@Mock
	UserChannelDao<User, Channel> userChannelDao;

	@Mock
	User pircBotXuser;

	@Mock
	OutputUser outputUser;

	@Before
	public void before() {
		writer.setBot(bot);
		when(bot.getUserChannelDao()).thenReturn(userChannelDao);
		when(userChannelDao.getUser("user")).thenReturn(pircBotXuser);
		when(pircBotXuser.send()).thenReturn(outputUser);
		when(bot.isConnected()).thenReturn(true);
	}

	@Test
	public void testMessage() throws Exception {
		writer.message("abc", "user");
		verify(outputUser).message("abc");
	}

	@Test
	public void testAction() throws Exception {
		writer.action("abc", "user");
		verify(outputUser).action("abc");
	}

	@Test
	public void botDisconnects() throws Exception {
		// bot disconnects at a bad timing
		doThrow(new RuntimeException("Not connected to server")).when(outputUser).message(anyString());
		assertThat(writer.message("abc", "user")).isEqualTo(err(new GameChatWriter.Error.Retry(0)));
	}
}

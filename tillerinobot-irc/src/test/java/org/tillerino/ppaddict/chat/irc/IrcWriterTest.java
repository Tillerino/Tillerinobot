package org.tillerino.ppaddict.chat.irc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;

import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.UserChannelDao;
import org.pircbotx.output.OutputUser;
import org.tillerino.ppaddict.chat.GameChatMetrics;
import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.chat.PrivateMessage;
import org.tillerino.ppaddict.chat.irc.BotRunnerImpl.CloseableBot;
import org.tillerino.ppaddict.util.RetryableException;

@RunWith(MockitoJUnitRunner.class)
public class IrcWriterTest {
	@Mock
    LiveActivity liveActivity;

	@Mock
	ExecutorService exec;

	@Mock
	Pinger pinger;

	@Mock
	GameChatMetrics botInfo;

	@InjectMocks
	IrcWriter queue;

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
		queue.setBot(bot);
		when(bot.getUserChannelDao()).thenReturn(userChannelDao);
		when(userChannelDao.getUser("user")).thenReturn(pircBotXuser);
		when(pircBotXuser.send()).thenReturn(outputUser);
		when(bot.isConnected()).thenReturn(true);
	}

	@Test
	public void testMessage() throws Exception {
		queue.message("abc", new PrivateMessage(6789, "user", 12345, "hello"));
		verify(outputUser).message("abc");
	}

	@Test
	public void testAction() throws Exception {
		queue.action("abc", new PrivateMessage(6789, "user", 12345, "hello"));
		verify(outputUser).action("abc");
	}

	@Test
	public void botDisconnects() throws Exception {
		// bot disconnects at a bad timing
		doThrow(new RuntimeException("Not connected to server")).when(outputUser).message(anyString());
		assertThatThrownBy(() -> queue.message("abc", new PrivateMessage(6789, "user", 12345, "hello")))
			.isInstanceOf(RetryableException.class);
	}
}

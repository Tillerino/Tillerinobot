package tillerino.tillerinobot;

import java.nio.charset.Charset;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.PircBotX;

@Slf4j
@Singleton
public class BotRunnerImpl implements BotRunner {
	PircBotX bot = null;

	@Inject
	public BotRunnerImpl(Provider<IRCBot> tillerinoBot,
			@Named("tillerinobot.irc.server") String server,
			@Named("tillerinobot.irc.port") int port,
			@Named("tillerinobot.irc.nickname") String nickname,
			@Named("tillerinobot.irc.password") String password,
			@Named("tillerinobot.irc.autojoin") String autojoinChannel) {
		super();
		this.tillerinoBot = tillerinoBot;
		this.server = server;
		this.port = port;
		this.nickname = nickname;
		this.password = password;
		this.autojoinChannel = autojoinChannel;
	}

	private final Provider<IRCBot> tillerinoBot;

	private final String server;
	private final int port;
	private final String nickname;
	private final String password;
	private final String autojoinChannel;

	@Override
	@CheckForNull
	public PircBotX getBot() {
		return bot;
	}

	private boolean reconnect = true;
	private int reconnectTimeout = 10000;

	@Override
	public void run() {
		for (; reconnect;) {
			@SuppressWarnings("unchecked")
			Builder<PircBotX> configurationBuilder = new Configuration.Builder<PircBotX>()
					.setServer(server, port).setMessageDelay(1000)
					.setName(nickname).addListener(tillerinoBot.get())
					.setEncoding(Charset.forName("UTF-8"))
					.setAutoReconnect(false);
			if (password != null) {
				configurationBuilder.setServerPassword(password);
			}
			if (autojoinChannel != null) {
				configurationBuilder.addAutoJoinChannel(autojoinChannel);
			}
			try {
				try {
					(bot = new PircBotX(configurationBuilder.buildConfiguration())).startBot();
				} finally {
					bot = null;
				}
			} catch (Exception e) {
				log.error("exception running IRC bot", e);
			} finally {
				try {
					Thread.sleep(reconnectTimeout);
				} catch (InterruptedException e1) {
					return;
				}
			}
		}
	}
}
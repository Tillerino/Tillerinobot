package tillerino.tillerinobot;

import java.nio.charset.Charset;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.PircBotX;

@Singleton
public class BotRunnerImpl implements BotRunner {
	PircBotX bot = null;

	@Inject
	public BotRunnerImpl(IRCBot tillerinoBot,
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

	private final IRCBot tillerinoBot;

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
		@SuppressWarnings("unchecked")
		Builder<PircBotX> configurationBuilder = new Configuration.Builder<PircBotX>()
				.setServer(server, port).setMessageDelay(1000)
				.setName(nickname).addListener(tillerinoBot)
				.setEncoding(Charset.forName("UTF-8")).setAutoReconnect(false);
		if (password != null) {
			configurationBuilder.setServerPassword(password);
		}
		if (autojoinChannel != null) {
			configurationBuilder.addAutoJoinChannel(autojoinChannel);
		}
		for (; reconnect;) {
			try {
				try {
					tillerinoBot.pinger.quit.set(false);
					(bot = new PircBotX(configurationBuilder.buildConfiguration())).startBot();
				} finally {
					bot = null;
				}
			} catch (Exception e) {
				try {
					Thread.sleep(reconnectTimeout);
				} catch (InterruptedException e1) {
					return;
				}
			}
		}
	}
}
package tillerino.tillerinobot;

import java.io.IOException;
import java.net.Socket;
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
public class BotRunnerImpl implements BotRunner, TidyObject {
	public static class CloseableBot extends PircBotX {
		public CloseableBot(Configuration<? extends PircBotX> configuration) {
			super(configuration);
		}
		
		@Override
		public Socket getSocket() {
			return super.getSocket();
		}
	}

	volatile CloseableBot bot = null;

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
	public CloseableBot getBot() {
		return bot;
	}

	private volatile boolean reconnect = true;
	private int reconnectTimeout = 10000;

	IRCBot listener;
	
	@Override
	public void run() {
		for (; ;) {
			if(!reconnect) {
				break;
			}
			try {
				listener = tillerinoBot.get();
				try {
					@SuppressWarnings("unchecked")
					Builder<PircBotX> configurationBuilder = new Configuration.Builder<PircBotX>()
							.setServer(server, port).setMessageDelay(1000)
							.setName(nickname).addListener(listener)
							.setEncoding(Charset.forName("UTF-8"))
							.setAutoReconnect(false);
					if (password != null && !password.isEmpty()) {
						configurationBuilder.setServerPassword(password);
					}
					if (autojoinChannel != null && !autojoinChannel.isEmpty()) {
						configurationBuilder.addAutoJoinChannel(autojoinChannel);
					}
					if(reconnect) {
						(bot = new CloseableBot(configurationBuilder.buildConfiguration())).startBot();
					}
				} finally {
					bot = null;
					listener.tidyUp(false);
					listener = null;
				}
			} catch (Exception e) {
				log.error("exception running IRC bot", e);
			} finally {
				try {
					if(reconnect) {
						Thread.sleep(reconnectTimeout);
					}
				} catch (InterruptedException e1) {
					return;
				}
			}
		}
	}
	
	@Override
	public void tidyUp(boolean fromShutdownHook) {
		synchronized (this) {
			reconnect = false;
			if(bot != null && bot.isConnected()) {
				bot.sendIRC().quitServer();
				try {
					bot.getSocket().close();
				} catch (IOException e) {
					log.error("error closing socket", e);
				}
			}
			if(listener != null) {
				listener.tidyUp(fromShutdownHook);
			}
		}
	}
}
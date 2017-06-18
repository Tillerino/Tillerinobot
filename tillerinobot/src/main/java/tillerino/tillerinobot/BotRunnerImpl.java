package tillerino.tillerinobot;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.InputParser;
import org.pircbotx.User;
import org.pircbotx.UserChannelDao;
import org.pircbotx.Configuration.BotFactory;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.QuitEvent;
import org.pircbotx.hooks.managers.ThreadedListenerManager;
import org.pircbotx.snapshot.UserChannelDaoSnapshot;
import org.pircbotx.snapshot.UserSnapshot;
import org.pircbotx.PircBotX;

import com.google.common.collect.Lists;

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
	
	static class CustomBotFactory extends BotFactory {
		@Override
		public InputParser createInputParser(PircBotX bot) {
			return new CustomInputParser(bot);
		}
		
		@Override
		public UserChannelDao<User, Channel> createUserChannelDao(PircBotX bot) {
			return new CustomUserChannelDao(bot, this);
		}
	}
	
	/**
	 * This class is just there to adjust visibility
	 */
	static class CustomUserChannelDao extends UserChannelDao<User, Channel> {
		public CustomUserChannelDao(PircBotX bot, BotFactory botFactory) {
			super(bot, botFactory);
		}
		
		@Override
		protected void removeChannel(Channel channel) {
			super.removeChannel(channel);
		}
		
		@Override
		protected void removeUserFromChannel(User user, Channel channel) {
			super.removeUserFromChannel(user, channel);
		}
		
		@Override
		protected void removeUser(User user) {
			super.removeUser(user);
		}
	}

	/**
	 * This class is here to avoid the immense garbage and CPU time that is
	 * caused by {@link UserChannelDao#createSnapshot()}.
	 */
	static class CustomInputParser extends InputParser {
		public CustomInputParser(PircBotX bot) {
			super(bot);
		}
		
		final UserChannelDaoSnapshot userDaoSnapshot = new UserChannelDaoSnapshot(null, null, null, null, null, null, null);

		@Override
		public void processCommand(String target, String sourceNick, String sourceLogin, String sourceHostname, String command, String line, List<String> parsedLine) throws IOException {
			if (command.equals("PART")) {
				Channel channel = (target.length() != 0 && configuration.getChannelPrefixes().indexOf(target.charAt(0)) >= 0) ? bot.getUserChannelDao().getChannel(target) : null;
				if(channel == null)
					return;
				
				User source = bot.getUserChannelDao().getUser(sourceNick);

				if (sourceNick.equals(bot.getNick())) {
					((CustomUserChannelDao) bot.getUserChannelDao()).removeChannel(channel);
				} else {
					((CustomUserChannelDao) bot.getUserChannelDao()).removeUserFromChannel(source, channel);
				}
				
				final UserSnapshot sourceSnapshot = source.createSnapshot();
				configuration.getListenerManager().dispatchEvent(new PartEvent<PircBotX>(bot, userDaoSnapshot, channel.createSnapshot(), sourceSnapshot, ""));
			} else if (command.equals("QUIT")) {
				User source = bot.getUserChannelDao().getUser(sourceNick);
				
				if (!sourceNick.equals(bot.getNick())) {
					((CustomUserChannelDao) bot.getUserChannelDao()).removeUser(source);
				}
				
				final UserSnapshot sourceSnapshot = source.createSnapshot();
				configuration.getListenerManager().dispatchEvent(new QuitEvent<PircBotX>(bot, userDaoSnapshot, sourceSnapshot, ""));
			} else {
				super.processCommand(target, sourceNick, sourceLogin, sourceHostname, command, line, parsedLine);
			}
		}
	}
	
	static class CustomThreadedListenerManager extends ThreadedListenerManager<PircBotX> {
		public CustomThreadedListenerManager() {
			super();
		}

		public CustomThreadedListenerManager(ExecutorService pool) {
			super(pool);
		}
		
		@Override
		public void shutdown(PircBotX bot) {
			List<ManagedFutureTask> remainingTasks;
			synchronized (runningListeners) {
				remainingTasks = Lists.newArrayList(runningListeners.get(bot));
			}

			for (ManagedFutureTask curFuture : remainingTasks) {
				try {
					curFuture.cancel(true);
				} catch (Exception e) {
					log.error("exception cancelling future", e);
				}
			}
		}
	}
	
	volatile CloseableBot bot = null;

	@Inject
	public BotRunnerImpl(Provider<IRCBot> tillerinoBot,
			@Named("tillerinobot.irc.server") String server,
			@Named("tillerinobot.irc.port") int port,
			@Named("tillerinobot.irc.nickname") String nickname,
			@Named("tillerinobot.irc.password") String password,
			@Named("tillerinobot.irc.autojoin") String autojoinChannel,
			@Named("tillerinobot.git.commit.id.abbrev") String commit,
			@Named("tillerinobot.git.commit.message.short") String commitMessage) {
		super();
		this.tillerinoBot = tillerinoBot;
		this.server = server.split(",");
		this.port = port;
		this.nickname = nickname;
		this.password = password;
		this.autojoinChannel = autojoinChannel;
		this.commit = commit;
		this.commitMessage = commitMessage;
	}

	private final Provider<IRCBot> tillerinoBot;

	private final String[] server;
	private final int port;
	private final String nickname;
	private final String password;
	private final String autojoinChannel;
	private final String commit;
	private final String commitMessage;

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
		log.info("Starting Tillerinobot {}: {}", commit, commitMessage);
		for (int i = 0; ; i++) {
			if(!reconnect) {
				break;
			}
			try {
				listener = tillerinoBot.get();
				try {
					final CustomThreadedListenerManager listenerManager = new CustomThreadedListenerManager();
					listenerManager.addListener(listener);
					
					Builder<PircBotX> configurationBuilder = new Configuration.Builder<PircBotX>()
							.setServer(server[i % server.length], port).setMessageDelay(250)
							.setListenerManager(listenerManager)
							.setName(nickname)
							.setEncoding(Charset.forName("UTF-8"))
							.setAutoReconnect(false)
							.setBotFactory(new CustomBotFactory());
					if (password != null && !password.isEmpty()) {
						configurationBuilder.setServerPassword(password);
					}
					if (autojoinChannel != null && !autojoinChannel.isEmpty()) {
						configurationBuilder.addAutoJoinChannel(autojoinChannel);
					}
					if(reconnect) {
						log.info("Connecting");
						(bot = new CloseableBot(configurationBuilder.buildConfiguration())).startBot();
						log.info("Bot stopped");
					}
				} finally {
					bot = null;
					listener = null;
				}
			} catch (Exception e) {
				log.error("exception running IRC bot", e);
			} finally {
				try {
					if(reconnect) {
						log.debug("Sleeping before reconnect");
						Thread.sleep(reconnectTimeout);
					}
				} catch (InterruptedException e1) {
					return;
				}
			}
		}
		log.info("Exiting");
	}
	
	@Override
	public void tidyUp(boolean fromShutdownHook) {
		log.info("tidyUp({})", fromShutdownHook);
		
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
		}
	}
}
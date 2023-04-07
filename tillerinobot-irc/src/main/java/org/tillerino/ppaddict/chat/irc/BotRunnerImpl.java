package org.tillerino.ppaddict.chat.irc;

import static org.tillerino.ppaddict.util.Result.ok;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;
import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.Configuration.BotFactory;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.exception.IrcException;
import org.pircbotx.InputParser;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.UserChannelDao;
import org.pircbotx.Utils;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.QuitEvent;
import org.pircbotx.hooks.managers.ThreadedListenerManager;
import org.pircbotx.snapshot.UserChannelDaoSnapshot;
import org.pircbotx.snapshot.UserSnapshot;
import org.tillerino.ppaddict.chat.GameChatClient;
import org.tillerino.ppaddict.chat.GameChatClientMetrics;
import org.tillerino.ppaddict.chat.GameChatEventConsumer;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.util.Result;

import com.google.common.util.concurrent.MoreExecutors;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class BotRunnerImpl implements GameChatClient, Runnable {
	public static final int DEFAULT_MESSAGE_DELAY = 250;
	@SuppressFBWarnings(value = "MS", justification = "We're modifying this in tests")
	public static int MESSAGE_DELAY = DEFAULT_MESSAGE_DELAY;

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
	// warns about copying messages below, but they are copied for visibility
	@SuppressWarnings("squid:S1185")
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
		public void handleLine(String line) throws IOException, IrcException {
			List<String> parsedLine = Utils.tokenizeLine(line);
			if (parsedLine.isEmpty()) {
				// bancho started sending empty lines on 2020-08-27
				return;
			}
			super.handleLine(line);
		}

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
			/*
			 * run with a single thread which will only do some light
			 * preprocessing and then push everything into a queue.
			 * This thread is allowed to time out.
			 */
			super(MoreExecutors.newDirectExecutorService());
		}

		@Override
		public void shutdown(PircBotX bot) {
			pool.shutdownNow();
		}
	}
	
	volatile CloseableBot bot = null;

	@SuppressFBWarnings("EI_EXPOSE_REP2")
	@Inject
	public BotRunnerImpl(@Named("tillerinobot.irc.server") String server,
			@Named("tillerinobot.irc.port") int port,
			@Named("tillerinobot.irc.nickname") String nickname,
			@Named("tillerinobot.irc.password") String password,
			@Named("tillerinobot.irc.autojoin") String autojoinChannel,
			@Named("tillerinobot.ignore") boolean silent,
			@Named("messagePreprocessor") GameChatEventConsumer downStream,
			Clock clock) {
		Pinger pinger = new Pinger(metrics, clock);
		this.writer = new IrcWriter(pinger);
		this.listener = new IrcHooks(downStream, metrics, pinger, silent, writer, clock);
		this.server = server.split(",");
		this.port = port;
		this.nickname = nickname;
		this.password = password;
		this.autojoinChannel = autojoinChannel;
	}

	private final IrcHooks listener;
	@Getter // we use this to hack local tests
	private final IrcWriter writer;

	private final String[] server;
	private final int port;
	private final String nickname;
	private final String password;
	private final String autojoinChannel;

	private final GameChatClientMetrics metrics = new GameChatClientMetrics();

	private volatile boolean reconnect = true;
	private int reconnectTimeout = 10000;

	@Override
	public void run() {
		log.info("Starting Tillerinobot tag {}", StringUtils.defaultIfBlank(System.getenv("TAG"), "(unknown)"));
		for (int i = 0; reconnect; i++) {
			try {
				try {
					final CustomThreadedListenerManager listenerManager = new CustomThreadedListenerManager();
					listenerManager.addListener(listener);
					
					Builder<PircBotX> configurationBuilder = new Configuration.Builder<PircBotX>()
							.setServer(server[i % server.length], port)
							.setMessageDelay(MESSAGE_DELAY)
							.setListenerManager(listenerManager)
							.setName(nickname)
							.setEncoding(StandardCharsets.UTF_8)
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
						bot = new CloseableBot(configurationBuilder.buildConfiguration());
						bot.startBot();
						log.info("Bot stopped");
					}
				} finally {
					bot = null;
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
					Thread.currentThread().interrupt();
					reconnect = false;
				}
			}
		}
		log.info("Exiting");
	}

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

	@Override
	public Result<GameChatClientMetrics, Error> getMetrics() {
		CloseableBot bot = this.bot;
		metrics.setConnected(bot != null && bot.isConnected());
		return ok(Mapper.INSTANCE.copy(metrics));
	}

	public void disconnectSoftly() {
		CloseableBot bot = this.bot;
		if (bot != null) {
			bot.sendIRC().quitServer();
		}
	}

	public void stopReconnecting() {
		reconnect = false;
	}

	@org.mapstruct.Mapper
	public interface Mapper {
		public static final Mapper INSTANCE = Mappers.getMapper(Mapper.class);

		GameChatClientMetrics copy(GameChatClientMetrics m);
	}
}
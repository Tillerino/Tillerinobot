package tillerino.tillerinobot;


import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import lombok.extern.slf4j.Slf4j;
import static org.apache.commons.lang3.StringUtils.*;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.MDC;
import org.pircbotx.User;
import org.pircbotx.hooks.CoreHooks;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.DisconnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.events.QuitEvent;
import org.pircbotx.hooks.events.ServerResponseEvent;
import org.pircbotx.hooks.events.UnknownEvent;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BotBackend.IRCName;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException.QuietException;
import tillerino.tillerinobot.handlers.AccHandler;
import tillerino.tillerinobot.handlers.NPHandler;
import tillerino.tillerinobot.handlers.OptionsHandler;
import tillerino.tillerinobot.handlers.RecommendHandler;
import tillerino.tillerinobot.handlers.ResetHandler;
import tillerino.tillerinobot.handlers.WithHandler;
import tillerino.tillerinobot.lang.Default;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.rest.BotInfoService;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Slf4j
@SuppressWarnings(value = { "rawtypes", "unchecked" })
public class IRCBot extends CoreHooks {
	public interface IRCBotUser {
		/**
		 * @return the user's IRC nick, not their actual user name.
		 */
		@IRCName String getNick();
		/**
		 * 
		 * @param msg
		 * @return true if the message was sent
		 */
		boolean message(String msg);
		
		/**
		 * 
		 * @param msg
		 * @return true if the action was sent
		 */
		boolean action(String msg);
	}
	
	public interface CommandHandler {
		public boolean handle(String command, IRCBotUser ircUser, OsuApiUser apiUser, UserData userData)
				throws UserException, IOException, SQLException;
	}
	
	final BotBackend backend;
	final private boolean silent;
	final RecommendationsManager manager;
	final BotInfoService botInfo;
	final UserDataManager userDataManager;
	final List<CommandHandler> commandHandlers = new ArrayList<>();
	
	@Inject
	public IRCBot(BotBackend backend, RecommendationsManager manager,
			BotInfoService botInfo, UserDataManager userDataManager,
			Pinger pinger, @Named("tillerinobot.ignore") boolean silent) {
		this.backend = backend;
		this.manager = manager;
		this.botInfo = botInfo;
		this.userDataManager = userDataManager;
		this.pinger = pinger;
		this.silent = silent;
		
		commandHandlers.add(new ResetHandler(manager));
		commandHandlers.add(new OptionsHandler());
		commandHandlers.add(new AccHandler(backend));
		commandHandlers.add(new WithHandler(backend));
		commandHandlers.add(new RecommendHandler(backend, manager));
	}

	@Override
	public void onConnect(ConnectEvent event) throws Exception {
		botInfo.setRunningSince(System.currentTimeMillis());
		log.info("connected");
	}
	
	@Override
	public void onAction(ActionEvent event) throws Exception {
		if(silent)
			return;
		
		if (event.getChannel() == null || event.getUser().getNick().equals("Tillerino")) {
			processPrivateAction(fromIRC(event.getUser()), event.getMessage());
		}
	}

	/**
	 * additional locks to avoid users causing congestion in the fair locks by queuing commands in multiple threads
	 */
	LoadingCache<String, Semaphore> perUserLock = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build(new CacheLoader<String, Semaphore>() {
		@Override
		public Semaphore load(String arg0) throws Exception {
			return new Semaphore(1);
		}
	});

	void processPrivateAction(IRCBotUser user, String message) {
		MDC.put("user", user.getNick());
		log.info("action: " + message);
		
		Language lang = new Default();

		Semaphore semaphore = perUserLock.getUnchecked(user.getNick());
		if(!semaphore.tryAcquire()) {
			log.warn("concurrent action");
			return;
		}

		try {
			OsuApiUser apiUser = getUserOrThrow(user);
			UserData userData = userDataManager.getData(apiUser.getUserId());
			lang = userData.getLanguage();
			
			checkVersionInfo(user);

			new NPHandler(backend).handle(message, user, apiUser, userData);
		} catch (Throwable e) {
			handleException(user, e, lang);
		} finally {
			semaphore.release();
		}
	}

	private void handleException(IRCBotUser user, Throwable e, Language lang) {
		try {
			if(e instanceof ExecutionException) {
				e = e.getCause();
			}
			if(e instanceof UserException) {
				if(e instanceof QuietException) {
					return;
				}
				user.message(e.getMessage());
			} else {
				String string = getRandomString(6);

				if (e instanceof IOException) {
					user.message(lang.externalException(string));
				} else {
					user.message(lang.internalException(string));
				}
				log.error(string + ": fucked up", e);
			}
		} catch (Throwable e1) {
			log.error("holy balls", e1);
		}
	}

	public static String getRandomString(int length) {
		Random r = new Random();
		char[] chars = new char[length];
		for (int j = 0; j < chars.length; j++) {
			chars[j] = (char) ('A' + r.nextInt(26));
		}
		String string = new String(chars);
		return string;
	}

	@Override
	public void onPrivateMessage(PrivateMessageEvent event) throws Exception {
		if(silent)
			return;
		
		processPrivateMessage(fromIRC(event.getUser()), event.getMessage());
	}
	
	Semaphore senderSemaphore = new Semaphore(1, true);
	
	final Pinger pinger;
	
	IRCBotUser fromIRC(final User user) {
		return new IRCBotUser() {
			
			@Override
			public boolean message(String msg) {
				try {
					senderSemaphore.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
					return false;
				}
				try {
					pinger.ping(user.getBot());
					
					user.send().message(msg);
					log.info("sent: " + msg);
					botInfo.setLastSentMessage(System.currentTimeMillis());
					return true;
				} catch (IOException | InterruptedException e) {
					log.error("not sent: " + e.getMessage());
					return false;
				} finally {
					senderSemaphore.release();
				}
			}
			
			@Override
			public boolean action(String msg) {
				try {
					senderSemaphore.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
					return false;
				}
				try {
					pinger.ping(user.getBot());
					
					user.send().action(msg);
					log.info("sent action: " + msg);
					return true;
				} catch (IOException | InterruptedException e) {
					log.error("action not sent: " + e.getMessage());
					return false;
				} finally {
					senderSemaphore.release();
				}
			}
			
			@Override
			@IRCName
			@SuppressFBWarnings(value = "TQ", justification = "producer")
			public String getNick() {
				return user.getNick();
			}
		};
	}
	
	void processPrivateMessage(final IRCBotUser user, String originalMessage) {
		MDC.put("user", user.getNick());
		log.info("received: " + originalMessage);

		Language lang = new Default();

		Semaphore semaphore = perUserLock.getUnchecked(user.getNick());
		if(!semaphore.tryAcquire()) {
			log.warn("concurrent message");
			return;
		}

		try {
			OsuApiUser apiUser = getUserOrThrow(user);
			UserData userData = userDataManager.getData(apiUser.getUserId());
			lang = userData.getLanguage();
			
			Pattern hugPattern = Pattern.compile("\\bhugs?\\b", Pattern.CASE_INSENSITIVE);
			
			if(hugPattern.matcher(originalMessage).find()) {
				if (apiUser != null && userData.getHearts() > 0) {
					lang.hug(user, apiUser);
					return;
				}
			}
			
			if (!originalMessage.startsWith("!")) {
				return;
			}
			originalMessage = originalMessage.substring(1).trim();

			checkVersionInfo(user);
			
			if(getLevenshteinDistance(originalMessage.toLowerCase(), "help") <= 1) {
				user.message(lang.help());
			} else if(getLevenshteinDistance(originalMessage.toLowerCase(), "faq") <= 1) {
				user.message(lang.faq());
			} else if(getLevenshteinDistance(originalMessage.toLowerCase().substring(0, Math.min("complain".length(), originalMessage.length())), "complain") <= 2) {
				Recommendation lastRecommendation = manager
						.getLastRecommendation(apiUser.getUserId());
				if(lastRecommendation != null && lastRecommendation.beatmap != null) {
					log.warn("COMPLAINT: " + lastRecommendation.beatmap.getBeatmap().getBeatmapId() + " mods: " + lastRecommendation.bareRecommendation.getMods() + ". Recommendation source: " + Arrays.asList(ArrayUtils.toObject(lastRecommendation.bareRecommendation.getCauses())));
					user.message(lang.complaint());
				}
			} else {
				boolean handled = false;
				for (CommandHandler handler : commandHandlers) {
					if (handled = handler.handle(originalMessage, user,	apiUser, userData)) {
						break;
					}
				}
				if (!handled) {
					throw new UserException(lang.unknownCommand(originalMessage));
				}
			}
		} catch (Throwable e) {
			handleException(user, e, lang);
		} finally {
			semaphore.release();
		}
	}

	private void checkVersionInfo(final IRCBotUser user) throws SQLException, UserException {
		int userVersion = backend.getLastVisitedVersion(user.getNick());
		if(userVersion < currentVersion) {
			if(versionMessage == null || user.message(versionMessage)) {
				backend.setLastVisitedVersion(user.getNick(), currentVersion);
			}
		}
	}
	
	@Override
	public void onDisconnect(DisconnectEvent event) throws Exception {
		log.info("disconnected");
		exec.shutdown();
	}
	
	AtomicLong lastSerial = new AtomicLong(System.currentTimeMillis());
	
	@Override
	public void onEvent(Event event) throws Exception {
		MDC.put("event", lastSerial.incrementAndGet());
		
		botInfo.setLastInteraction(System.currentTimeMillis());
		
		if(lastListTime < System.currentTimeMillis() - 60 * 60 * 1000) {
			lastListTime = System.currentTimeMillis();
			
			event.getBot().sendRaw().rawLine("NAMES #osu");
		}
		
		super.onEvent(event);
	}
	
	@Override
	public void onUnknown(UnknownEvent event) throws Exception {
		pinger.handleUnknownEvent(event);
	}
	
	static final int currentVersion = 8;
	static final String versionMessage = "Gamma recommendations are now available and the default engine for players upward of rank 100k. You can now check pp for custom accuracies with the !acc command. See !help for more information.";
	
	long lastListTime = System.currentTimeMillis();
	
	ExecutorService exec = Executors.newFixedThreadPool(4, new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setDaemon(true);
			return t;
		}
	});
	
	@Override
	public void onJoin(JoinEvent event) throws Exception {
		final String nick = event.getUser().getNick();

		if (silent) {
			return;
		}

		MDC.put("user", nick);
		IRCBotUser user = fromIRC(event.getUser());
		welcomeIfDonator(user);

		scheduleRegisterActivity(nick);
	}
	
	void welcomeIfDonator(IRCBotUser user) {
		try {
			Integer userid = backend.resolveIRCName(user.getNick());
			
			if(userid == null)
				return;
			
			OsuApiUser apiUser = backend.getUser(userid, 0);
			
			if(apiUser == null)
				return;
			
			if(backend.getDonator(apiUser) > 0) {
				// this is a donator, let's welcome them!
				
				long inactiveTime = System.currentTimeMillis() - backend.getLastActivity(apiUser);
				
				userDataManager.getData(userid).getLanguage()
						.welcomeUser(user, apiUser, inactiveTime);
				
				checkVersionInfo(user);
			}
		} catch (Exception e) {
			log.error("error welcoming potential donator", e);
		}
	}

	public void scheduleRegisterActivity(final String nick) {
		try {
			exec.submit(new Runnable() {
				@Override
				public void run() {
					registerActivity(nick);
				}
			});
		} catch (RejectedExecutionException e) {
			// bot is shutting down
		}
	}
	
	@Override
	public void onPart(PartEvent event) throws Exception {
		scheduleRegisterActivity(event.getUser().getNick());
	}

	@Override
	public void onQuit(QuitEvent event) throws Exception {
		scheduleRegisterActivity(event.getUser().getNick());
	}
	
	@Override
	public void onServerResponse(ServerResponseEvent event) throws Exception {
		if(event.getCode() == 353) {
			ImmutableList<String> parsedResponse = event.getParsedResponse();
			
			String[] usernames = parsedResponse.get(parsedResponse.size() - 1).split(" ");
			
			for (int i = 0; i < usernames.length; i++) {
				String nick = usernames[i];
				
				if(nick.startsWith("@") || nick.startsWith("+"))
					nick = nick.substring(1);
				
				scheduleRegisterActivity(nick);
			}
			
			System.out.println("processed user list event");
		} else {
			super.onServerResponse(event);
		}
	}

	private void registerActivity(final @IRCName String fNick) {
		try {
			Integer userid = backend.resolveIRCName(fNick);
			
			if(userid == null) {
				return;
			}
			
			backend.registerActivity(userid);
		} catch (Exception e) {
			log.error("error logging activity", e);
		}
	}

	@Nonnull
	OsuApiUser getUserOrThrow(IRCBotUser user) throws UserException, SQLException, IOException {
		Integer userId = backend.resolveIRCName(user.getNick());
		
		if(userId == null) {
			String string = IRCBot.getRandomString(8);
			log.error("bot user not resolvable " + string + " name: " + user.getNick());
			
			throw new UserException(new Default().unresolvableName(string, user.getNick()));
		}
		
		OsuApiUser apiUser = backend.getUser(userId, 60 * 60 * 1000);
		
		if(apiUser == null) {
			throw new RuntimeException("nickname was resolved, but user not found in api: " + userId);
		}
		
		return apiUser;
	}
}

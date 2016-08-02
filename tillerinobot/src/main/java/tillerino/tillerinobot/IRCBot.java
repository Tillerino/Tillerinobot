package tillerino.tillerinobot;


import static org.apache.commons.lang3.StringUtils.*;

import java.io.IOException;
import java.net.SocketTimeoutException;
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
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManagerFactory;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ArrayUtils;
import org.pircbotx.PircBotX;
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
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BotBackend.IRCName;
import tillerino.tillerinobot.BotRunnerImpl.CloseableBot;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException.QuietException;
import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager;
import tillerino.tillerinobot.handlers.AccHandler;
import tillerino.tillerinobot.handlers.DebugHandler;
import tillerino.tillerinobot.handlers.LinkPpaddictHandler;
import tillerino.tillerinobot.handlers.NPHandler;
import tillerino.tillerinobot.handlers.OptionsHandler;
import tillerino.tillerinobot.handlers.RecentHandler;
import tillerino.tillerinobot.handlers.RecommendHandler;
import tillerino.tillerinobot.handlers.ResetHandler;
import tillerino.tillerinobot.handlers.WithHandler;
import tillerino.tillerinobot.handlers.FixIDHandler;
import tillerino.tillerinobot.lang.Default;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.rest.BotInfoService.BotInfo;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Slf4j
@SuppressWarnings(value = { "rawtypes", "unchecked" })
public class IRCBot extends CoreHooks implements TidyObject {
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

	public static final String MDC_HANDLER = "handler";
	public static final String MDC_STATE = "state";
	
	final BotBackend backend;
	final private boolean silent;
	final RecommendationsManager manager;
	final BotInfo botInfo;
	final UserDataManager userDataManager;
	final List<CommandHandler> commandHandlers = new ArrayList<>();
	final ThreadLocalAutoCommittingEntityManager em;
	final EntityManagerFactory emf;
	final IrcNameResolver resolver;
	
	@Inject
	public IRCBot(BotBackend backend, RecommendationsManager manager,
			BotInfo botInfo, UserDataManager userDataManager,
			Pinger pinger, @Named("tillerinobot.ignore") boolean silent,
			ThreadLocalAutoCommittingEntityManager em,
			EntityManagerFactory emf, IrcNameResolver resolver) {
		this.backend = backend;
		this.manager = manager;
		this.botInfo = botInfo;
		this.userDataManager = userDataManager;
		this.pinger = pinger;
		this.silent = silent;
		this.em = em;
		this.emf = emf;
		this.resolver = resolver;
		
		commandHandlers.add(new ResetHandler(manager));
		commandHandlers.add(new OptionsHandler());
		commandHandlers.add(new AccHandler(backend));
		commandHandlers.add(new WithHandler(backend));
		commandHandlers.add(new RecommendHandler(backend, manager));
		commandHandlers.add(new RecentHandler(backend));
		commandHandlers.add(new DebugHandler(backend, resolver));
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
			MDC.put("user", event.getUser().getNick());
			processPrivateAction(fromIRC(event.getUser(), event), event.getMessage());
		}
	}
	
	/**
	 * This wrapper around a Semaphore keeps track of when it was last acquired
	 * via {@link #tryAcquire()} and what happened since.
	 */
	public static class TimingSemaphore {
		private long lastAcquired = 0;
		
		private Thread lastAcquiredThread = null;
		
		private int attemptsSinceLastAcquired = 0;
		
		private boolean sentWarning = false;
		
		private final Semaphore semaphore;
		
		public TimingSemaphore(int permits, boolean fair) {
			semaphore = new Semaphore(permits, fair);
		}
		
		public synchronized boolean tryAcquire() {
			if(!semaphore.tryAcquire()) {
				return false;
			}
			lastAcquired = System.currentTimeMillis();
			lastAcquiredThread = Thread.currentThread();
			attemptsSinceLastAcquired = 0;
			sentWarning = false;
			return true;
		}
		
		public long getLastAcquired() {
			return lastAcquired;
		}
		
		public Thread getLastAcquiredThread() {
			return lastAcquiredThread;
		}

		public void release() {
			semaphore.release();
		}
		
		public int getAttemptsSinceLastAcquired() {
			return ++attemptsSinceLastAcquired;
		}
		
		public boolean isSentWarning() {
			if(!sentWarning) {
				sentWarning = true;
				return false;
			}
			return true;
		}
	}

	/**
	 * additional locks to avoid users causing congestion in the fair locks by queuing commands in multiple threads
	 */
	LoadingCache<String, TimingSemaphore> perUserLock = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build(new CacheLoader<String, TimingSemaphore>() {
		@Override
		public TimingSemaphore load(String arg0) throws Exception {
			return new TimingSemaphore(1, false);
		}
	});
	
	void handleSemaphoreInUse(String purpose, TimingSemaphore semaphore, Language lang, IRCBotUser user) {
		double processing = (System.currentTimeMillis() - semaphore.getLastAcquired()) / 1000d;
		if(processing > 5) {
			StackTraceElement[] stackTrace = semaphore.getLastAcquiredThread().getStackTrace();
			stackTrace = Stream.of(stackTrace)
					.filter(elem -> elem.getClassName().contains("tillerino"))
					.toArray(StackTraceElement[]::new);
			Throwable t = new Throwable("Processing thread's stack trace");
			t.setStackTrace(stackTrace);
			log.warn(purpose + " - request has been processing for " + processing, t);
			if(!semaphore.isSentWarning()) {
				user.message(lang.getPatience());
			}
		} else {
			log.debug(purpose);
		}
		if(semaphore.getAttemptsSinceLastAcquired() >= 3 && !semaphore.isSentWarning()) {
			user.message("[http://i.imgur.com/Ykfua8r.png ...]");
		}
	}

	void processPrivateAction(IRCBotUser user, String message) {
		log.debug("action: " + message);
		
		Language lang = new Default();

		TimingSemaphore semaphore = perUserLock.getUnchecked(user.getNick());
		if(!semaphore.tryAcquire()) {
			handleSemaphoreInUse("concurrent action", semaphore, lang, user);
			return;
		}

		try {
			OsuApiUser apiUser = getUserOrThrow(user);
			UserData userData = userDataManager.getData(apiUser.getUserId());
			lang = userData.getLanguage();
			
			checkVersionInfo(user);

			new NPHandler(backend).handle(message, user, apiUser, userData);
		} catch (RuntimeException | Error | UserException | IOException | SQLException | InterruptedException e) {
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
			if(e instanceof InterruptedException) {
				return;
			}
			if(e instanceof UserException) {
				if(e instanceof QuietException) {
					return;
				}
				user.message(e.getMessage());
			} else {
				if (e instanceof SocketTimeoutException) {
					user.message(lang.apiTimeoutException());
					log.debug("osu api timeout");
				} else {
					String string = logException(e, log);
	
					if (e instanceof IOException) {
						user.message(lang.externalException(string));
					} else {
						user.message(lang.internalException(string));
					}
				}
			}
		} catch (Throwable e1) {
			log.error("holy balls", e1);
		}
	}

	public static String logException(Throwable e, Logger logger) {
		String string = getRandomString(6);
		logger.error(string + ": fucked up", e);
		return string;
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
		
		MDC.put("user", event.getUser().getNick());
		processPrivateMessage(fromIRC(event.getUser(), event), event.getMessage());
	}
	
	Semaphore senderSemaphore = new Semaphore(1, true);
	
	final Pinger pinger;
	
	IRCBotUser fromIRC(final User user, final Event<PircBotX> event) {
		return new IRCBotUser() {
			
			@Override
			public boolean message(String msg) {
				try {
					senderSemaphore.acquire();
				} catch (InterruptedException e) {
					return false;
				}
				try {
					pinger.ping((CloseableBot) user.getBot());
					
					user.send().message(msg);
					MDC.put("duration", System.currentTimeMillis() - event.getTimestamp() + "");
					log.debug("sent: " + msg);
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
					return false;
				}
				try {
					pinger.ping((CloseableBot) user.getBot());
					
					user.send().action(msg);
					MDC.put("duration", System.currentTimeMillis() - event.getTimestamp() + "");
					log.debug("sent action: " + msg);
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
		MDC.put(MDC_STATE, "msg");
		log.debug("received: " + originalMessage);

		Language lang = new Default();

		TimingSemaphore semaphore = perUserLock.getUnchecked(user.getNick());
		if(!semaphore.tryAcquire()) {
			handleSemaphoreInUse("concurrent message", semaphore, lang, user);
			return;
		}

		try {
			if (new FixIDHandler(resolver).handle(originalMessage, user, null, null)) {
				return;
			}
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
			
			if(new LinkPpaddictHandler(backend).handle(originalMessage, user, apiUser, userData)) {
				return;
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
					log.debug("COMPLAINT: " + lastRecommendation.beatmap.getBeatmap().getBeatmapId() + " mods: " + lastRecommendation.bareRecommendation.getMods() + ". Recommendation source: " + Arrays.asList(ArrayUtils.toObject(lastRecommendation.bareRecommendation.getCauses())));
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
		} catch (RuntimeException | Error | UserException | IOException | SQLException | InterruptedException e) {
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
		log.info("received DisconnectEvent");
		tidyUp(false);
	}
	
	AtomicLong lastSerial = new AtomicLong(System.currentTimeMillis());
	
	@Override
	public void onEvent(Event event) throws Exception {
		MDC.put("event", "" + lastSerial.incrementAndGet());
		em.setThreadLocalEntityManager(emf.createEntityManager());
		try {
			botInfo.setLastInteraction(System.currentTimeMillis());

			if (lastListTime < System.currentTimeMillis() - 60 * 60 * 1000) {
				lastListTime = System.currentTimeMillis();

				event.getBot().sendRaw().rawLine("NAMES #osu");
			}

			super.onEvent(event);
		} finally {
			em.close();
			MDC.clear();
		}
	}
	
	@Override
	public void onUnknown(UnknownEvent event) throws Exception {
		pinger.handleUnknownEvent(event);
	}
	
	static final int currentVersion = 10;
	static final String versionMessage = "Quick update: I now speak [https://github.com/Tillerino/Tillerinobot/blob/master/tillerinobot/src/main/java/tillerino/tillerinobot/UserDataManager.java#L62 a bunch of new languages] thanks to the contributions of your fellow players."
			+ " If you'd like to contribute a translation as well, please [https://github.com/Tillerino/Tillerinobot/wiki/Contact get in touch]!"
			+ " Oh and you can get hidden mods for gamma recommendations by adding \" hd\" to whatever you're requesting."
			+ " If you haven't already done so, make sure to check out [http://ppaddict.tillerino.org/ ppaddict].";
	
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
		IRCBotUser user = fromIRC(event.getUser(), event);
		welcomeIfDonator(user);

		scheduleRegisterActivity(nick);
	}
	
	void welcomeIfDonator(IRCBotUser user) {
		try {
			Integer userid;
			try {
				userid = resolver.resolveIRCName(user.getNick());
			} catch (SocketTimeoutException e1) {
				log.debug("timeout while resolving username {} (welcomeIfDonator)", user.getNick());
				return;
			}
			
			if(userid == null)
				return;
			
			OsuApiUser apiUser;
			try {
				apiUser = backend.getUser(userid, 0);
			} catch (SocketTimeoutException e) {
				log.debug("osu api timeout while getting user {} (welcomeIfDonator)", userid);
				return;
			}
			
			if(apiUser == null)
				return;
			
			if(backend.getDonator(apiUser) > 0) {
				// this is a donator, let's welcome them!
				UserData data = userDataManager.getData(userid);
				
				if (!data.isShowWelcomeMessage())
					return;

				long inactiveTime = System.currentTimeMillis() - backend.getLastActivity(apiUser);
				
				data.getLanguage()
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
					MDC.put("user", nick);
					em.setThreadLocalEntityManager(emf.createEntityManager());
					try {
						registerActivity(nick);
					} finally {
						em.close();
						MDC.clear();
					}
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
			Integer userid = resolver.resolveIRCName(fNick);
			
			if(userid == null) {
				return;
			}
			
			backend.registerActivity(userid);
		} catch (SocketTimeoutException e) {
			log.debug("osu api timeout while logging activity of user {}", fNick);
		} catch (Exception e) {
			log.error("error logging activity", e);
		}
	}

	@Nonnull
	OsuApiUser getUserOrThrow(IRCBotUser user) throws UserException, SQLException, IOException {
		Integer userId = resolver.resolveIRCName(user.getNick());
		
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

	@Override
	public void tidyUp(boolean fromShutdownHook) {
		log.info("tidyUp({})", fromShutdownHook);
		if(!exec.isShutdown()) {
			exec.shutdownNow();
		}
	}
}

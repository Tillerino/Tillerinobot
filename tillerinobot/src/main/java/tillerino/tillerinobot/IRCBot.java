package tillerino.tillerinobot;


import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.WebApplicationException;

import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.CoreHooks;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.DisconnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.events.ServerResponseEvent;
import org.pircbotx.hooks.events.UnknownEvent;
import org.pircbotx.hooks.types.GenericUserEvent;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.tillerino.osuApiModel.OsuApiUser;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.BotBackend.IRCName;
import tillerino.tillerinobot.BotRunnerImpl.CloseableBot;
import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.AsyncTask;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;
import tillerino.tillerinobot.CommandHandler.ResponseList;
import tillerino.tillerinobot.CommandHandler.Success;
import tillerino.tillerinobot.CommandHandler.Task;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException.QuietException;
import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager;
import tillerino.tillerinobot.handlers.AccHandler;
import tillerino.tillerinobot.handlers.ComplaintHandler;
import tillerino.tillerinobot.handlers.DebugHandler;
import tillerino.tillerinobot.handlers.FixIDHandler;
import tillerino.tillerinobot.handlers.HelpHandler;
import tillerino.tillerinobot.handlers.LinkPpaddictHandler;
import tillerino.tillerinobot.handlers.NPHandler;
import tillerino.tillerinobot.handlers.OptionsHandler;
import tillerino.tillerinobot.handlers.OsuTrackHandler;
import tillerino.tillerinobot.handlers.RecentHandler;
import tillerino.tillerinobot.handlers.RecommendHandler;
import tillerino.tillerinobot.handlers.ResetHandler;
import tillerino.tillerinobot.handlers.WithHandler;
import tillerino.tillerinobot.lang.Default;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.osutrack.OsutrackDownloader;
import tillerino.tillerinobot.osutrack.UpdateResult;
import tillerino.tillerinobot.rest.BotInfoService.BotInfo;

@Slf4j
@SuppressWarnings(value = { "rawtypes", "unchecked" })
public class IRCBot extends CoreHooks {
	interface IRCBotUser {
		/**
		 * @return the user's IRC nick, not their actual user name.
		 */
		@IRCName String getNick();
		/**
		 * 
		 * @param msg
		 * @param success see {@link Success}
		 * @return true if the message was sent
		 */
		boolean message(String msg, boolean success);
		
		/**
		 * 
		 * @param msg
		 * @return true if the action was sent
		 */
		boolean action(String msg);
	}

	public static final String MDC_HANDLER = "handler";
	public static final String MDC_STATE = "state";
	public static final String MDC_SUCCESS = "success";
	public static final String MDC_DURATION = "duration";
	public static final String MCD_OSU_API_RATE_BLOCKED_TIME = "osuApiRateBlockedTime";
	
	final BotBackend backend;
	final private boolean silent;
	final RecommendationsManager manager;
	final BotInfo botInfo;
	final UserDataManager userDataManager;
	final List<CommandHandler> commandHandlers = new ArrayList<>();
	final ThreadLocalAutoCommittingEntityManager em;
	final EntityManagerFactory emf;
	final IrcNameResolver resolver;
	final OsutrackDownloader osutrackDownloader;
	private final RateLimiter rateLimiter;
	
	@Inject
	public IRCBot(BotBackend backend, RecommendationsManager manager,
			BotInfo botInfo, UserDataManager userDataManager,
			Pinger pinger, @Named("tillerinobot.ignore") boolean silent,
			ThreadLocalAutoCommittingEntityManager em,
			EntityManagerFactory emf, IrcNameResolver resolver, OsutrackDownloader osutrackDownloader,
			@Named("tillerinobot.maintenance") ExecutorService exec, RateLimiter rateLimiter) {
		this.backend = backend;
		this.manager = manager;
		this.botInfo = botInfo;
		this.userDataManager = userDataManager;
		this.pinger = pinger;
		this.silent = silent;
		this.em = em;
		this.emf = emf;
		this.resolver = resolver;
		this.osutrackDownloader = osutrackDownloader;
		this.exec = exec;
		this.rateLimiter = rateLimiter;
		
		commandHandlers.add(new ResetHandler(manager));
		commandHandlers.add(new OptionsHandler(manager));
		commandHandlers.add(new AccHandler(backend));
		commandHandlers.add(new WithHandler(backend));
		commandHandlers.add(new RecommendHandler(manager));
		commandHandlers.add(new RecentHandler(backend));
		commandHandlers.add(new DebugHandler(backend, resolver));
		commandHandlers.add(new HelpHandler());
		commandHandlers.add(new ComplaintHandler(manager));
		commandHandlers.add(new OsuTrackHandler(osutrackDownloader));
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
		rateLimiter.setThreadPriority(RateLimiter.REQUEST);
		
		if (event.getChannel() == null || event.getUser().getNick().equals("Tillerino")) {
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
				user.message(lang.getPatience(), false);
			}
		} else {
			log.debug(purpose);
		}
		if(semaphore.getAttemptsSinceLastAcquired() >= 3 && !semaphore.isSentWarning()) {
			user.message("[http://i.imgur.com/Ykfua8r.png ...]", false);
		}
	}

	void processPrivateAction(IRCBotUser user, String message) {
		MDC.put(MDC_STATE, "action");
		log.debug("action: " + message);
		botInfo.setLastReceivedMessage(System.currentTimeMillis());
		
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

			sendResponse(new NPHandler(backend).handle(message, apiUser, userData), user);
		} catch (RuntimeException | Error | UserException | IOException | SQLException | InterruptedException e) {
			handleException(user, e, lang);
		} finally {
			semaphore.release();
		}
	}

	private void handleException(IRCBotUser user, Throwable e, Language lang) {
		try {
			MDC.remove(MDC_SUCCESS);
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
				user.message(e.getMessage(), false);
			} else {
				if (e instanceof ServiceUnavailableException) {
					// We're shutting down. Nothing to do here.
				} else if (isTimeout(e)) {
					user.message(lang.apiTimeoutException(), false);
					log.debug("osu api timeout");
				} else {
					String string = logException(e, log);
	
					if ((e instanceof IOException) || isExternalException(e)) {
						user.message(lang.externalException(string), false);
					} else {
						user.message(lang.internalException(string), false);
					}
				}
			}
		} catch (Throwable e1) {
			log.error("holy balls", e1);
		}
	}

	/**
	 * Checks if this is a JAX-RS exception that would have been thrown because
	 * an external osu resource is not available.
	 */
	public static boolean isExternalException(Throwable e) {
		if (!(e instanceof WebApplicationException)) {
			return false;
		}
		int code = ((WebApplicationException) e).getResponse().getStatus();
		/*
		 * 502 = Bad Gateway
		 * 504 = Gateway timeout
		 * 520 - 527 = Cloudflare, used by osu's web endpoints
		 */
		return code == 502 || code == 504 || (code >= 520 && code <= 527);
	}

	public static boolean isTimeout(Throwable e) {
		return (e instanceof SocketTimeoutException)
				|| ((e instanceof IOException) && e.getMessage().startsWith("Premature EOF"));
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
		rateLimiter.setThreadPriority(RateLimiter.REQUEST);
		
		processPrivateMessage(fromIRC(event.getUser(), event), event.getMessage());
	}

	@Override
	public void onMessage(MessageEvent event) throws Exception {
		botInfo.setLastReceivedMessage(System.currentTimeMillis());
	}
	
	Semaphore senderSemaphore = new Semaphore(1, true);
	
	final Pinger pinger;
	
	IRCBotUser fromIRC(final User user, final Event<PircBotX> event) {
		return new IRCBotUser() {
			
			@Override
			public boolean message(String msg, boolean success) {
				try {
					senderSemaphore.acquire();
				} catch (InterruptedException e) {
					return false;
				}
				try {
					pinger.ping((CloseableBot) user.getBot());
					
					user.send().message(msg);
					MDC.put(MDC_STATE, "sent");
					if (success) {
						MDC.put(MDC_DURATION, System.currentTimeMillis() - event.getTimestamp() + "");
						MDC.put(MDC_SUCCESS, "true");
						MDC.put(MCD_OSU_API_RATE_BLOCKED_TIME, String.valueOf(rateLimiter.blockedTime()));
						if (Objects.equals(MDC.get(MDC_HANDLER), RecommendHandler.MDC_FLAG)) {
							botInfo.setLastRecommendation(System.currentTimeMillis());
						}
					}
					log.debug("sent: " + msg);
					botInfo.setLastSentMessage(System.currentTimeMillis());
					return true;
				} catch (IOException | InterruptedException e) {
					log.error("not sent: " + e.getMessage());
					return false;
				} finally {
					MDC.remove(MDC_DURATION);
					MDC.remove(MDC_SUCCESS);
					MDC.remove(MCD_OSU_API_RATE_BLOCKED_TIME);
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
					MDC.put(MDC_STATE, "sent");
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
	
	void sendResponse(@Nullable Response response, IRCBotUser user) {
		if (response instanceof ResponseList) {
			for (Response r : ((ResponseList) response).responses) {
				sendResponse(r, user);
			}
		}
		if (response instanceof Message) {
			user.message(((Message) response).getContent(), false);
		} 
		if (response instanceof Success) {
			user.message(((Success) response).getContent(), true);
		} 
		if (response instanceof Action) {
			user.action(((Action) response).getContent());
		} 
		if (response instanceof Task) {
			((Task) response).run();
		}
		if (response instanceof AsyncTask) {
			exec.submit(() -> {
				em.setThreadLocalEntityManager(emf.createEntityManager());
				try {
					((AsyncTask) response).run();
				} finally {
					em.close();
				}
			});
		}
	}
	
	boolean tryHandleAndRespond(CommandHandler handler, String originalMessage,
			OsuApiUser apiUser, UserData userData, IRCBotUser user)
			throws UserException, IOException, SQLException,
			InterruptedException {
		Response response = handler.handle(originalMessage, apiUser, userData);
		if (response == null) {
			return false;
		}
		sendResponse(response, user);
		return true;
	}
	
	void processPrivateMessage(final IRCBotUser user, String originalMessage) {
		MDC.put(MDC_STATE, "msg");
		log.debug("received: " + originalMessage);
		botInfo.setLastReceivedMessage(System.currentTimeMillis());

		Language lang = new Default();

		TimingSemaphore semaphore = perUserLock.getUnchecked(user.getNick());
		if(!semaphore.tryAcquire()) {
			handleSemaphoreInUse("concurrent message", semaphore, lang, user);
			return;
		}

		try {
			if (tryHandleAndRespond(new FixIDHandler(resolver), originalMessage, null, null, user)) {
				return;
			}
			OsuApiUser apiUser = getUserOrThrow(user);
			UserData userData = userDataManager.getData(apiUser.getUserId());
			lang = userData.getLanguage();
			
			Pattern hugPattern = Pattern.compile("\\bhugs?\\b", Pattern.CASE_INSENSITIVE);
			
			if(hugPattern.matcher(originalMessage).find()) {
				if (apiUser != null && userData.getHearts() > 0) {
					sendResponse(lang.hug(apiUser), user);
					return;
				}
			}
			
			if(tryHandleAndRespond(new LinkPpaddictHandler(backend), originalMessage, apiUser, userData, user)) {
				return;
			}
			if (!originalMessage.startsWith("!")) {
				return;
			}
			originalMessage = originalMessage.substring(1).trim();

			checkVersionInfo(user);
			
			Response response = null;
			for (CommandHandler handler : commandHandlers) {
				if ((response = handler.handle(originalMessage, apiUser, userData)) != null) {
					sendResponse(response, user);
					break;
				}
				MDC.remove(MDC_HANDLER);
			}
			if (response == null) {
				throw new UserException(lang.unknownCommand(originalMessage));
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
			if(versionMessage == null || user.message(versionMessage, false)) {
				backend.setLastVisitedVersion(user.getNick(), currentVersion);
			}
		}
	}
	
	@Override
	public void onDisconnect(DisconnectEvent event) throws Exception {
		log.info("received DisconnectEvent");
	}
	
	AtomicLong lastSerial = new AtomicLong(System.currentTimeMillis());
	
	@Override
	public void onEvent(Event event) throws Exception {
		botInfo.setLastInteraction(System.currentTimeMillis());
		MDC.put("event", "" + lastSerial.incrementAndGet());
		em.setThreadLocalEntityManager(emf.createEntityManager());
		try {
			rateLimiter.setThreadPriority(RateLimiter.EVENT);
			// clear blocked time in case it wasn't cleared by the last thread
			rateLimiter.blockedTime();

			if (lastListTime < System.currentTimeMillis() - 60 * 60 * 1000) {
				lastListTime = System.currentTimeMillis();

				event.getBot().sendRaw().rawLine("NAMES #osu");
			}

			User user = null;
			if (event instanceof GenericUserEvent<?>) {
				user = ((GenericUserEvent) event).getUser();
				if (user != null) {
					MDC.put("user", user.getNick());
				}
			}

			super.onEvent(event);

			/*
			 * We delay registering the activity until after the event has been handled to
			 * avoid a race condition and to make sure that the event handler can find
			 * out the actual last active time.
			 */
			if (user != null) {
				scheduleRegisterActivity(user.getNick());
			}
		} finally {
			em.close();
			rateLimiter.clearThreadPriority();
			// clear blocked time so it isn't carried over to the next request under any circumstances
			rateLimiter.blockedTime();
			MDC.clear();
		}
	}
	
	@Override
	public void onUnknown(UnknownEvent event) throws Exception {
		pinger.handleUnknownEvent(event);
	}

	static final int currentVersion = 12;
	static final String versionMessage = "Quick update: You might have heard of a sweet tool called [https://ameobea.me/osutrack/ osu!track] made by [https://osu.ppy.sh/u/Ameo Ameo]."
			+ " Starting now, I can query it for you. Give it a go! Just type !u."
			+ " For more info check out the [https://github.com/Tillerino/Tillerinobot/wiki/osu!track wiki].";

	long lastListTime = System.currentTimeMillis();
	
	private final ExecutorService exec;
	
	@Override
	public void onJoin(JoinEvent event) throws Exception {
		if (silent) {
			return;
		}

		IRCBotUser user = fromIRC(event.getUser(), event);
		welcomeIfDonator(user);
	}
	
	void welcomeIfDonator(IRCBotUser user) {
		try {
			Integer userid;
			try {
				userid = resolver.resolveIRCName(user.getNick());
			} catch (IOException e) {
				if (isTimeout(e)) {
					log.debug("timeout while resolving username {} (welcomeIfDonator)", user.getNick());
					return;
				}
				throw e;
			}
			
			if(userid == null)
				return;
			
			OsuApiUser apiUser;
			try {
				apiUser = backend.getUser(userid, 0);
			} catch (IOException e) {
				if (isTimeout(e)) {
					log.debug("osu api timeout while getting user {} (welcomeIfDonator)", userid);
					return;
				}
				throw e;
			}
			
			if(apiUser == null)
				return;
			
			if(backend.getDonator(apiUser) > 0) {
				// this is a donator, let's welcome them!
				UserData data = userDataManager.getData(userid);
				
				if (!data.isShowWelcomeMessage())
					return;

				long inactiveTime = System.currentTimeMillis() - backend.getLastActivity(apiUser);
				
				Response welcome = data.getLanguage().welcomeUser(apiUser,
						inactiveTime);
				sendResponse(welcome, user);

				if (data.isOsuTrackWelcomeEnabled()) {
					UpdateResult update = osutrackDownloader.getUpdate(user.getNick());
					Response updateResponse = OsuTrackHandler.updateResultToResponse(update);
					sendResponse(updateResponse, user);
				}
				
				checkVersionInfo(user);
			}
		} catch (InterruptedException e) {
			// no problem
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
		} catch (Exception e) {
			if (isTimeout(e)) {
				log.debug("osu api timeout while logging activity of user {}", fNick);
			} else {
				log.error("error logging activity", e);
			}
		}
	}

	@Nonnull
	OsuApiUser getUserOrThrow(IRCBotUser user) throws UserException, SQLException, IOException, InterruptedException {
		Integer userId = resolver.resolveIRCName(user.getNick());
		
		if(userId == null) {
			String string = IRCBot.getRandomString(8);
			log.error("bot user not resolvable " + string + " name: " + user.getNick());

			// message not in language-files, since we cant possible know language atm
			throw new UserException("Your name is confusing me. Are you banned? If not, pls check out [https://github.com/Tillerino/Tillerinobot/wiki/How-to-fix-%22confusing-name%22-error this page] on how to resolve it!"
						+ " if that does not work, pls [https://github.com/Tillerino/Tillerinobot/wiki/Contact contact Tillerino]. (reference "
						+ string + ")");
		}
		
		OsuApiUser apiUser = backend.getUser(userId, 60 * 60 * 1000);
		
		if(apiUser == null) {
			throw new RuntimeException("nickname was resolved, but user not found in api: " + userId);
		}
		
		return apiUser;
	}
}

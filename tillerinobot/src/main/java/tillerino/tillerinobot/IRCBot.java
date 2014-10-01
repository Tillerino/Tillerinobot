package tillerino.tillerinobot;


import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import lombok.extern.slf4j.Slf4j;
import static org.apache.commons.lang3.StringUtils.*;

import org.apache.log4j.MDC;
import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.Utils;
import org.pircbotx.exception.IrcException;
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
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta.PercentageEstimates;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;
import tillerino.tillerinobot.UserException.QuietException;
import tillerino.tillerinobot.lang.Default;
import tillerino.tillerinobot.lang.Language;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

@Slf4j
@SuppressWarnings(value = { "rawtypes", "unchecked" })
public class IRCBot extends CoreHooks {
	Language lang = new Default();
	
	public interface IRCBotUser {
		/**
		 * @return the user's IRC nick, not their actual user name.
		 */
		String getNick();
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
	
	PircBotX bot;
	final BotBackend backend;
	final private String server;
	final private boolean rememberRecommendations;
	final private boolean silent;
	RecommendationsManager manager;
	
	@CheckForNull
	final BotAPIServer apiServer;
	
	public IRCBot(BotBackend backend, String server, int port, String nickname, String password, String autojoinChannel, boolean rememberRecommendations, boolean silent, BotAPIServer apiServer) {
		this.server = server;
		Builder<PircBotX> configurationBuilder = new Configuration.Builder<PircBotX>()
				.setServer(server, port).setMessageDelay(2000).setName(nickname).addListener(this).setEncoding(Charset.forName("UTF-8")).setAutoReconnect(false);
		if(password != null) {
				configurationBuilder.setServerPassword(password);
		}
		if(autojoinChannel != null) {
			configurationBuilder.addAutoJoinChannel(autojoinChannel);
		}
		bot = new PircBotX(configurationBuilder.buildConfiguration());
		this.backend = backend;
		this.manager = new RecommendationsManager(backend);
		this.rememberRecommendations = rememberRecommendations;
		this.silent = silent;
		this.apiServer = apiServer;
	}

	public void run() throws IOException, IrcException {
		if(apiServer != null) {
			apiServer.botInfo.setRunningSince(System.currentTimeMillis());
		}
		bot.startBot();
	}

	@Override
	public void onConnect(ConnectEvent event) throws Exception {
		System.out.println("connected!");
	}
	
	@Override
	public void onAction(ActionEvent event) throws Exception {
		if(silent)
			return;
		
		if (event.getChannel() == null || event.getUser().getNick().equals("Tillerino")) {
			processPrivateAction(fromIRC(event.getUser()), event.getMessage());
		}
	}

	Pattern npPattern = Pattern
			.compile("(?:is listening to|is watching|is playing) \\[http://osu.ppy.sh/b/(\\d+).*\\]((?: "
					+ "(?:"
					+ "-Easy|-NoFail|-HalfTime"
					+ "|\\+HardRock|\\+SuddenDeath|\\+Perfect|\\+DoubleTime|\\+Nightcore|\\+Hidden|\\+Flashlight"
					+ "|~Relax~|~AutoPilot~|-SpunOut|\\|Autoplay\\|" + "))*)");
	
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
		
		Semaphore semaphore = perUserLock.getUnchecked(user.getNick());
		if(!semaphore.tryAcquire()) {
			log.warn("concurrent action");
			return;
		}

		try {
			OsuApiUser apiUser = getUserOrThrow(user);
			
			checkVersionInfo(user);

			Matcher m = npPattern.matcher(message);

			if (!m.matches()) {
				log.error("no match: " + message);
				return;
			}

			int beatmapid = Integer.valueOf(m.group(1));

			long mods = 0;

			Pattern words = Pattern.compile("\\w+");

			Matcher mWords = words.matcher(m.group(2));

			while(mWords.find()) {
				Mods mod = Mods.valueOf(mWords.group());

				if(mod.isEffective())
					mods |= Mods.getMask(mod);
			}

			BeatmapMeta beatmap = backend.loadBeatmap(beatmapid, mods, lang);

			if (beatmap == null) {
				user.message(lang.unknownBeatmap());
				return;
			}
			
			String addition = null;
			if (beatmap.getEstimates() instanceof PercentageEstimates) {
				PercentageEstimates estimates = (PercentageEstimates) beatmap.getEstimates();
				
				if(estimates.getMods() != mods) {
					addition = "(" + lang.noInformationForModsShort() + ")";
				}
			}

			int hearts = backend.getDonator(apiUser);
			
			if(user.message(beatmap.formInfoMessage(false, addition, hearts))) {
				songInfoCache.put(user.getNick(), beatmapid);
				
				lang.optionalCommentOnNP(user, apiUser, beatmap);
			}

		} catch (Throwable e) {
			handleException(user, e);
		} finally {
			semaphore.release();
		}
	}

	private void handleException(IRCBotUser user, Throwable e) {
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

				user.message(lang.exception(string));
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
	
	Pinger pinger = new Pinger();
	
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
					pinger.ping();
					
					user.send().message(msg);
					log.info("sent: " + msg);
					apiServer.botInfo.setLastSentMessage(System.currentTimeMillis());
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
					pinger.ping();
					
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
			public String getNick() {
				return user.getNick();
			}
		};
	}
	
	Cache<String, Integer> songInfoCache = CacheBuilder.newBuilder().build(); 
	
	void processPrivateMessage(final IRCBotUser user, String message) throws IOException {
		MDC.put("user", user.getNick());
		log.info("received: " + message);

		Semaphore semaphore = perUserLock.getUnchecked(user.getNick());
		if(!semaphore.tryAcquire()) {
			log.warn("concurrent message");
			return;
		}

		try {
			OsuApiUser apiUser = getUserOrThrow(user);
			
			Pattern hugPattern = Pattern.compile("\\bhugs?\\b");
			
			if(hugPattern.matcher(message).find()) {
				if(apiUser != null && backend.getDonator(apiUser) > 0) {
					lang.hug(user, apiUser);
					return;
				}
			}
			
			if (!message.startsWith("!")) {
				return;
			}

			checkVersionInfo(user);

			message = message.substring(1).trim().toLowerCase();
			
			boolean isRecommend = false;
			
			if(message.equals("r")) {
				isRecommend = true;
				message = "";
			}
			if(getLevenshteinDistance(message, "recommend") <= 2) {
				isRecommend = true;
				message = "";
			}
			if(message.startsWith("r ")) {
				isRecommend = true;
				message = message.substring(2);
			}
			if(message.contains(" ")) {
				int pos = message.indexOf(' ');
				if(getLevenshteinDistance(message.substring(0, pos), "recommend") <= 2) {
					isRecommend = true;
					message = message.substring(pos + 1);
				}
			}
			
			if(getLevenshteinDistance(message, "help") <= 1) {
				user.message(lang.help());
			} else if(getLevenshteinDistance(message, "faq") <= 1) {
				user.message(lang.faq());
			} else if(getLevenshteinDistance(message.substring(0, Math.min("complain".length(), message.length())), "complain") <= 2) {
				Recommendation lastRecommendation = manager.getLastRecommendation(user.getNick());
				if(lastRecommendation != null && lastRecommendation.beatmap != null) {
					log.warn("COMPLAINT: " + lastRecommendation.beatmap.getBeatmap().getId() + " mods: " + lastRecommendation.bareRecommendation.getMods() + ". Recommendation source: " + Arrays.asList(lastRecommendation.bareRecommendation.getCauses()));
					user.message(lang.complaint());
				}
			} else if(isRecommend) {
				Recommendation recommendation = manager.getRecommendation(user.getNick(), apiUser, message, lang);

				if(recommendation.beatmap == null) {
					user.message(lang.excuseForError());
					log.error("unknow recommendation occurred");
					return;
				}
				String addition = null;
				if(recommendation.bareRecommendation.getMods() < 0) {
					addition = lang.tryWithMods();
				}
				if(recommendation.bareRecommendation.getMods() > 0 && recommendation.beatmap.getMods() == 0) {
					addition = lang.tryWithMods(Mods.getMods(recommendation.bareRecommendation.getMods()));
				}
				
				int hearts = backend.getDonator(apiUser);
				
				if(user.message(recommendation.beatmap.formInfoMessage(true, addition, hearts))) {
					songInfoCache.put(user.getNick(), recommendation.beatmap.getBeatmap().getId());
					if(rememberRecommendations) {
						backend.saveGivenRecommendation(user.getNick(), apiUser.getUserId(), recommendation.beatmap.getBeatmap().getId(), recommendation.bareRecommendation.getMods());
					}
					
					lang.optionalCommentOnRecommendation(user, apiUser, recommendation);
				}

			} else if(message.startsWith("with ")) {
				Integer lastSongInfo = songInfoCache.getIfPresent(user.getNick());
				if(lastSongInfo == null) {
					throw new UserException(lang.noLastSongInfo());
				}
				message = message.substring(5);
				
				Long mods = Mods.fromShortNamesContinuous(message);
				if(mods == null) {
					throw new UserException(lang.malformattedMods(message));
				}
				if(mods == 0)
					return;
				BeatmapMeta beatmap = backend.loadBeatmap(lastSongInfo, mods, lang);
				if(beatmap.getMods() == 0) {
					throw new UserException(lang.noInformationForMods());
				}
				
				int hearts = backend.getDonator(apiUser);
				
				if(user.message(beatmap.formInfoMessage(false, null, hearts))) {
					lang.optionalCommentOnWith(user, apiUser, beatmap);
				}
			} else {
				throw new UserException(lang.unknownCommand(message));
			}
		} catch (Throwable e) {
			handleException(user, e);
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
	
	void shutDown() {
		bot.sendIRC().quitServer();
	}
	
	@Override
	public void onDisconnect(DisconnectEvent event) throws Exception {
		exec.shutdown();
	}
	
	class Pinger {
		volatile String pingMessage = null;
		volatile CountDownLatch pingLatch = null;
		final AtomicBoolean quit = new AtomicBoolean(false);
		
		/*
		 * this method is synchronized through the sender semaphore
		 */
		void ping() throws IOException, InterruptedException {
			try {
				if(quit.get()) {
					throw new IOException("ping gate closed");
				}

				long time = System.currentTimeMillis();

				synchronized (this) {
					pingLatch = new CountDownLatch(1);
					pingMessage = getRandomString(16);
				}

				Utils.sendRawLineToServer(bot, "PING " + pingMessage);

				if(!pingLatch.await(10, TimeUnit.SECONDS)) {
					throw new IOException("ping timed out");
				}

				long ping = System.currentTimeMillis() - time;

				if(ping > 1500) {
					if(apiServer != null) {
						apiServer.botInfo.setLastPingDeath(System.currentTimeMillis());
					}
					throw new IOException("death ping: " + ping);
				}
			} catch(IOException e) {
				if(!quit.get()) {
					quit.set(true);
					bot.sendIRC().quitServer();
				}
				throw e;
			}
		}
		
		void handleUnknownEvent(UnknownEvent event) {
			synchronized(this) {
				if (pingMessage == null)
					return;

				boolean contains = event.getLine().contains(" PONG ");
				boolean endsWith = event.getLine().endsWith(pingMessage);
				if (contains
						&& endsWith) {
					pingLatch.countDown();
				}
			}
		}
	}
	
	
	AtomicLong lastSerial = new AtomicLong(System.currentTimeMillis());
	
	@Override
	public void onEvent(Event event) throws Exception {
		MDC.put("server", server);
		MDC.put("event", lastSerial.incrementAndGet());
		MDC.put("permanent", rememberRecommendations ? 1 : 0);
		
		if(apiServer != null) {
			apiServer.botInfo.setLastInteraction(System.currentTimeMillis());
		}
		
		if(lastListTime < System.currentTimeMillis() - 60 * 60 * 1000) {
			lastListTime = System.currentTimeMillis();
			
			bot.sendRaw().rawLine("NAMES #osu");
		}
		
		super.onEvent(event);
	}
	
	@Override
	public void onUnknown(UnknownEvent event) throws Exception {
		pinger.handleUnknownEvent(event);
	}
	
	static final int currentVersion = 7;
	static final String versionMessage = "I'm back \\o/";
	
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
		final String fNick = event.getUser().getNick();

		MDC.put("user", fNick);
		IRCBotUser user = fromIRC(event.getUser());
		welcomeIfDonator(user);
		
		exec.submit(new Runnable() {
			@Override
			public void run() {
				registerActivity(fNick);
			}
		});
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
				
				lang.welcomeUser(user, apiUser, inactiveTime);
				
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

	private void registerActivity(final String fNick) {
		try {
			Integer userid = backend.resolveIRCName(fNick);
			
			if(userid == null) {
				log.warn("user " + fNick + " could not be found");
				return;
			}
			
			backend.registerActivity(userid);
		} catch (Exception e) {
			log.error("error logging activity", e);
		}
	}

	public boolean isConnected() {
		return bot.isConnected();
	}
	
	@Nonnull
	OsuApiUser getUserOrThrow(IRCBotUser user) throws UserException, SQLException, IOException {
		Integer userId = backend.resolveIRCName(user.getNick());
		
		if(userId == null) {
			String string = IRCBot.getRandomString(8);
			log.error("bot user not resolvable " + string + " name: " + user.getNick());
			
			throw new UserException(lang.unresolvableName(string, user.getNick()));
		}
		
		OsuApiUser apiUser = backend.getUser(userId, 60 * 60 * 1000);
		
		if(apiUser == null) {
			throw new RuntimeException("nickname was resolved, but user not found in api.");
		}
		
		return apiUser;
	}
}

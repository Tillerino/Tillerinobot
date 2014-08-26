package tillerino.tillerinobot;


import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import static org.apache.commons.lang3.StringUtils.*;

import org.apache.commons.lang3.StringUtils;
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
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.events.QuitEvent;
import org.pircbotx.hooks.events.ServerResponseEvent;
import org.pircbotx.hooks.events.UnknownEvent;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta.Estimates;
import tillerino.tillerinobot.BeatmapMeta.OldEstimates;
import tillerino.tillerinobot.BeatmapMeta.PercentageEstimates;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

@Slf4j
@SuppressWarnings(value = { "rawtypes", "unchecked" })
public class IRCBot extends CoreHooks {
	final PircBotX bot;
	final BotBackend backend;
	final private String server;
	final private boolean rememberRecommendations;
	final private boolean silent;
	
	public IRCBot(BotBackend backend, String server, int port, String nickname, String password, String autojoinChannel, boolean rememberRecommendations, boolean silent) {
		this.server = server;
		Builder<PircBotX> configurationBuilder = new Configuration.Builder<PircBotX>()
				.setServer(server, port).setMessageDelay(2000).setName(nickname).addListener(this).setEncoding(Charset.forName("UTF-8"));
		if(password != null) {
				configurationBuilder.setServerPassword(password);
		}
		if(autojoinChannel != null) {
			configurationBuilder.addAutoJoinChannel(autojoinChannel);
		}
		bot = new PircBotX(configurationBuilder.buildConfiguration());
		this.backend = backend;
		this.rememberRecommendations = rememberRecommendations;
		this.silent = silent;
	}

	public void run() throws IOException, IrcException {
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

			BeatmapMeta beatmap = backend.loadBeatmap(beatmapid, mods);

			if (beatmap == null) {
				user.message("I'm sorry, I don't know that map. It might be very new, very hard or simply unranked.");
				return;
			}

			OsuApiUser apiUser = backend.getUser(user.getNick());
			if(apiUser == null) {
				throw new NullPointerException("osu api user was null, but name was already resolved?");
			}
			int hearts = backend.getDonator(apiUser);
			
			if(sendSongInfo(user, beatmap, false, null, hearts)) {
				songInfoCache.put(user.getNick(), beatmapid);
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

				user.message("Something went wrong. If this keeps happening, tell Tillerino to look after incident "
						+ string + ", please.");
				log.error(string + ": fucked up", e);
			}
		} catch (Throwable e1) {
			log.error("holy balls", e1);
		}
	}
	
	static DecimalFormat format = new DecimalFormat("#.##");
	static DecimalFormat noDecimalsFormat = new DecimalFormat("#");
	
	public static boolean sendSongInfo(IRCBotUser user, BeatmapMeta beatmap, boolean formLink, String addition, int hearts) {
		String beatmapName = beatmap.getBeatmap().getArtist() + " - " + beatmap.getBeatmap().getTitle()
				+ " [" + beatmap.getBeatmap().getVersion() + "]";
		if(formLink) {
			beatmapName = "[http://osu.ppy.sh/b/" + beatmap.getBeatmap().getId() + " " + beatmapName + "]";
		}
		
		if(beatmap.getMods() != 0) {
			String mods = "";
			for(Mods mod : Mods.getMods(beatmap.getMods())) {
				if(mod.isEffective()) {
					mods += mod.getShortName();
				}
			}
			beatmapName += " " + mods;
		}

		String estimateMessage = "";
		if(beatmap.getPersonalPP() != null) {
			estimateMessage += "future you: " + beatmap.getPersonalPP() + "pp | ";
		}
		
		Estimates estimates = beatmap.getEstimates();
		
		if (estimates instanceof OldEstimates) {
			OldEstimates oldEstimates = (OldEstimates) estimates;
			
			double community = oldEstimates.getCommunityPP();
			community = Math.round(community * 2) / 2;
			String ppestimate = community % 1 == 0 ? "" + (int) community : "" + format.format(community);

			String cQ = oldEstimates.isTrustCommunity() ? "" : "??";
			String bQ = oldEstimates.isTrustMax() ? "" : "??";
			
			estimateMessage += "community: " + ppestimate + cQ + "pp";
			if(oldEstimates.getMaxPP() != null)
				estimateMessage += " | best: " + oldEstimates.getMaxPP() + bQ + "pp";
		} else if (estimates instanceof PercentageEstimates) {
			PercentageEstimates percentageEstimates = (PercentageEstimates) estimates;

			estimateMessage += "95%: " + noDecimalsFormat.format(percentageEstimates.getPPForAcc(.95)) + "pp";
			estimateMessage += " | 98%: " + noDecimalsFormat.format(percentageEstimates.getPPForAcc(.98)) + "pp";
			estimateMessage += " | 99%: " + noDecimalsFormat.format(percentageEstimates.getPPForAcc(.99)) + "pp";
			estimateMessage += " | 100%: " + noDecimalsFormat.format(percentageEstimates.getPPForAcc(1)) + "pp";
		}
		
		estimateMessage += " | " + secondsToMinuteColonSecond(beatmap.getBeatmap().getTotalLength());
		estimateMessage += " ★ " + format.format(beatmap.getBeatmap().getStarDifficulty());
		estimateMessage += " ♫ " + format.format(beatmap.getBeatmap().getBpm());
		estimateMessage += " AR" + format.format(beatmap.getBeatmap().getApproachRate());

		String heartString = hearts > 0 ? " " + StringUtils.repeat('♥', hearts) : "";

		return user.message(beatmapName + "   " + estimateMessage + (addition != null ? "   " + addition : "") + heartString);
	}

	public static String secondsToMinuteColonSecond(int length) {
		return length / 60 + ":" + StringUtils.leftPad(String.valueOf(length % 60), 2, '0');
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
	public void onMessage(MessageEvent event) throws Exception {
		if(silent)
			return;
		
		if(event.getUser().getNick().equals("Tillerino")) {
			processPrivateMessage(fromIRC(event.getUser()), event.getMessage());
		}
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
					return true;
				} catch (IOException | InterruptedException e) {
					log.error("not sent: " + e.getMessage());
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

		String commandChar = "!";
		if(user.getNick().equals("Tillerino"))
			commandChar = ".";

		if (!message.startsWith(commandChar)) {
			return;
		}

		Semaphore semaphore = perUserLock.getUnchecked(user.getNick());
		if(!semaphore.tryAcquire()) {
			log.warn("concurrent message");
			return;
		}

		try {
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
				user.message("Hi! I'm the robot who killed Tillerino and took over his account."
						+ " Check https://twitter.com/Tillerinobot for status and updates!"
						+ " See https://github.com/Tillerino/Tillerinobot/wiki for commands!");
			} else if(getLevenshteinDistance(message, "faq") <= 1) {
				user.message("See https://github.com/Tillerino/Tillerinobot/wiki/FAQ for FAQ!");
			} else if(getLevenshteinDistance(message.substring(0, Math.min("complain".length(), message.length())), "complain") <= 2) {
				Recommendation lastRecommendation = backend.getLastRecommendation(user.getNick());
				if(lastRecommendation != null && lastRecommendation.beatmap != null) {
					log.warn("COMPLAINT: " + lastRecommendation.beatmap.getBeatmap().getId() + " mods: " + lastRecommendation.mods + ". Recommendation source: " + backend.getCause(user.getNick(), lastRecommendation.beatmap.getBeatmap().getId()));
					user.message("Your complaint has been filed. Tillerino will look into it when he can.");
				}
			} else if(isRecommend) {
				String[] remaining = message.split(" ");
				
				boolean isGamma = false;
				
				for (int i = 0; i < remaining.length; i++) {
					if(remaining[i].length() == 0)
						continue;
					if(getLevenshteinDistance(remaining[i], "nomod") <= 2) {
						remaining[i] = "nomod";
						continue;
					}
					if(getLevenshteinDistance(remaining[i], "relax") <= 2) {
						remaining[i] = "relax";
						continue;
					}
					if(getLevenshteinDistance(remaining[i], "gamma") <= 2) {
						remaining[i] = "gamma";
						isGamma = true;
						continue;
					}
					if(isGamma && remaining[i].equals("dt") || remaining[i].equals("hr")) {
						continue;
					}
					throw new UserException("I don't know what \"" + remaining[i] + "\" is supposed to mean. Try !help if you need some pointers.");
				}
				
				Recommendation recommendation = backend.loadRecommendation(user.getNick(), message);

				if(recommendation.beatmap == null) {
					user.message("I'm sorry, there was this beautiful sequence of ones and zeros and I got distracted. What did you want again?");
					log.error("unknow recommendation occurred");
					return;
				}
				String addition = null;
				if(recommendation.mods < 0) {
					addition = "Try this map with some mods!";
				}
				if(recommendation.mods > 0 && recommendation.beatmap.getMods() == 0) {
					addition = "Try this map with " + Mods.getShortNames(Mods.getMods(recommendation.mods));
				}
				
				OsuApiUser apiUser = backend.getUser(user.getNick());
				if(apiUser == null) {
					throw new NullPointerException("osu api user was null, but name was already resolved?");
				}
				int hearts = backend.getDonator(apiUser);
				
				if(sendSongInfo(user, recommendation.beatmap, true, addition, hearts)) {
					songInfoCache.put(user.getNick(), recommendation.beatmap.getBeatmap().getId());
					if(rememberRecommendations) {
						backend.saveGivenRecommendation(user.getNick(), recommendation.beatmap.getBeatmap().getId());
					}
				}

			} else if(message.startsWith("with ")) {
				Integer lastSongInfo = songInfoCache.getIfPresent(user.getNick());
				if(lastSongInfo == null) {
					throw new UserException("I don't remember you getting any song info...");
				}
				message = message.substring(5);
				
				Long mods = getMods(message);
				if(mods == null) {
					throw new UserException("those mods don't look right. mods can be any combination of DT HR HD HT EZ NC FL SO NF. Combine them without any spaces or special chars. Example: !with HDHR, !with DTEZ");
				}
				if(mods == 0)
					return;
				BeatmapMeta beatmap = backend.loadBeatmap(lastSongInfo, mods);
				if(beatmap.getMods() == 0) {
					throw new UserException("Sorry, I can't provide information for those mods at this time.");
				}
				
				int hearts = 0;
				OsuApiUser apiUser = backend.getUser(user.getNick());
				if(apiUser != null) {
					hearts = backend.getDonator(apiUser);
				}
				
				sendSongInfo(user, beatmap, false, null, hearts);
			} else {
				throw new UserException("unknown command " + message
						+ ". type !help if you need help!");
			}
		} catch (Throwable e) {
			handleException(user, e);
		} finally {
			semaphore.release();
		}
	}

	private Long getMods(String message) throws UserException {
		long mods = 0;
		for(int i = 0; i < message.length(); i+=2) {
			try {
				Mods mod = Mods.fromShortName(message.substring(i, i + 2).toUpperCase());
				if(mod.isEffective()) {
					if(mod == Mods.Nightcore) {
						mods |= Mods.getMask(Mods.DoubleTime);
					} else {
						mods |= Mods.getMask(mod);
					}
				}
			} catch(Exception e) {
				return null;
			}
		}
		return mods;
	}

	private void checkVersionInfo(final IRCBotUser user) throws SQLException, UserException {
		int userVersion = backend.getLastVisitedVersion(user.getNick());
		if(userVersion < currentVersion) {
			user.message(versionMessage);
			backend.setLastVisitedVersion(user.getNick(), currentVersion);
		}
	}
	
	void shutDown() {
		bot.sendIRC().quitServer();
	}
	
	class Pinger {
		volatile String pingMessage = null;
		volatile CountDownLatch pingLatch = null;
		final AtomicLong pingGate = new AtomicLong();
		
		void ping() throws IOException, InterruptedException {
			long time = System.currentTimeMillis();
			
			if(pingGate.get() > System.currentTimeMillis()) {
				throw new IOException("ping gate closed");
			}
			
			synchronized (this) {
				pingLatch = new CountDownLatch(1);
				pingMessage = getRandomString(16);
			}

			Utils.sendRawLineToServer(bot, "PING " + pingMessage);
			
			if(!pingLatch.await(10, TimeUnit.SECONDS)) {
				pingGate.set(System.currentTimeMillis() + 60000);
				throw new IOException("ping timed out");
			}
			
			long ping = System.currentTimeMillis() - time;
			
			if(ping > 1500) {
				pingGate.set(System.currentTimeMillis() + 60000);
				throw new IOException("death ping: " + ping);
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
	
	static final int currentVersion = 5;
	static final String versionMessage = "I made recommendations a little easier to get some of the farmy nature of the early versions back. Let me know if they've become too easy, too hard, or *just right* @Tillerinobot. They should now also consider a little more than just your top 10 scores depending on how steep your top pp contributors fall off.";
	
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
		
		welcomeIfDonator(event.getUser());
		
		exec.submit(new Runnable() {
			public void run() {
				backend.registerActivity(fNick);
			}
		});
	}
	
	void welcomeIfDonator(User user) {
		try {
			OsuApiUser apiUser = backend.getUser(user.getNick());
			
			if(apiUser == null)
				return;
			
			if(backend.getDonator(apiUser) > 0) {
				// this is a donator, let's welcome them!
				
				IRCBotUser botUser = fromIRC(user);
				
				long lastActivity = backend.getLastActivity(apiUser);
				
				if(lastActivity > System.currentTimeMillis() - 60 * 1000) {
					botUser.message("beep boop");
				} else if(lastActivity > System.currentTimeMillis() - 60 * 60 * 1000) {
					botUser.message("Welcome back, " + apiUser.getUsername() + ".");
					checkVersionInfo(botUser);
				} else if(lastActivity > System.currentTimeMillis() - 24 * 60 * 60 * 1000) {
					String[] messages = {
							"you look like you want a recommendation.",
							"how nice to see you! :)",
							"my favourite human. (Don't tell the other humans!)",
							"what a pleasant surprise! ^.^",
							"I was hoping you'd show up. All the other humans are lame, but don't tell them I said that! :3",
							"what do you feel like doing today?",
					};
					
					Random random = new Random();
					
					String message = messages[random.nextInt(messages.length)];
					
					botUser.message(apiUser.getUsername() + ", " + message);
					checkVersionInfo(botUser);
				} else if(lastActivity < System.currentTimeMillis() - 7l * 24 * 60 * 60 * 1000) {
					botUser.message(apiUser.getUsername() + "...");
					botUser.message("...is that you? It's been so long!");
					checkVersionInfo(botUser);
					botUser.message("It's good to have you back. Can I interest you in a recommendation?");
				}
			}
		} catch (Exception e) {
			log.error("error welcoming potential donator", e);
		}
	}

	@Override
	public void onPart(PartEvent event) throws Exception {
		final String fNick = event.getUser().getNick();
		
		exec.submit(new Runnable() {
			public void run() {
				backend.registerActivity(fNick);
			}
		});
	}
	
	@Override
	public void onQuit(QuitEvent event) throws Exception {
		final String fNick = event.getUser().getNick();
		
		exec.submit(new Runnable() {
			public void run() {
				backend.registerActivity(fNick);
			}
		});
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
				
				final String fNick = nick;
				
				exec.submit(new Runnable() {
					public void run() {
						backend.registerActivity(fNick);
					}
				});
			}
			
			System.out.println("processed user list event");
		} else {
			super.onServerResponse(event);
		}
	}
}

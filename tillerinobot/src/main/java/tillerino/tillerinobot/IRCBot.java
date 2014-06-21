package tillerino.tillerinobot;


import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

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
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.events.UnknownEvent;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Slf4j
@SuppressWarnings(value = { "rawtypes", "unchecked" })
public class IRCBot extends CoreHooks {
	final PircBotX bot;
	final BotBackend backend;
	final private String server;
	final private boolean rememberRecommendations;
	
	public IRCBot(BotBackend backend, String server, int port, String nickname, String password, String autojoinChannel, boolean rememberRecommendations) {
		this.server = server;
		Builder<PircBotX> configurationBuilder = new Configuration.Builder<PircBotX>()
				.setServer(server, port).setMessageDelay(2000).setName(nickname).addListener(this);
		if(password != null) {
				configurationBuilder.setServerPassword(password);
		}
		if(autojoinChannel != null) {
			configurationBuilder.addAutoJoinChannel(autojoinChannel);
		}
		bot = new PircBotX(configurationBuilder.buildConfiguration());
		this.backend = backend;
		this.rememberRecommendations = rememberRecommendations;
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

		try {
			Semaphore semaphore = perUserLock.getUnchecked(user.getNick());
			if(!semaphore.tryAcquire()) {
				log.warn("concurrent action");
				return;
			}

			try {
				Matcher m = npPattern.matcher(message);

				if (!m.matches()) {
					log.error("no match: " + message);
					return;
				}

				int beatmapid = Integer.valueOf(m.group(1));

				BeatmapMeta beatmap = backend.loadBeatmap(beatmapid);

				if (beatmap == null) {
					user.message("I'm sorry, I don't know that map. It might be very new, very hard or simply unranked.");
					return;
				}

				sendSongInfo(user, beatmap, false, null);
			} finally {
				semaphore.release();
			}
		} catch (Exception e) {
			handleException(user, message, e);
			return;
		}
	}

	private void handleException(IRCBotUser user, String message, Throwable e) {
		if(e instanceof ExecutionException) {
			e = e.getCause();
		}
		if(e instanceof UserException) {
			user.message(e.getMessage());
		} else {
			String string = getRandomString(6);

			user.message("Something went wrong. If this keeps happening, tell Tillerino to look after incident "
					+ string + ", please.");
			log.error(string + ": fucked up", e);
		}
	}
	
	static DecimalFormat format = new DecimalFormat("#.##");

	private static boolean sendSongInfo(IRCBotUser user, BeatmapMeta beatmap, boolean formLink, String addition) {
		String beatmapName = beatmap.getArtist() + " - " + beatmap.getTitle()
				+ " [" + beatmap.getVersion() + "]";
		if(formLink) {
			beatmapName = "[http://osu.ppy.sh/b/" + beatmap.getBeatmapid() + " " + beatmapName + "]";
		}

		double community = beatmap.getCommunityPP();
		community = Math.round(community * 2) / 2;
		String ppestimate = community % 1 == 0 ? "" + (int) community : "" + format.format(community);

		String cQ = beatmap.isTrustCommunity() ? "" : "??";
		String bQ = beatmap.isTrustMax() ? "" : "??";
		
		String estimateMessage = "community: "
				+ ppestimate
				+ cQ + " pp"
				+ (beatmap.getMaxPP() != null ? (" | best: " + beatmap.getMaxPP() + bQ + " pp")
						: "") + " | star difficulty: " + format.format(beatmap.getStarDifficulty());


		return user.message(beatmapName + "   " + estimateMessage + (addition != null ? "   " + addition : ""));
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
		if(event.getUser().getNick().equals("Tillerino")) {
			processPrivateMessage(fromIRC(event.getUser()), event.getMessage());
		}
	}

	@Override
	public void onPrivateMessage(PrivateMessageEvent event) throws Exception {
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
			message = message.substring(1);

			if(message.equalsIgnoreCase("help")) {
				user.message("Hi! I'm the robot who killed Tillerino and took over his account."
						+ " Check https://twitter.com/Tillerinobot for status and updates!"
						+ " See https://github.com/Tillerino/Tillerinobot/wiki for commands!");
			} else if(message.equalsIgnoreCase("faq")) {
				user.message("See https://github.com/Tillerino/Tillerinobot/wiki/FAQ for FAQ!");
			} else if(message.startsWith("complain")) {
				Recommendation lastRecommendation = backend.getLastRecommendation(user.getNick());
				if(lastRecommendation != null && lastRecommendation.beatmap != null) {
					log.warn("COMPLAINT: " + lastRecommendation.beatmap.getBeatmapid() + (lastRecommendation.mods ? " with mods" : "") + ". Recommendation source: " + backend.getCause(user.getNick(), lastRecommendation.beatmap.getBeatmapid()));
					user.message("Your complaint has been filed. Tillerino will look into it when he can.");
				}
			} else if(message.equals("recommend") || message.equals("recommend beta") || message.equals("recommend nomod")) {
				try {
					Recommendation result = backend.loadRecommendation(user.getNick(), message);

					if(result.beatmap == null) {
						user.message("I'm sorry, there was this beautiful sequence of ones and zeros and I got distracted. What did you want again?");
						log.error("unknow recommendation occurred");
						return;
					}
					String addition = null;
					if(result.mods) {
						addition = "Try this map with some mods! " + (result.beatmap.getMaxPP() != null && result.beatmap.getMaxPP() < 75 ? "Maybe DT?" : "I think you're better at finding the right ones than me.");
					}
					if(sendSongInfo(user, result.beatmap, true, addition)) {
						if(rememberRecommendations) {
							backend.saveGivenRecommendation(user.getNick(), result.beatmap.getBeatmapid());
						}
					}
				} catch (Exception e) {
					handleException(user, message, e);
				}
			} else {
				user.message("unknown command " + message
						+ ". type !help if you need help!");
			}
		} finally {
			semaphore.release();
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
			
			log.info("ping: " + ping);
			
			if(ping > 1000) {
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
		super.onEvent(event);
	}
	
	@Override
	public void onUnknown(UnknownEvent event) throws Exception {
		System.out.println(event.getLine());
		
		pinger.handleUnknownEvent(event);
	}
}

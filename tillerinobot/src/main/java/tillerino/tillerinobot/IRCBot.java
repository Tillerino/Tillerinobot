package tillerino.tillerinobot;


import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.CoreHooks;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Slf4j
@SuppressWarnings(value = { "rawtypes", "unchecked" })
public class IRCBot extends CoreHooks {
	static class MessageRateLimiter implements Runnable {
		TransferQueue<Object> queue = new LinkedTransferQueue<>();
		Semaphore semaphore = new Semaphore(1, true);
		
		@Override
		public void run() {
			for(;;) {
				try {
					queue.transfer(new Object());
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					return;
				}
			}
		}
		
		void ensureMessageRateLimit() throws InterruptedException {
			semaphore.acquire();
			try {
				queue.take();
			} finally {
				semaphore.release();
			}
		}
	}
	
	final PircBotX bot;

	BotBackend backend;
	
	public IRCBot(BotBackend backend, String server, int port, String nickname, String password, String autojoinChannel) {
		Builder<PircBotX> configurationBuilder = new Configuration.Builder<PircBotX>()
				.setServer(server, port).setName(nickname).addListener(this);
		if(password != null) {
				configurationBuilder.setServerPassword(password);
		}
		if(autojoinChannel != null) {
			configurationBuilder.addAutoJoinChannel(autojoinChannel);
		}
		bot = new PircBotX(configurationBuilder.buildConfiguration());
		this.backend = backend;
	}

	public void run() throws IOException, IrcException {
		Thread messageRateLimiterPushThread = new Thread(messageRateLimiter);
		messageRateLimiterPushThread.setDaemon(true);
		messageRateLimiterPushThread.start();
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
		log.info(user.getNick() + " action: " + message);

		try {
			Matcher m = npPattern.matcher(message);

			if (!m.matches()) {
				log.error("no match: " + message);
				return;
			}
			
			Semaphore semaphore = perUserLock.getUnchecked(user.getNick());
			if(!semaphore.tryAcquire()) {
				log.warn("concurrent action from " + user.getNick() + ": " + message);
				return;
			}

			try {

				int beatmapid = Integer.valueOf(m.group(1));

				BeatmapMeta beatmap = backend.loadBeatmap(beatmapid);

				if (beatmap != null) {
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
			log.error(string + ": fucked up responding to " + user.getNick() + message,
					e);
		}
	}

	private static void sendSongInfo(IRCBotUser user, BeatmapMeta beatmap, boolean formLink, String addition) {
		String beatmapName = beatmap.getArtist() + " - " + beatmap.getTitle()
				+ " [" + beatmap.getVersion() + "]";
		if(formLink) {
			beatmapName = "[http://osu.ppy.sh/b/" + beatmap.getBeatmapid() + " " + beatmapName + "]";
		}

		double community = beatmap.getCommunityPP();
		community = Math.round(community * 2) / 2;
		String ppestimate = community % 1 == 0 ? "" + (int) community : "" + community;

		String cQ = beatmap.isTrustCommunity() ? "" : "??";
		String bQ = beatmap.isTrustMax() ? "" : "??";
		
		String estimateMessage = "community: "
				+ ppestimate
				+ cQ + " pp"
				+ (beatmap.getMaxPP() != null ? (" | best: " + beatmap.getMaxPP() + bQ + " pp")
						: "") + " | star difficulty: " + beatmap.getStarDifficulty();


		user.message(beatmapName + "   " + estimateMessage + (addition != null ? "   " + addition : ""));
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
	
	MessageRateLimiter messageRateLimiter = new MessageRateLimiter();
	
	IRCBotUser fromIRC(final User user) {
		return new IRCBotUser() {
			
			@Override
			public void message(String msg) {
				try {
					messageRateLimiter.ensureMessageRateLimit();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				user.send().message(msg);
				log.info("to " + user.getNick() + ": " + msg);
			}
			
			@Override
			public String getNick() {
				return user.getNick();
			}
		};
	}
	
	void processPrivateMessage(final IRCBotUser user, String message) throws IOException {
		log.info(user.getNick() + ": " + message);

		String commandChar = "!";
		if(user.getNick().equals("Tillerino"))
			commandChar = ".";

		if (!message.startsWith(commandChar)) {
			return;
		}

		Semaphore semaphore = perUserLock.getUnchecked(user.getNick());
		if(!semaphore.tryAcquire()) {
			log.warn("concurrent message from " + user.getNick() + ": " + message);
			return;
		}

		try {
			message = message.substring(1);

			if(message.startsWith("help")) {
				user.message("Hi! I'm the robot who killed Tillerino and took over his account. check for status and updates at https://twitter.com/Tillerinobot");
				user.message("I respond to the following commands.");
				user.message(" /np: I tell you community/best pp for the map you're listening to.");
				user.message("!recommend: I will suggest a map that you might like.");
				user.message("!recommend nomod: I will suggest a map that you might like. I'll avoid mods.");
				user.message("!recommend beta: Preview a new version of the recommendation algorithm.");
				user.message("!complain: Complain about the last recommendation that you got.");
			} else if(message.startsWith("recommend")) {
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
					sendSongInfo(user, result.beatmap, true, addition);
				} catch (Exception e) {
					handleException(user, message, e);
				}
			} else if(message.startsWith("complain")) {
				Recommendation lastRecommendation = backend.getLastRecommendation(user.getNick());
				if(lastRecommendation != null) {
					log.warn("COMPLAINT: " + lastRecommendation.beatmap.getBeatmapid() + (lastRecommendation.mods ? " with mods" : "") + ". Recommendation source: " + backend.getCause(user.getNick(), lastRecommendation.beatmap.getBeatmapid()));
					user.message("You complaint has been filed. Tillerino will look into it when he can.");
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
}

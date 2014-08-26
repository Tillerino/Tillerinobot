package tillerino.tillerinobot;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;
import org.pircbotx.User;
import org.pircbotx.output.OutputUser;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.IRCBot.Pinger;

public class IRCBotTest {
	public static final class LastResponseUser implements IRCBotUser {
		String response;

		@Override
		public boolean message(String msg) {
			this.response = msg;
			return true;
		}

		@Override
		public String getNick() {
			return "dummyNick";
		}
	}

	public static final class FirstResponseUser implements IRCBotUser {
		String response;

		@Override
		public boolean message(String msg) {
			if(this.response == null)
				this.response = msg;
			return true;
		}

		@Override
		public String getNick() {
			return "dummyNick";
		}
	}

	static class TestBackend implements BotBackend {
		public class Derpommendation implements BeatmapMeta {
			OsuApiBeatmap beatmap = new OsuApiBeatmap();
			public Derpommendation() {
				beatmap.id = 411134170;
				beatmap.artist = "artist";
				beatmap.starDifficulty = 3.3;
				beatmap.version = "version";
				beatmap.bpm = 175;
				beatmap.approachRate = 8.5;
				beatmap.totalLength = 143;
				beatmap.approved = 2;
			}
			
			@Override
			public OsuApiBeatmap getBeatmap() {
				return beatmap;
			}
			
			
			
			@Override
			public Estimates getEstimates() {
				return new OldEstimates() {
					@Override
					public boolean isTrustMax() {
						return true;
					}

					@Override
					public boolean isTrustCommunity() {
						return true;
					}

					@Override
					public Integer getMaxPP() {
						return 200;
					}

					@Override
					public double getCommunityPP() {
						return 100;
					}
				};
			}

			@Override
			public Integer getPersonalPP() {
				if(getBeatmap().getTitle().equals("title"))
					return 100;
				return null;
			}

			@Override
			public long getMods() {
				return 0;
			}
		}

		@Override
		public String getCause(String nick, int beatmapid) throws IOException {
			return "whatever";
		}

		Recommendation normalRecommendation = new Recommendation();
		Recommendation nomodRecommendation = new Recommendation();
		Recommendation relaxRecommendation = new Recommendation();

		public TestBackend() {
			normalRecommendation.beatmap = new Derpommendation();
			normalRecommendation.beatmap.getBeatmap().title = "title";
			normalRecommendation.mods = -1;
			
			nomodRecommendation.beatmap = new Derpommendation();
			nomodRecommendation.beatmap.getBeatmap().title = "nomod";
			nomodRecommendation.mods = 0;
			
			relaxRecommendation.beatmap = new Derpommendation();
			relaxRecommendation.beatmap.getBeatmap().title = "relax";
			relaxRecommendation.mods = 0;
		}

		@Override
		public Recommendation getLastRecommendation(String nick) {
			return normalRecommendation;
		}

		@Override
		public BeatmapMeta loadBeatmap(int beatmapid, long mods) {
			return normalRecommendation.beatmap;
		}

		@Override
		public Recommendation loadRecommendation(String userNick, String message)
				throws SQLException, IOException, UserException {
			if(message.equals(""))
				return normalRecommendation;
			if(message.equals("relax nomod"))
				return nomodRecommendation;
			if(message.equals("relax"))
				return relaxRecommendation;
			throw new IllegalArgumentException();
		}

		@Override
		public void saveGivenRecommendation(String nick, int beatmapid)
				throws SQLException {
			
		}
		
		int versionVisited = -1;

		@Override
		public int getLastVisitedVersion(String nick) throws SQLException {
			return versionVisited;
		}

		@Override
		public void setLastVisitedVersion(String nick, int version)
				throws SQLException {
			versionVisited = version;
		}

		@Override
		public void registerActivity(String nick) {
			// no
		}

		@Override
		public OsuApiUser getUser(String ircNick) throws SQLException,
				IOException {
			return new OsuApiUser();
		}

		@Override
		public long getLastActivity(OsuApiUser user) throws SQLException {
			return 0;
		}

		@Override
		public int getDonator(OsuApiUser user) throws SQLException,
				IOException {
			return 0;
		}
	}
	
	IRCBot bot = new IRCBot(new TestBackend(), "nothing", 456, "nobody", null, null, false, false);
	
	public static String syso(String s) {
		System.out.println(s);
		return s;
	}
	
	@Test
	public void testWrongStrings() throws IOException {
		
		assertEquals(getResponse(bot, "!recommend", false), IRCBot.versionMessage);
		
		assertNotEquals(getResponse(bot, "!recommend", false), IRCBot.versionMessage);
		
		assertTrue(syso(getResponse(bot, "!recommend", true)).contains("artist - title [version]"));
		
		assertTrue(getResponse(bot, "!r", true).contains("artist - title [version]"));
		
		assertTrue(getResponse(bot, "!r relax", true).contains("artist - relax [version]"));
		
		assertTrue(getResponse(bot, "!r relax nomod", true).contains("artist - nomod [version]"));
		
		assertTrue(getResponse(bot, "!recomend", true).contains("artist - title [version]"));
		
		assertTrue(getResponse(bot, "!reccomend", true).contains("artist - title [version]"));
		
		assertTrue(getResponse(bot, "!complian", true).contains("complaint"));
		
		assertTrue(getResponse(bot, "!recccomend", true).contains("!help"));
		
		assertTrue(getResponse(bot, "!halp", true).contains("twitter"));
		
		assertTrue(getResponse(bot, "!feq", true).contains("FAQ"));
	}
	
	@Test
	public void testPersonalPP() throws IOException {
		assertTrue(getResponse(bot, "!recommend", true).contains("future you: 100pp"));
		assertFalse(getResponse(bot, "!recommend relax", true).contains("future you"));
		
		System.out.println(getResponse(bot, "!r", true));
	}

	public static String getResponse(IRCBot bot, String command, boolean lastResponse) throws IOException {
		if(lastResponse) {
			LastResponseUser user = new LastResponseUser();
			bot.processPrivateMessage(user, command);
			return user.response;
		}
		FirstResponseUser user = new FirstResponseUser();
		bot.processPrivateMessage(user, command);
		return user.response;
	}

	@Test
	public void testWelcomeIfDonator() throws Exception {
		BotBackend backend = mock(BotBackend.class);
		
		OsuApiUser osuApiUser = mock(OsuApiUser.class);
		when(osuApiUser.getUsername()).thenReturn("TheDonator");
		
		when(backend.getUser(anyString())).thenReturn(osuApiUser);
		when(backend.getDonator(any(OsuApiUser.class))).thenReturn(1);
		
		OutputUser outputUser = mock(OutputUser.class);
		User user = mock(User.class);
		when(user.send()).thenReturn(outputUser);
		when(user.getNick()).thenReturn("TheDonator");
		
		IRCBot bot = getTestBot(backend);
		
		when(backend.getLastActivity(any(OsuApiUser.class))).thenReturn(System.currentTimeMillis() - 1000);
		bot.welcomeIfDonator(user);
		verify(outputUser).message("beep boop");

		when(backend.getLastActivity(any(OsuApiUser.class))).thenReturn(System.currentTimeMillis() - 100000);
		bot.welcomeIfDonator(user);
		verify(outputUser).message("Welcome back, TheDonator.");
	}
	
	IRCBot getTestBot(BotBackend backend) {
		IRCBot ircBot = new IRCBot(backend, "server", 1, "botuser", null, null, false, false);
		ircBot.pinger = mock(Pinger.class);
		return ircBot;
	}
}

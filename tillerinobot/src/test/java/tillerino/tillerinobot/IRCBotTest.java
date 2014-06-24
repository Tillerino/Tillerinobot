package tillerino.tillerinobot;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;
import org.tillerino.osuApiModel.OsuApiBeatmap;

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
			}
			
			@Override
			public OsuApiBeatmap getBeatmap() {
				return beatmap;
			}
			
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
			normalRecommendation.mods = true;
			
			nomodRecommendation.beatmap = new Derpommendation();
			nomodRecommendation.beatmap.getBeatmap().title = "nomod";
			nomodRecommendation.mods = false;
			
			relaxRecommendation.beatmap = new Derpommendation();
			relaxRecommendation.beatmap.getBeatmap().title = "relax";
			relaxRecommendation.mods = false;
		}

		@Override
		public Recommendation getLastRecommendation(String nick) {
			return normalRecommendation;
		}

		@Override
		public BeatmapMeta loadBeatmap(int beatmapid) {
			return normalRecommendation.beatmap;
		}

		@Override
		public Recommendation loadRecommendation(String userNick, String message)
				throws SQLException, IOException, UserException {
			if(message.equals("recommend"))
				return normalRecommendation;
			if(message.equals("recommend relax nomod"))
				return nomodRecommendation;
			if(message.equals("recommend relax"))
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
	}

	@Test
	public void testWrongStrings() throws IOException {
		IRCBot bot = new IRCBot(new TestBackend(), "nothing", 456, "nobody", null, null, false);
		
		assertEquals(getResponse(bot, "!recommend", false), IRCBot.versionMessage);
		
		assertNotEquals(getResponse(bot, "!recommend", false), IRCBot.versionMessage);
		
		assertTrue(getResponse(bot, "!recommend", true).contains("artist - title [version]"));
		
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
}

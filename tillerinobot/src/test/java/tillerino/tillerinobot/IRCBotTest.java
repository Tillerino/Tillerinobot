package tillerino.tillerinobot;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;
import org.tillerino.osuApiModel.OsuApiBeatmap;

public class IRCBotTest {
	public static final class DummyUser implements IRCBotUser {
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
		Recommendation betaRecommendation = new Recommendation();

		public TestBackend() {
			normalRecommendation.beatmap = new Derpommendation();
			normalRecommendation.beatmap.getBeatmap().title = "title";
			normalRecommendation.mods = true;
			
			nomodRecommendation.beatmap = new Derpommendation();
			nomodRecommendation.beatmap.getBeatmap().title = "nomod";
			nomodRecommendation.mods = false;
			
			betaRecommendation.beatmap = new Derpommendation();
			betaRecommendation.beatmap.getBeatmap().title = "beta";
			betaRecommendation.mods = false;
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
			if(message.equals("recommend nomod"))
				return nomodRecommendation;
			if(message.equals("recommend beta"))
				return betaRecommendation;
			throw new IllegalArgumentException();
		}

		@Override
		public void saveGivenRecommendation(String nick, int beatmapid)
				throws SQLException {
			
		}
	}

	@Test
	public void testWrongStrings() throws IOException {
		IRCBot bot = new IRCBot(new TestBackend(), "nothing", 456, "nobody", null, null, false);
		
		assertTrue(getResponse(bot, "!recommend").contains("artist - title [version]"));
		
		assertTrue(getResponse(bot, "!r").contains("artist - title [version]"));
		
		assertTrue(getResponse(bot, "!r beta").contains("artist - beta [version]"));
		
		assertTrue(getResponse(bot, "!r nomod").contains("artist - nomod [version]"));
		
		assertTrue(getResponse(bot, "!recomend").contains("artist - title [version]"));
		
		assertTrue(getResponse(bot, "!reccomend").contains("artist - title [version]"));
		
		assertTrue(getResponse(bot, "!complian").contains("complaint"));
		
		assertTrue(getResponse(bot, "!recccomend").contains("!help"));
		
		assertTrue(getResponse(bot, "!halp").contains("twitter"));
		
		assertTrue(getResponse(bot, "!feq").contains("FAQ"));
	}
	
	public static String getResponse(IRCBot bot, String command) throws IOException {
		DummyUser user = new DummyUser();
		bot.processPrivateMessage(user, command);
		return user.response;
	}
}

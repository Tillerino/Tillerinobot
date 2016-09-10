package tillerino.tillerinobot;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.Getter;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.OsuApiScore;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.RecommendationsManager.BareRecommendation;
import tillerino.tillerinobot.RecommendationsManager.Model;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.diff.CBeatmap;
import tillerino.tillerinobot.diff.PercentageEstimates;
import tillerino.tillerinobot.lang.Language;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * <p>
 * Backend implementation for the purposes of testing the Frontend.
 * </p>
 * 
 * <p>
 * Everything that the backend does, is saved in the file tillerinobot-db.json
 * in the current working directory. You can edit the file if you want to test
 * something specific.
 * </p>
 * 
 * <p>
 * Beatmaps are randomly generated while trying to look realistic (consistency
 * between star diff, version, and pp). The pp curve is approximates with acc^5.
 * </p>
 * 
 * <p>
 * Recommendations just look for the closest candidates to (user's pp/20) with
 * 98% acc while respecting selected mods.
 * </p>
 * 
 * @author Tillerino
 */
public class TestBackend implements BotBackend {
	public static class FakePercentageEstimates implements PercentageEstimates {
		private final long mods;
		private final double fPp;

		public FakePercentageEstimates(long mods, double fPp) {
			this.mods = mods;
			this.fPp = fPp;
		}

		@Override
		public boolean isShaky() {
			return false;
		}

		@Override
		public double getPPForAcc(double acc) {
			return fPp * Math.pow((double) acc, 5);
		}

		@Override
		public long getMods() {
			return mods;
		}

		@Override
		public Double getStarDiff() {
			return Math.log(fPp / 8) / Math.log(2);
		}

		@Override
		public double getPP(double acc, int combo, int misses) {
			return getPPForAcc(acc);
		}
		
		@Override
		public int getAllObjectsCount() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public CBeatmap getBeatmap() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isOppaiOnly() {
			return false;
		}
		
		@Override
		public boolean isRanked() {
			return true;
		}
	}

	static class User {
		int lastVisistedVersion = 0;
		OsuApiUser apiUser;
		boolean isDonator = false;
		long lastActivity;
		String options;
	}

	static class Database {
		Map<Integer, User> users = new HashMap<>();

		Map<String, Integer> userNames = new HashMap<>();

		Map<Integer, OsuApiBeatmap> beatmaps = new HashMap<>();

		Map<BeatmapWithMods, FakePercentageEstimates> estimates = new HashMap<>();
	}

	Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls()
			.enableComplexMapKeySerialization().create();

	boolean serialize;
	
	@Getter
	Map<Integer, Integer> setIds = new HashMap<>();

	@Inject
	public TestBackend(
			@Named("tillerinobot.test.persistentBackend") boolean serialize) {
		this.serialize = serialize;
		if(serialize) {
			try (Reader reader = new InputStreamReader(new BufferedInputStream(
					new FileInputStream("tillerinobot-db.json")))) {
				database = gson.fromJson(reader, Database.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				TestBackend.class.getResourceAsStream("/beatmapIds.txt")))) {
			for (String line; (line = reader.readLine()) != null;) {
				String[] s = line.split("\t");
				setIds.put(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void writeDatabase() {
		if(!serialize)
			return;
		
		try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(
				new FileOutputStream("tillerinobot-db.json")))) {
			gson.toJson(database, writer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Database database = new Database();

	@Override
	public BeatmapMeta loadBeatmap(int beatmapid, final long mods, Language lang)
			throws SQLException, IOException, UserException {
		OsuApiBeatmap beatmap = getBeatmap(beatmapid);

		BeatmapWithMods entry = new BeatmapWithMods(beatmapid, mods);

		FakePercentageEstimates estimates = database.estimates.get(entry);

		if (estimates == null) {
			double pp = Math.pow(2, beatmap.getStarDifficulty()) * 8;

			if (Mods.DoubleTime.is(mods)) {
				pp *= 2.5;
			}
			if (Mods.HardRock.is(mods)) {
				pp *= 1.3;
			}
			if (Mods.Hidden.is(mods)) {
				pp *= 1.1;
			}

			final double fPp = pp;

			estimates = new FakePercentageEstimates(mods, fPp);
		}

		return new BeatmapMeta(beatmap, null, estimates);
	}

	public OsuApiBeatmap getBeatmap(int beatmapid) {
		Random rand = new Random(beatmapid);
		OsuApiBeatmap beatmap = database.beatmaps.get(beatmapid);
		if (beatmap == null) {
			beatmap = new OsuApiBeatmap();
			beatmap.setBeatmapId(beatmapid);
			if (setIds.containsKey(beatmapid)) {
				beatmap.setSetId(setIds.get(beatmapid));
			}
			{
				// ARTIST
				String[] artists = { "Hatsune Miku", "IOSYS", "Nightcore",
						"DragonForce", "ClariS" };
				beatmap.setArtist(artists[beatmapid % artists.length]);
			}
			beatmap.setTitle("Beatmap " + beatmapid);
			{
				// VERSION AND DIFFICULTY
				String[] versions = { "Easy", "Normal", "Hard", "Hyper",
						"Insane", "Another", "Extra" };
				int diff = beatmapid % versions.length;
				beatmap.setVersion(versions[diff]);

				beatmap.setStarDifficulty(diff + rand.nextDouble());
				beatmap.setTotalLength((int) (30 + Math.pow(rand.nextDouble(),
						3) * 600));
				beatmap.setApproachRate(5 + Math.min(4, diff)
						+ (int) (rand.nextDouble() + .5));
				beatmap.setCircleSize(diff + 1);
				beatmap.setBpm(50 * Math.pow(2, diff * .4 + rand.nextDouble()));
			}
		}
		return beatmap;
	}

	public void hintUser(String username, boolean isDonator, int rank, double pp)
			throws SQLException, IOException {
		User user;
		boolean write = false;
		if (!database.userNames.containsKey(username)) {
			write = true;
			int userid = database.userNames.size() + 1;
			user = new User();
			user.apiUser = new OsuApiUser();
			user.apiUser.setUserId(userid);
			database.userNames.put(username, userid);
			database.users.put(userid, user);
		}
		
		user = database.users.get(database.userNames.get(username));
		
		user.isDonator = isDonator;
		user.apiUser.setUserName(username);
		user.apiUser.setPp(pp);
		user.apiUser.setRank(rank);
		
		if (write) {
			writeDatabase();
		}
	}

	@Override
	public int getLastVisitedVersion(String nick) throws SQLException,
			UserException {
		return database.users.get(database.userNames.get(nick)).lastVisistedVersion;
	}

	@Override
	public void setLastVisitedVersion(String nick, int version)
			throws SQLException {
		database.users.get(database.userNames.get(nick)).lastVisistedVersion = version;
		writeDatabase();
	}

	@Override
	public OsuApiUser getUser(int userid, long maxAge) throws SQLException,
			IOException {
		return database.users.get(userid).apiUser;
	}

	@Override
	public void registerActivity(int userid) throws SQLException {
		database.users.get(userid).lastActivity = System.currentTimeMillis();
		writeDatabase();
	}

	@Override
	public long getLastActivity(OsuApiUser user) throws SQLException {
		return database.users.get(user.getUserId()).lastActivity;
	}

	@Override
	public int getDonator(OsuApiUser user) throws SQLException, IOException {
		return database.users.get(user.getUserId()).isDonator ? 1 : 0;
	}

	@Override
	public Collection<BareRecommendation> loadRecommendations(int userid,
			Collection<Integer> exclude, Model model, boolean nomod,
			long requestMods) throws SQLException, IOException, UserException {
		List<BeatmapMeta> maps = new ArrayList<>();
		ArrayList<Long> mods = new ArrayList<>();
		if(requestMods == 0) {
			mods.add(0l);
			if(!nomod) {
				mods.add(Mods.getMask(Mods.DoubleTime));
				mods.add(Mods.getMask(Mods.DoubleTime, Mods.Hidden));
				mods.add(Mods.getMask(Mods.HardRock));
				mods.add(Mods.getMask(Mods.Hidden, Mods.HardRock));
			}
		} else {
			mods.add(requestMods);
			mods.add(requestMods | Mods.getMask(Mods.Hidden));
		}
		OsuApiUser user = getUser(userid, 0);
		final double equivalent = user.getPp() / 20;
		for (int i : setIds.keySet()) {
			for(long m : mods) {
				BeatmapMeta meta = loadBeatmap(i, m, null);
				if(Math.abs(1 - ((PercentageEstimates) meta.getEstimates()).getPPForAcc(.98) / equivalent) < .15) {
					maps.add(meta);
				}
			}
		}
		Collection<BareRecommendation> recommendations = new ArrayList<>();
		for (final BeatmapMeta meta : maps) {
			final PercentageEstimates est = (PercentageEstimates) meta.getEstimates();
			recommendations.add(new BareRecommendation() {
				@Override
				public double getProbability() {
					return .15 - Math.abs(1 - est.getPPForAcc(.98) / equivalent);
				}
				
				@Override
				public Integer getPersonalPP() {
					return (int) Math.ceil((est.getPPForAcc(.98) + equivalent) / 2);
				}
				
				@Override
				public long getMods() {
					return meta.getMods();
				}
				
				@Override
				public long[] getCauses() {
					return new long[0];
				}
				
				@Override
				public int getBeatmapId() {
					return meta.getBeatmap().getBeatmapId();
				}
			});
		}
		return recommendations;
	}

	@Override
	public boolean verifyGeneralKey(String key) throws SQLException {
		return "testkey".equals(key);
	}
	
	@Override
	public String getOptions(int user) throws SQLException {
		return database.users.get(user).options;
	}
	
	@Override
	public void saveOptions(int user, String options) throws SQLException {
		database.users.get(user).options = options;
		writeDatabase();
	}

	@Override
	public String tryLinkToPpaddict(String token, OsuApiUser user) throws SQLException {
		return null;
	}

	@Override
	public List<OsuApiScore> getRecentPlays(int userid) throws IOException {
		return Collections.emptyList();
	}

	@Override
	public OsuApiUser downloadUser(String userName) throws IOException,
			SQLException {
		Integer userid = database.userNames.get(userName);
		if (userid == null) {
			return null;
		}
		return database.users.get(userid).apiUser;
	}
}

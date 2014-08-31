package tillerino.tillerinobot;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta.PercentageEstimates;
import tillerino.tillerinobot.RecommendationsManager.BareRecommendation;
import tillerino.tillerinobot.RecommendationsManager.Model;
import static org.mockito.Mockito.*;

public abstract class TestBackend implements BotBackend {
	/*
	 * user 1 = normal user rank 10000
	 * 
	 * user 2 = donator rank 5000
	 * 
	 * user 3 = normal user rank 20000
	 */
	
	@Override
	public Integer resolveIRCName(String ircName) throws SQLException,
			IOException {
		switch (ircName) {
		case "user":
			return 1;
		case "donator":
			return 2;
		case "lowrank":
			return 3;
		default:
			return null;
		}
	}
	
	@Override
	public int getDonator(OsuApiUser user) throws SQLException, IOException {
		return user.getUserId() == 2 ? 1 : 0;
	}
	
	@Override
	public OsuApiUser getUser(int userid, long maxAge) throws SQLException,
			IOException {
		OsuApiUser apiUser = new OsuApiUser();
		
		apiUser.setUserId(userid);
		
		if(userid == 1) {
			apiUser.setUsername("user");
			apiUser.setRank(10000);
		} else if(userid == 2) {
			apiUser.setUsername("donator");
			apiUser.setRank(5000);
		} else if(userid == 3) {
			apiUser.setUsername("lowrank");
			apiUser.setRank(20000);
		} else {
			return null;
		}
		
		return apiUser;
	}
	
	@Override
	public Set<Integer> loadGivenRecommendations(String ircName)
			throws SQLException {
		return new HashSet<>();
	}
	
	int lastVisitedVersion = 1;
	
	@Override
	public int getLastVisitedVersion(String nick) {
		return lastVisitedVersion;
	}
	
	@Override
	public void setLastVisitedVersion(String nick, int version)
			throws SQLException {
		lastVisitedVersion = version;
	}
	
	@Override
	public Collection<BareRecommendation> loadRecommendations(int userid,
			Collection<Integer> exclude, Model model, boolean nomod,
			long requestMods) throws SQLException, IOException, UserException {
		Collection<BareRecommendation> ret = new ArrayList<>();
		
		for(int i = 1; i <= 10; i++) {
			BareRecommendation bareRecommendation = mock(BareRecommendation.class);
			when(bareRecommendation.getBeatmapId()).thenReturn(i);
			when(bareRecommendation.getCauses()).thenReturn(Collections.singleton(2l));
			when(bareRecommendation.getPersonalPP()).thenReturn(100);
			when(bareRecommendation.getProbability()).thenReturn(1d);
			when(bareRecommendation.getMods()).thenReturn(requestMods);
			
			ret.add(bareRecommendation);
		}
		return ret;
	}
	
	@Override
	public BeatmapMeta loadBeatmap(int beatmapid, final long mods)
			throws SQLException, IOException, UserException {
		OsuApiBeatmap beatmap = new OsuApiBeatmap();
		
		beatmap.setArtist("artist");
		beatmap.setVersion("version");
		beatmap.setId(beatmapid);
		beatmap.setTitle("title");
		
		BeatmapMeta beatmapMeta = new BeatmapMeta(beatmap, null, new PercentageEstimates() {
			@Override
			public double getPPForAcc(double acc) {
				return 100 * acc;
			}
			
			@Override
			public long getMods() {
				return mods;
			}
		});
		
		return beatmapMeta;
	}
	
	@Override
	public void registerActivity(int userid) throws SQLException {
		
	}
}

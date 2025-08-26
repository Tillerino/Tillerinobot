package tillerino.tillerinobot;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.inject.Inject;

import org.tillerino.osuApiModel.v2.DownloaderV2;

import lombok.RequiredArgsConstructor;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.data.ApiBeatmap;
import tillerino.tillerinobot.data.ApiScore;
import tillerino.tillerinobot.data.ApiUser;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class OsuApiV2Sometimes implements OsuApi {
  private final OsuApiV1 v1;
  private final OsuApiV1 v2;
  private final UserDataManager userDataManager;

  @Override
  public List<ApiScore> getUserTop(int userId, int mode, int limit) throws IOException {
    try (UserData userData = userDataManager.loadUserData(userId)) {
      if (userData.isV2()) {
        return v2.getUserTop(userId, mode, limit);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return v1.getUserTop(userId, mode, limit);
  }

  @Override
  public List<ApiScore> getUserRecent(int userid, int mode) throws IOException {
    try (UserData userData = userDataManager.loadUserData(userid)) {
      if (userData.isV2()) {
        return v2.getUserRecent(userid, mode);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return v1.getUserRecent(userid, mode);
  }

  // always delegate rest to V1
  @Override
  public ApiUser getUser(int userId, int gameMode) throws IOException {
    return v1.getUser(userId, gameMode);
  }

  @Override
  public ApiUser getUser(String username, int mode) throws IOException {
    return v1.getUser(username, mode);
  }

  @Override
  public ApiBeatmap getBeatmap(int beatmapid, long mods) throws IOException {
    return v1.getBeatmap(beatmapid, mods);
  }
}

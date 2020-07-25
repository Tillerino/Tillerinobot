package org.tillerino.ppaddict.server;

import java.sql.SQLException;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.ppaddict.server.auth.Credentials;

import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.diff.PercentageEstimates;

public interface PpaddictBackend {
  public interface BeatmapData {
    OsuApiBeatmap getBeatmap();

    PercentageEstimates getEstimates();
  }

  @CheckForNull
  Credentials resolveCookie(String cookie) throws SQLException;

  @Nonnull
  String createCookie(Credentials userIdentifier) throws SQLException;

  /**
   * @return may return null if the server is shutting down or if there are errors in the backend
   */
  @CheckForNull
  Map<BeatmapWithMods, BeatmapData> getBeatmaps();
}

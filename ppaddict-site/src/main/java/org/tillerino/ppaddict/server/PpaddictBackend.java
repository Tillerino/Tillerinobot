package org.tillerino.ppaddict.server;

import java.sql.SQLException;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.mapstruct.factory.Mappers;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.ppaddict.server.auth.Credentials;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.diff.PercentageEstimates;

public interface PpaddictBackend {
  /**
   * Extends {@link OsuApiBeatmapForDiff} with a few fields which we require in ppaddict. The goal
   * it to not have to use the much larger {@link OsuApiBeatmap} which has a lot of fields we don't
   * need and needlessly increases memory usage.
   */
  @Data
  @EqualsAndHashCode
  @ToString
  class OsuApiBeatmapForPpaddict {
    // we make these fields public because Mapstruct + Lombok in current combination is crap
    public int beatmapId, setId, totalLength;
    public String artist, title, version;
    public double bpm, circleSize;

    public double getCircleSize(@BitwiseMods long mods) {
      return OsuApiBeatmap.calcCircleSize(getCircleSize(), mods);
    }

    public double getBpm(@BitwiseMods long mods) {
      return OsuApiBeatmap.calcBpm(getBpm(), mods);
    }

    public int getTotalLength(@BitwiseMods long mods) {
      return OsuApiBeatmap.calcTotalLength(getTotalLength(), mods);
    }

    @org.mapstruct.Mapper
    public interface Mapper {
      Mapper INSTANCE = Mappers.getMapper(Mapper.class);

      OsuApiBeatmapForPpaddict shrink(OsuApiBeatmap large);
    }
  }

  @Value
  class BeatmapData {
    PercentageEstimates estimates;

    OsuApiBeatmapForPpaddict beatmap;
  }

  @CheckForNull
  Credentials resolveCookie(String cookie) throws SQLException;

  @Nonnull
  String createCookie(Credentials userIdentifier) throws SQLException;

  /**
   * @return may return null if the server is shutting down or if there are errors in the backend.
   *         The map may change and periodically is replaced entirely. During startup, the map is
   *         filled gradually. See also {@link #getBeatmapsGeneration()}.
   */
  @CheckForNull
  Map<BeatmapWithMods, BeatmapData> getBeatmaps();

  /**
   * @return increased whenever beatmaps are reloaded. This signals that caches need to be cleared.
   *         -1 during the initial load
   */
  long getBeatmapsGeneration();
}

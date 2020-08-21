package org.tillerino.ppaddict.server;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.client.services.RecommendationsService;
import org.tillerino.ppaddict.server.auth.Credentials;
import org.tillerino.ppaddict.shared.Beatmap;
import org.tillerino.ppaddict.shared.PpaddictException;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.UserException.RareUserException;
import tillerino.tillerinobot.data.GivenRecommendation;
import tillerino.tillerinobot.lang.Default;
import tillerino.tillerinobot.recommendations.Recommendation;
import tillerino.tillerinobot.recommendations.RecommendationsManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@Singleton
public class RecommendationsServiceImpl extends RemoteServiceServlet implements
    RecommendationsService {
  static final long serialVersionUID = 1L;
  Logger log = LoggerFactory.getLogger(RecommendationsServiceImpl.class);

  @Inject
  UserDataServiceImpl userDataService;
  @Inject
  PpaddictBackend backend;
  @Inject
  BotBackend botBackend;
  @Inject
  RecommendationsManager recommendationsManager;
  @Inject
  BeatmapTableServiceImpl beatmapTableService;

  @Override
  public List<Beatmap> getRecommendations() throws PpaddictException {
    Credentials credentials = userDataService.getCredentialsOrThrow(getThreadLocalRequest());

    PersistentUserData userData = userDataService.getServerUserData(credentials);

    int osuId = userData.getLinkedOsuIdOrThrow();

    List<GivenRecommendation> givenRecommendations =
        recommendationsManager.loadVisibleRecommendations(osuId);

    LinkedList<Beatmap> recommendations = new LinkedList<>();

    for (GivenRecommendation givenRecommendation : givenRecommendations) {
      final BeatmapMeta meta;
      try {
        meta =
            botBackend.loadBeatmap(givenRecommendation.getBeatmapid(),
                givenRecommendation.getMods(), new Default());
      } catch (SQLException | IOException | InterruptedException e) {
        throw ExceptionsUtil.getLoggedWrappedException(log, e);
      } catch (UserException e) {
        continue;
      }

      if (meta == null) {
        continue;
      }

      recommendations.add(beatmapTableService.makeBeatmap(userData, meta));

      if (recommendations.size() >= 10) {
        break;
      }
    }


    try {
      OsuApiUser apiUser = botBackend.getUser(osuId, 0);
      for (int i = 0; i < 100 && recommendations.size() < 10; i++) {
        try {
          Recommendation recommendation =
              recommendationsManager.getRecommendation(apiUser, userData.getSettings()
                  .getRecommendationsParameters(), new Default());

          final BeatmapMeta meta = recommendation.beatmap;

          if (meta.getBeatmap().getMaxCombo() <= 0) {
            continue;
          }

          recommendationsManager.saveGivenRecommendation(osuId,
              recommendation.bareRecommendation.getBeatmapId(),
              recommendation.bareRecommendation.getMods());

          Beatmap beatmap = beatmapTableService.makeBeatmap(userData, meta);

          recommendations.push(beatmap);
        } catch (RareUserException e) {
          continue;
        }
      }
    } catch (SQLException | IOException | UserException | InterruptedException e) {
      throw ExceptionsUtil.getLoggedWrappedException(log, e);
    }

    return recommendations;
  }

  @Override
  public Beatmap hideRecommendation(int beatmapid, String mods) throws PpaddictException {
    try {
      Long longMods =
          mods != null ? (mods.equals("?") ? -1 : Mods.fromShortNamesContinuous(mods)) : (Long) 0l;
      PersistentUserData linkedData =
          userDataService.getServerUserData(userDataService
              .getCredentialsOrThrow(getThreadLocalRequest()));
      int osuId = linkedData.getLinkedOsuIdOrThrow();
      recommendationsManager.hideRecommendation(osuId, beatmapid, longMods);
      OsuApiUser apiUser = botBackend.getUser(osuId, 0);
      Recommendation recommendation = null;
      for (int i = 0; i < 10; i++) {
        try {
          recommendation =
              recommendationsManager.getRecommendation(apiUser, linkedData.getSettings()
                  .getRecommendationsParameters(), new Default());
        } catch (RareUserException e) {
          continue;
        }
        break;
      }
      if (recommendation == null) {
        throw new PpaddictException(
            "Could not get a recommendation. Pls contact @Tillerino or /u/Tillerino");
      }
      recommendationsManager.saveGivenRecommendation(osuId,
          recommendation.bareRecommendation.getBeatmapId(),
          recommendation.bareRecommendation.getMods());
      return beatmapTableService.makeBeatmap(linkedData, recommendation.beatmap);
    } catch (SQLException | UserException | IOException | InterruptedException e) {
      throw ExceptionsUtil.getLoggedWrappedException(log, e);
    }
  }
}

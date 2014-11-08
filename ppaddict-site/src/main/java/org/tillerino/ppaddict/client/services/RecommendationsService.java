package org.tillerino.ppaddict.client.services;

import java.util.List;

import javax.annotation.CheckForNull;

import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.ppaddict.shared.Beatmap;
import org.tillerino.ppaddict.shared.PpaddictException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("recommendations")
public interface RecommendationsService extends RemoteService {
  List<Beatmap> getRecommendations() throws PpaddictException;

  Beatmap hideRecommendation(@BeatmapId int beatmapid, @CheckForNull String mods)
      throws PpaddictException;
}

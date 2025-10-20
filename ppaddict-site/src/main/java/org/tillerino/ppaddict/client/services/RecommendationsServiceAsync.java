package org.tillerino.ppaddict.client.services;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;
import org.tillerino.ppaddict.shared.Beatmap;

public interface RecommendationsServiceAsync {

    void getRecommendations(AsyncCallback<List<Beatmap>> callback);

    void hideRecommendation(int beatmapid, String mods, AsyncCallback<Beatmap> callback);
}

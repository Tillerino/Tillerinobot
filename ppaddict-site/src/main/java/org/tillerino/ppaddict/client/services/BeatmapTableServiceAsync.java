package org.tillerino.ppaddict.client.services;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.tillerino.ppaddict.shared.BeatmapBundle;
import org.tillerino.ppaddict.shared.BeatmapRangeRequest;

public interface BeatmapTableServiceAsync {

    void getRange(BeatmapRangeRequest request, AsyncCallback<BeatmapBundle> callback);
}

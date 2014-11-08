package org.tillerino.ppaddict.client.services;

import org.tillerino.ppaddict.shared.BeatmapBundle;
import org.tillerino.ppaddict.shared.BeatmapRangeRequest;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface BeatmapTableServiceAsync {

  void getRange(BeatmapRangeRequest request, AsyncCallback<BeatmapBundle> callback);

}

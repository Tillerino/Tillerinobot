package org.tillerino.ppaddict.client.services;

import org.tillerino.ppaddict.shared.BeatmapBundle;
import org.tillerino.ppaddict.shared.BeatmapRangeRequest;
import org.tillerino.ppaddict.shared.PpaddictException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("beatmaps")
public interface BeatmapTableService extends RemoteService {
  BeatmapBundle getRange(BeatmapRangeRequest request) throws PpaddictException;
}

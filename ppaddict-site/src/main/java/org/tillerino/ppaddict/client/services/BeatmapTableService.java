package org.tillerino.ppaddict.client.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.tillerino.ppaddict.shared.BeatmapBundle;
import org.tillerino.ppaddict.shared.BeatmapRangeRequest;
import org.tillerino.ppaddict.shared.PpaddictException;

@RemoteServiceRelativePath("beatmaps")
public interface BeatmapTableService extends RemoteService {
    BeatmapBundle getRange(BeatmapRangeRequest request) throws PpaddictException;
}

package org.tillerino.ppaddict.client.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import javax.annotation.CheckForNull;
import org.tillerino.ppaddict.shared.BeatmapRangeRequest;
import org.tillerino.ppaddict.shared.ClientUserData;
import org.tillerino.ppaddict.shared.InitialData;
import org.tillerino.ppaddict.shared.PpaddictException;
import org.tillerino.ppaddict.shared.Settings;

@RemoteServiceRelativePath(value = "user")
public interface UserDataService extends RemoteService {
    ClientUserData getStatus() throws PpaddictException;

    void saveSettings(Settings s) throws PpaddictException;

    /** @param request non-null if coming from a beatmap(set) link */
    InitialData getInitialData(@CheckForNull BeatmapRangeRequest request) throws PpaddictException;

    String getLinkString() throws PpaddictException;

    void saveComment(int beatmapid, String mods, String comment) throws PpaddictException;

    String createApiKey() throws PpaddictException;
}

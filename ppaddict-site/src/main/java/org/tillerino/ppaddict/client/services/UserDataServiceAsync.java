package org.tillerino.ppaddict.client.services;

import com.google.gwt.user.client.rpc.AsyncCallback;
import javax.annotation.CheckForNull;
import org.tillerino.ppaddict.shared.BeatmapRangeRequest;
import org.tillerino.ppaddict.shared.ClientUserData;
import org.tillerino.ppaddict.shared.InitialData;
import org.tillerino.ppaddict.shared.Settings;

public interface UserDataServiceAsync {

    void getStatus(AsyncCallback<ClientUserData> callback);

    void saveSettings(Settings s, AsyncCallback<Void> callback);

    void saveComment(int beatmapid, @CheckForNull String mods, String comment, AsyncCallback<Void> callback);

    void getInitialData(@CheckForNull BeatmapRangeRequest request, AsyncCallback<InitialData> callback);

    void getLinkString(AsyncCallback<String> callback);

    void createApiKey(AsyncCallback<String> callback);
}

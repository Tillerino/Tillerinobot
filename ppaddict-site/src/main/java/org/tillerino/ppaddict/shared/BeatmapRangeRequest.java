package org.tillerino.ppaddict.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import javax.annotation.Nonnull;

public class BeatmapRangeRequest extends BeatmapFilter {
    public BeatmapRangeRequest() {}

    public BeatmapRangeRequest(@Nonnull BeatmapRangeRequest o) {
        super(o);
        length = o.length;
        loadedUserRequest = o.loadedUserRequest;
        start = o.start;
    }

    public enum Sort implements IsSerializable {
        EXPECTED,
        PERFECT,
        BPM,
        LENGTH,
        STAR_DIFF
    }

    public int start = 0;
    public int length = 100;

    /**
     * true if user was logged in. only after this happened, the request will be persisted.
     *
     * <p>not persistent, but still transmitted from client to server
     */
    public boolean loadedUserRequest = false;

    @Override
    public String toString() {
        return start + " " + length + " " + sortBy + " " + direction + " "
                + getSearches().getSearchText() + " AR " + aR + " CS " + cS + " expected " + expectedPP
                + " perfect " + perfectPP + " mapLength " + mapLength + " " + getSearches();
    }
}

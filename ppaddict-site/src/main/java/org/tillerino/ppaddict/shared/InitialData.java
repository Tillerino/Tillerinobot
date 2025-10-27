package org.tillerino.ppaddict.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.Nonnull;

public class InitialData implements IsSerializable {
    @Nonnull
    public ClientUserData userData;

    @Nonnull
    public BeatmapBundle beatmapBundle;

    @Nonnull
    public BeatmapRangeRequest request;

    public InitialData(
            @Nonnull ClientUserData userData,
            @Nonnull BeatmapBundle beatmapBundle,
            @Nonnull BeatmapRangeRequest request) {
        super();
        this.userData = userData;
        this.beatmapBundle = beatmapBundle;
        this.request = request;
    }

    @SuppressFBWarnings(value = "NP", justification = "unused; for GWT")
    private InitialData() {}
}

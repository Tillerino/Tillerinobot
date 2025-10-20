package org.tillerino.ppaddict.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.ArrayList;
import java.util.List;

public class BeatmapBundle implements IsSerializable {
    public List<Beatmap> beatmaps = new ArrayList<>();
    public int available;
    public boolean loggedIn;

    public BeatmapBundle() {}
}

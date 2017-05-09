package org.tillerino.ppaddict.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BeatmapBundle implements IsSerializable {
  public List<Beatmap> beatmaps = new ArrayList<>();
  public int available;
  public boolean loggedIn;

  public BeatmapBundle() {}
}

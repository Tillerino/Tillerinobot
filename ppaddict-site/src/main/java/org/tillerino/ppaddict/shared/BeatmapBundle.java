package org.tillerino.ppaddict.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BeatmapBundle implements Serializable {
  private static final long serialVersionUID = 1L;

  public List<Beatmap> beatmaps = new ArrayList<Beatmap>();
  public int available;
  public boolean loggedIn;

  public BeatmapBundle() {}
}

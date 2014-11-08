package org.tillerino.ppaddict.shared;

import java.io.Serializable;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class InitialData implements Serializable {
  private static final long serialVersionUID = 1L;

  @Nonnull
  public ClientUserData userData;
  @Nonnull
  public BeatmapBundle beatmapBundle;
  @Nonnull
  public BeatmapRangeRequest request;

  public InitialData(@Nonnull ClientUserData userData, @Nonnull BeatmapBundle beatmapBundle,
      @Nonnull BeatmapRangeRequest request) {
    super();
    this.userData = userData;
    this.beatmapBundle = beatmapBundle;
    this.request = request;
  }

  @SuppressFBWarnings(value = "NP", justification = "unused; for GWT")
  private InitialData() {

  }
}

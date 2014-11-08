package org.tillerino.ppaddict.shared;

import java.io.Serializable;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class BeatmapRangeRequest implements Serializable {
  public BeatmapRangeRequest() {

  }

  public BeatmapRangeRequest(@Nonnull BeatmapRangeRequest o) {
    aR = new MinMax(o.aR);
    bpm = new MinMax(o.bpm);
    cS = new MinMax(o.cS);
    oD = new MinMax(o.oD);
    expectedPP = new MinMax(o.expectedPP);
    mapLength = new MinMax(o.mapLength);
    perfectPP = new MinMax(o.perfectPP);
    starDiff = new MinMax(o.starDiff);

    direction = o.direction;
    length = o.length;
    loadedUserRequest = o.loadedUserRequest;
    searches = new Searches(o.searches);

    sortBy = o.sortBy;
    start = o.start;
  }

  public enum Sort {
    EXPECTED, PERFECT, BPM, LENGTH, STAR_DIFF
  }

  private static final long serialVersionUID = 1L;

  public int start = 0;
  public int length = 100;
  @CheckForNull
  public BeatmapRangeRequest.Sort sortBy = null;
  public int direction = 1;

  @Nonnull
  private Searches searches = new Searches();

  /**
   * @return never null. either was deserialized with JDO, in which case embedded class is not null
   *         or was created via default constructor
   */
  @Nonnull
  public Searches getSearches() {
    return searches;
  }

  public void setSearches(@Nonnull Searches searches) {
    this.searches = searches;
  }

  public MinMax expectedPP = new MinMax(null, null);

  public MinMax perfectPP = new MinMax(null, null);

  public MinMax aR = new MinMax(null, null);

  public MinMax cS = new MinMax(null, null);

  public MinMax oD = new MinMax(null, null);

  public MinMax bpm = new MinMax(null, null);

  public MinMax mapLength = new MinMax(null, null);

  public MinMax starDiff = new MinMax(null, null);

  /**
   * true if user was logged in. only after this happened, the request will be persisted.
   * 
   * not persistent, but still transmitted from client to server
   */
  public boolean loadedUserRequest = false;

  @Override
  public String toString() {
    return start + " " + length + " " + sortBy + " " + direction + " "
        + getSearches().getSearchText() + " AR " + aR + " CS " + cS + " expected " + expectedPP
        + " perfect " + perfectPP + " mapLength " + mapLength;
  }

}

package org.tillerino.ppaddict.shared;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BeatmapFilter implements IsSerializable {
  @CheckForNull
  public BeatmapRangeRequest.Sort sortBy = null;
  public int direction = 1;

  @Nonnull
  private Searches searches = new Searches();

  public MinMax expectedPP = new MinMax(null, null);

  public MinMax perfectPP = new MinMax(null, null);

  public MinMax aR = new MinMax(null, null);

  public MinMax cS = new MinMax(null, null);

  public MinMax oD = new MinMax(null, null);

  public MinMax bpm = new MinMax(null, null);

  public MinMax mapLength = new MinMax(null, null);

  public MinMax starDiff = new MinMax(null, null);

  public BeatmapFilter() {}

  public BeatmapFilter(@Nonnull BeatmapFilter o) {
    aR = new MinMax(o.aR);
    bpm = new MinMax(o.bpm);
    cS = new MinMax(o.cS);
    oD = new MinMax(o.oD);
    expectedPP = new MinMax(o.expectedPP);
    mapLength = new MinMax(o.mapLength);
    perfectPP = new MinMax(o.perfectPP);
    starDiff = new MinMax(o.starDiff);

    sortBy = o.sortBy;
    direction = o.direction;

    searches = new Searches(o.searches);
  }

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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + aR.hashCode();
    result = prime * result + bpm.hashCode();
    result = prime * result + cS.hashCode();
    result = prime * result + direction;
    result = prime * result + expectedPP.hashCode();
    result = prime * result + mapLength.hashCode();
    result = prime * result + oD.hashCode();
    result = prime * result + perfectPP.hashCode();
    result = prime * result + searches.hashCode();
    result = prime * result + ((sortBy == null) ? 0 : sortBy.hashCode());
    result = prime * result + starDiff.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    BeatmapFilter other = (BeatmapFilter) obj;
    if (!aR.equals(other.aR))
      return false;
    if (!bpm.equals(other.bpm))
      return false;
    if (!cS.equals(other.cS))
      return false;
    if (direction != other.direction)
      return false;
    if (!expectedPP.equals(other.expectedPP))
      return false;
    if (!mapLength.equals(other.mapLength))
      return false;
    if (!oD.equals(other.oD))
      return false;
    if (!perfectPP.equals(other.perfectPP))
      return false;
    if (!searches.equals(other.searches))
      return false;
    if (sortBy != other.sortBy)
      return false;
    if (!starDiff.equals(other.starDiff))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "BeatmapFilter [sortBy=" + sortBy + ", direction=" + direction + ", searches=" + searches
        + ", expectedPP=" + expectedPP + ", perfectPP=" + perfectPP + ", aR=" + aR + ", cS=" + cS
        + ", oD=" + oD + ", bpm=" + bpm + ", mapLength=" + mapLength + ", starDiff=" + starDiff
        + "]";
  }
}

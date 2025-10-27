package org.tillerino.ppaddict.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

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

    public boolean rankedOnly = false;

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

        rankedOnly = o.rankedOnly;
    }

    /**
     * @return never null. either was deserialized with JDO, in which case embedded class is not null or was created via
     *     default constructor
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
        result = prime * result + ((aR == null) ? 0 : aR.hashCode());
        result = prime * result + ((bpm == null) ? 0 : bpm.hashCode());
        result = prime * result + ((cS == null) ? 0 : cS.hashCode());
        result = prime * result + direction;
        result = prime * result + ((expectedPP == null) ? 0 : expectedPP.hashCode());
        result = prime * result + ((mapLength == null) ? 0 : mapLength.hashCode());
        result = prime * result + ((oD == null) ? 0 : oD.hashCode());
        result = prime * result + ((perfectPP == null) ? 0 : perfectPP.hashCode());
        result = prime * result + (rankedOnly ? 1231 : 1237);
        result = prime * result + ((searches == null) ? 0 : searches.hashCode());
        result = prime * result + ((sortBy == null) ? 0 : sortBy.hashCode());
        result = prime * result + ((starDiff == null) ? 0 : starDiff.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BeatmapFilter other = (BeatmapFilter) obj;
        if (aR == null) {
            if (other.aR != null) return false;
        } else if (!aR.equals(other.aR)) return false;
        if (bpm == null) {
            if (other.bpm != null) return false;
        } else if (!bpm.equals(other.bpm)) return false;
        if (cS == null) {
            if (other.cS != null) return false;
        } else if (!cS.equals(other.cS)) return false;
        if (direction != other.direction) return false;
        if (expectedPP == null) {
            if (other.expectedPP != null) return false;
        } else if (!expectedPP.equals(other.expectedPP)) return false;
        if (mapLength == null) {
            if (other.mapLength != null) return false;
        } else if (!mapLength.equals(other.mapLength)) return false;
        if (oD == null) {
            if (other.oD != null) return false;
        } else if (!oD.equals(other.oD)) return false;
        if (perfectPP == null) {
            if (other.perfectPP != null) return false;
        } else if (!perfectPP.equals(other.perfectPP)) return false;
        if (rankedOnly != other.rankedOnly) return false;
        if (searches == null) {
            if (other.searches != null) return false;
        } else if (!searches.equals(other.searches)) return false;
        if (sortBy != other.sortBy) return false;
        if (starDiff == null) {
            if (other.starDiff != null) return false;
        } else if (!starDiff.equals(other.starDiff)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "BeatmapFilter [sortBy=" + sortBy + ", direction=" + direction + ", searches=" + searches
                + ", expectedPP=" + expectedPP + ", perfectPP=" + perfectPP + ", aR=" + aR + ", cS=" + cS
                + ", oD=" + oD + ", bpm=" + bpm + ", mapLength=" + mapLength + ", starDiff=" + starDiff
                + ", rankedOnly=" + rankedOnly + "]";
    }
}

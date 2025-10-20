package org.tillerino.ppaddict.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BeatmapSetId;

public class Searches implements IsSerializable {
    public Searches() {}

    public Searches(Searches searches) {
        searchText = searches.searchText;
        searchComment = searches.searchComment;
        beatmapId = searches.beatmapId;
        setId = searches.setId;
    }

    private String searchText = "";
    private String searchComment = "";

    @CheckForNull
    @BeatmapId
    private Integer beatmapId;

    @CheckForNull
    @BeatmapSetId
    private Integer setId;

    public Searches getCopy() {
        Searches copy = new Searches();
        copy.setSearchText(searchText);
        copy.setSearchComment(searchComment);
        return copy;
    }

    public static String nonNullTrimmed(String string) {
        if (string == null) {
            return "";
        }
        return string.trim();
    }

    @Nonnull
    /** @return not null and trimmed */
    public String getSearchComment() {
        return nonNullTrimmed(searchComment);
    }

    @Nonnull
    /** @return not null and trimmed */
    public String getSearchText() {
        return nonNullTrimmed(searchText);
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public void setSearchComment(String searchComment) {
        this.searchComment = searchComment;
    }

    @CheckForNull
    @BeatmapId
    public Integer getBeatmapId() {
        return beatmapId;
    }

    public void setBeatmapId(@CheckForNull @BeatmapId Integer beatmapId) {
        this.beatmapId = beatmapId;
    }

    @CheckForNull
    @BeatmapSetId
    public Integer getSetId() {
        return setId;
    }

    public void setSetId(@CheckForNull @BeatmapSetId Integer setId) {
        this.setId = setId;
    }

    @Override
    public String toString() {
        return "Searches [searchText=" + searchText + ", searchComment=" + searchComment + ", beatmapId=" + beatmapId
                + ", beatmapSetId=" + setId + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((beatmapId == null) ? 0 : beatmapId.hashCode());
        result = prime * result + ((searchComment == null) ? 0 : searchComment.hashCode());
        result = prime * result + ((searchText == null) ? 0 : searchText.hashCode());
        result = prime * result + ((setId == null) ? 0 : setId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Searches other = (Searches) obj;
        if (beatmapId == null) {
            if (other.beatmapId != null) return false;
        } else if (!beatmapId.equals(other.beatmapId)) return false;
        if (searchComment == null) {
            if (other.searchComment != null) return false;
        } else if (!searchComment.equals(other.searchComment)) return false;
        if (searchText == null) {
            if (other.searchText != null) return false;
        } else if (!searchText.equals(other.searchText)) return false;
        if (setId == null) {
            if (other.setId != null) return false;
        } else if (!setId.equals(other.setId)) return false;
        return true;
    }
}

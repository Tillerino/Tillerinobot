package org.tillerino.ppaddict.shared;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BeatmapSetId;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Searches implements IsSerializable {
  public Searches() {}

  public Searches(Searches searches) {
    searchText = searches.searchText;
    searchComment = searches.searchComment;
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
  /**
   * @return not null and trimmed
   */
  public String getSearchComment() {
    return nonNullTrimmed(searchComment);
  }

  @Nonnull
  /**
   * @return not null and trimmed
   */
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
    return "Searches [searchText=" + searchText + ", searchComment=" + searchComment
        + ", beatmapId=" + beatmapId + ", beatmapSetId=" + setId + "]";
  }
}

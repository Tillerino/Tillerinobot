package org.tillerino.ppaddict.shared;

import java.io.Serializable;

import javax.annotation.Nonnull;

public class Searches implements Serializable {
  private static final long serialVersionUID = 1L;

  public Searches() {}

  public Searches(Searches searches) {
    searchText = searches.searchText;
    searchComment = searches.searchComment;
  }

  private String searchText = "";
  private String searchComment = "";

  public Searches getCopy() {
    Searches copy = new Searches();
    copy.setSearchText(searchText);
    copy.setSearchComment(searchComment);
    return copy;
  }

  public static String nonNullTrimmed(String string) {
    if (string == null)
      return "";
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
}

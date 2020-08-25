package org.tillerino.ppaddict.shared;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class Settings extends BeatmapFilterSettings {
  public static final Settings DEFAULT_SETTINGS = new Settings();

  public Settings() {}

  private boolean openDirectOnMapSelect = false;

  private String recommendationsParameters = null;

  public Settings(@Nonnull Settings o) {
    super(o);
    openDirectOnMapSelect = o.openDirectOnMapSelect;
    recommendationsParameters = o.recommendationsParameters;
  }

  public boolean isOpenDirectOnMapSelect() {
    return openDirectOnMapSelect;
  }

  public void setOpenDirectOnMapSelect(boolean openDirectOnMapSelect) {
    this.openDirectOnMapSelect = openDirectOnMapSelect;
  }

  @CheckForNull
  public String getRecommendationsParameters() {
    return recommendationsParameters;
  }

  public void setRecommendationsParameters(String recommendationsParameters) {
    this.recommendationsParameters = recommendationsParameters;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (openDirectOnMapSelect ? 1231 : 1237);
    result = prime * result
        + ((recommendationsParameters == null) ? 0 : recommendationsParameters.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Settings other = (Settings) obj;
    if (openDirectOnMapSelect != other.openDirectOnMapSelect) {
      return false;
    }
    if (recommendationsParameters == null) {
      if (other.recommendationsParameters != null) {
        return false;
      }
    } else if (!recommendationsParameters.equals(other.recommendationsParameters)) {
      return false;
    }
    return true;
  }
}

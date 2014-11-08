package org.tillerino.ppaddict.shared;

import java.io.Serializable;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.tillerino.ppaddict.shared.PpaddictException.OutOfBoundsException;

public class Settings implements Serializable {
  public static final Settings DEFAULT_SETTINGS = new Settings();

  private static final long serialVersionUID = 1L;

  public Settings() {}

  private boolean openDirectOnMapSelect = false;
  private boolean applyOtherFiltersWithTextFilter = false;

  private double lowAccuracy = 93, highAccuracy = 100;

  private String recommendationsParameters = null;

  public Settings(@Nonnull Settings o) {
    openDirectOnMapSelect = o.openDirectOnMapSelect;
    applyOtherFiltersWithTextFilter = o.applyOtherFiltersWithTextFilter;
    lowAccuracy = o.lowAccuracy;
    highAccuracy = o.highAccuracy;
    recommendationsParameters = o.recommendationsParameters;
  }

  public boolean isOpenDirectOnMapSelect() {
    return openDirectOnMapSelect;
  }

  public void setOpenDirectOnMapSelect(boolean openDirectOnMapSelect) {
    this.openDirectOnMapSelect = openDirectOnMapSelect;
  }

  public boolean isApplyOtherFiltersWithTextFilter() {
    return applyOtherFiltersWithTextFilter;
  }

  public void setApplyOtherFiltersWithTextFilter(boolean applyOtherFiltersWithTextFilter) {
    this.applyOtherFiltersWithTextFilter = applyOtherFiltersWithTextFilter;
  }

  public double getLowAccuracy() {
    return lowAccuracy;
  }

  public void setLowAccuracy(double lowAccuracy) throws OutOfBoundsException {
    this.lowAccuracy = PpaddictException.checkBounds("High accuracy", lowAccuracy, 0, highAccuracy);
  }

  public double getHighAccuracy() {
    return highAccuracy;
  }

  public void setHighAccuracy(double highAccuracy) throws OutOfBoundsException {
    this.highAccuracy =
        PpaddictException.checkBounds("High accuracy", highAccuracy, lowAccuracy, 100);
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
    int result = 1;
    result = prime * result + (applyOtherFiltersWithTextFilter ? 1231 : 1237);
    long temp;
    temp = Double.doubleToLongBits(highAccuracy);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(lowAccuracy);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (openDirectOnMapSelect ? 1231 : 1237);
    result =
        prime * result
            + ((recommendationsParameters == null) ? 0 : recommendationsParameters.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Settings other = (Settings) obj;
    if (applyOtherFiltersWithTextFilter != other.applyOtherFiltersWithTextFilter) {
      return false;
    }
    if (Double.doubleToLongBits(highAccuracy) != Double.doubleToLongBits(other.highAccuracy)) {
      return false;
    }
    if (Double.doubleToLongBits(lowAccuracy) != Double.doubleToLongBits(other.lowAccuracy)) {
      return false;
    }
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

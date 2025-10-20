package org.tillerino.ppaddict.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import javax.annotation.Nonnull;
import org.tillerino.ppaddict.shared.PpaddictException.OutOfBoundsException;

public class BeatmapFilterSettings implements IsSerializable {
    private boolean applyOtherFiltersWithTextFilter = false;

    private double lowAccuracy = 93, highAccuracy = 100;

    public BeatmapFilterSettings() {}

    public BeatmapFilterSettings(@Nonnull BeatmapFilterSettings o) {
        applyOtherFiltersWithTextFilter = o.applyOtherFiltersWithTextFilter;
        lowAccuracy = o.lowAccuracy;
        highAccuracy = o.highAccuracy;
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
        this.highAccuracy = PpaddictException.checkBounds("High accuracy", highAccuracy, lowAccuracy, 100);
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
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BeatmapFilterSettings other = (BeatmapFilterSettings) obj;
        if (applyOtherFiltersWithTextFilter != other.applyOtherFiltersWithTextFilter) return false;
        if (Double.doubleToLongBits(highAccuracy) != Double.doubleToLongBits(other.highAccuracy)) return false;
        if (Double.doubleToLongBits(lowAccuracy) != Double.doubleToLongBits(other.lowAccuracy)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "BeatmapFilterSettings [applyOtherFiltersWithTextFilter="
                + applyOtherFiltersWithTextFilter + ", lowAccuracy=" + lowAccuracy + ", highAccuracy="
                + highAccuracy + "]";
    }
}

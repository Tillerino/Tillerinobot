package org.tillerino.ppaddict.shared;

import java.io.Serializable;

public class MinMax implements Serializable {
  private static final long serialVersionUID = 1L;

  public MinMax() {

  }

  public MinMax(Integer min, Integer max) {
    super();
    this.min = min;
    this.max = max;
  }

  public MinMax(MinMax o) {
    min = o.min;
    max = o.max;
  }

  public Integer min;
  public Integer max;

  public MinMax getCopy() {
    return new MinMax(min, max);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((max == null) ? 0 : max.hashCode());
    result = prime * result + ((min == null) ? 0 : min.hashCode());
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
    MinMax other = (MinMax) obj;
    if (max == null) {
      if (other.max != null)
        return false;
    } else if (!max.equals(other.max))
      return false;
    if (min == null) {
      if (other.min != null)
        return false;
    } else if (!min.equals(other.min))
      return false;
    return true;
  }



  @Override
  public String toString() {
    return "{min: " + min + " max: " + max + "}";
  }
}

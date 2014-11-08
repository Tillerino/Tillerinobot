package org.tillerino.ppaddict.shared;

import org.tillerino.ppaddict.client.services.AbstractAsyncCallback;


/**
 * An exception that is supposed to be handled by the visitor. It's message should be displayed on
 * the website. This is done automatically by {@link AbstractAsyncCallback}.
 * 
 * @author Tillerino
 */
public class PpaddictException extends Exception {
  private static final long serialVersionUID = 1L;

  public PpaddictException(String message) {
    super(message);
  }

  @SuppressWarnings("unused")
  private PpaddictException() {

  }

  public static class NotLoggedIn extends PpaddictException {
    private static final long serialVersionUID = 1L;

    public NotLoggedIn() {
      super("You need to log in to use this feature.");
    }
  }

  public static class NotLinked extends PpaddictException {
    private static final long serialVersionUID = 1L;

    public NotLinked() {
      super("You need to link your osu! account to use this feature.");
    }
  }

  public static class OutOfBoundsException extends PpaddictException {
    private static final long serialVersionUID = 1L;

    public OutOfBoundsException(String propertyName, double lowerBound, double upperBound,
        double actualValue) {
      super("Attempting to set " + propertyName + " to " + actualValue + ", which is not between "
          + lowerBound + " and " + upperBound + ".");
    }
  }

  public static double checkBounds(String propertyName, double value, double lowerBound,
      double upperBound) throws OutOfBoundsException {
    if (value < lowerBound || value > upperBound) {
      throw new OutOfBoundsException(propertyName, lowerBound, upperBound, value);
    }
    return value;
  }

  public static double parseDouble(String propertyName, String value) throws PpaddictException {
    value = value.replace(',', '.');
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException e) {
      throw new PpaddictException(propertyName + " has unrecognizable number format: " + value);
    }
  }
}

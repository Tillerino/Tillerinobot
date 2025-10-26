package tillerino.tillerinobot;

import java.io.Serial;
import lombok.NonNull;

/**
 * This type of exception will be displayed to the user.
 *
 * @author Tillerino
 */
public class UserException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String ERROR_MESSAGE = "%s must be between %s and %s but was %s";

    public UserException(String message) {
        super(message);
    }

    public static void validateInclusiveBetween(long floor, long ceil, long actual, @NonNull String desc)
            throws UserException {
        if (actual < floor || actual > ceil) {
            throw new UserException(String.format(ERROR_MESSAGE, desc, floor, ceil, actual));
        }
    }

    public static void validateInclusiveBetween(double floor, double ceil, double actual, @NonNull String desc)
            throws UserException {
        if (actual < floor || actual > ceil) {
            throw new UserException(String.format(ERROR_MESSAGE, desc, floor, ceil, actual));
        }
    }

    /**
     * This type of exception is extremely rare in a sense that it won't occur again if the causing action is repeated.
     *
     * @author Tillerino
     */
    public static class RareUserException extends UserException {
        @Serial
        private static final long serialVersionUID = 1L;

        public RareUserException(String message) {
            super(message);
        }
    }
}

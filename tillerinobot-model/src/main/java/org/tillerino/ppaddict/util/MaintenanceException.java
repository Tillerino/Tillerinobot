package org.tillerino.ppaddict.util;

import jakarta.ws.rs.ServiceUnavailableException;
import java.io.Serial;

/**
 * This can be thrown from anywhere in the code to indicate that something does not work currently because of
 * maintenance. When encountering this exception, either use a fallback or display a message about maintenance.
 */
public class MaintenanceException extends ServiceUnavailableException {
    @Serial
    private static final long serialVersionUID = 1L;

    public MaintenanceException(String message) {
        super(message);
    }
}

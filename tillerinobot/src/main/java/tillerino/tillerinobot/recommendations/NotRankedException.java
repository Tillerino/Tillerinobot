package tillerino.tillerinobot.recommendations;

import tillerino.tillerinobot.UserException;

/**
 * Thrown when a loaded beatmap has neither aproved status 1 or 2.
 * 
 *	// TODO find out about "qualified" = 3
 * 
 * @author Tillerino
 */
public class NotRankedException extends UserException {
	private static final long serialVersionUID = 1L;

	public NotRankedException(String message) {
		super(message);
	}
}
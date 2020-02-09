package tillerino.tillerinobot.recommendations;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The type of recommendation model that the player has chosen.
 * 
 * @author Tillerino
 */
@RequiredArgsConstructor
public enum Model {
	ALPHA(false),
	BETA(false),
	// gamma4
	GAMMA(true),
	GAMMA5(true);

	@Getter
	private final boolean modsCapable;
}
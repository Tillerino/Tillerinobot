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
	GAMMA4(true),
	GAMMA5(true),
	GAMMA6(true),
	GAMMA7(true),
	/**
	 * External model made by NamePendingApproval
	 */
	NAP(true)
	;

	@Getter
	private final boolean modsCapable;
}
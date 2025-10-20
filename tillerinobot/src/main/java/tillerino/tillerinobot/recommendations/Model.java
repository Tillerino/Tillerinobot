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
    GAMMA8(true),
    GAMMA9(true),
    GAMMA10(true),
    /** External model made by NamePendingApproval */
    NAP(true);

    @Getter
    private final boolean modsCapable;
}

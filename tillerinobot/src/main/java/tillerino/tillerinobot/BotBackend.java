package tillerino.tillerinobot;

import javax.annotation.CheckForNull;
import org.tillerino.osuApiModel.types.UserId;
import tillerino.tillerinobot.data.ApiUser;

public interface BotBackend {
    /**
     * Checks if a user is a donator/patron.
     *
     * @return a positive value if the user is a donator/patron.
     */
    int getDonator(@UserId int user);

    /**
     * links the given user to a Patreon account using a token string.
     *
     * @param token a token that was emailed to the Patron.
     * @param user osu account to link to
     * @return the name of the Patreon account that current user was linked to, or null if the token was not valid
     */
    @CheckForNull
    String tryLinkToPatreon(String token, ApiUser user);
}

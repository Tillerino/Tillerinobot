package tillerino.tillerinobot.handlers.options;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;

import lombok.Getter;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Message;

import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Language;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.sql.SQLException;

public abstract class OptionHandler {
    @Nonnull
    private final String description;

    @Nonnull
    @Getter
    private final String optionName;

    @Nullable
    private final String shortOptionName;

    @Getter
    private final int minHearts;

    protected OptionHandler(@Nonnull String description, @Nonnull String optionName, @Nullable String shortOptionName, int minHearts) {
        this.description = description;
        this.optionName = optionName;
        this.shortOptionName = shortOptionName;
        this.minHearts = minHearts;
    }

    @CheckForNull
    public GameChatResponse handle(String option, boolean set, String value, UserData userData, OsuApiUser apiUser, Language lang) throws SQLException, IOException, UserException {
        if(!shouldHandle(option, userData.getHearts())) return null;

        if(set) {
            handleSet(value, userData, apiUser, lang);
            return responseAfterSet(userData, apiUser);
        } else {
            return handleGet(userData);
        }
    }

    protected GameChatResponse responseAfterSet(UserData userData, OsuApiUser apiUser) {
        return handleGet(userData);
    }

    protected boolean shouldHandle(String option, int userHearts) {
        if(userHearts < minHearts) return false;
        if(shortOptionName != null && shortOptionName.equals(option)) return true;
        return getLevenshteinDistance(option, optionName) <= 1;
    }

    protected abstract void handleSet(String value, UserData userData, OsuApiUser apiUser, Language lang) throws UserException, SQLException, IOException;

    @Nonnull
    private GameChatResponse handleGet(UserData userData) {
        return new Message(description + ": " + getCurrentValue(userData));
    }

    @Nonnull
    protected abstract String getCurrentValue(UserData userData);
}

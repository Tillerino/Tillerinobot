package tillerino.tillerinobot.handlers.options;

import java.io.IOException;
import java.sql.SQLException;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;

import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Language;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BooleanOptionHandler extends OptionHandler {
    protected BooleanOptionHandler(@Nonnull String description, @Nonnull String optionName, @Nullable String shortOptionName, int minHearts) {
        super(description, optionName, shortOptionName, minHearts);
    }

    @Override
    protected void handleSet(String value, UserData userData, OsuApiUser apiUser, Language lang)
        throws UserException, SQLException, IOException {
        handleSetBoolean(parseBoolean(value, lang), userData);
    }

    @Nonnull
    @Override
    protected String getCurrentValue(UserData userData) {
        return getCurrentBooleanValue(userData) ? "ON" : "OFF";
    }

    protected abstract void handleSetBoolean(boolean value, UserData userData) throws SQLException, IOException;

    protected abstract boolean getCurrentBooleanValue(UserData userData);

    public static boolean parseBoolean(final @Nonnull String original, Language lang) throws UserException {
        String s = original.toLowerCase();
        if (s.equals("on") || s.equals("true") || s.equals("yes") || s.equals("1")) {
            return true;
        }
        if (s.equals("off") || s.equals("false") || s.equals("no") || s.equals("0")) {
            return false;
        }
        throw new UserException(lang.invalidChoice(original, "on|true|yes|1|off|false|no|0"));
    }
}

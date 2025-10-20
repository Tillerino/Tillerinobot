package tillerino.tillerinobot.handlers.options;

import static java.util.stream.Collectors.joining;

import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.handlers.OptionsHandler;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.lang.LanguageIdentifier;

public class LangOptionHandler extends OptionHandler {
    public LangOptionHandler() {
        super("Language", "language", "lang", 0);
    }

    @Override
    protected void handleSet(String value, UserData userData, OsuApiUser apiUser, Language lang) throws UserException {
        LanguageIdentifier ident;
        try {
            ident = OptionsHandler.find(LanguageIdentifier.values(), i -> i.token, value);
        } catch (IllegalArgumentException e) {
            String choices = Stream.of(LanguageIdentifier.values())
                    .map(i -> i.token)
                    .sorted()
                    .collect(joining(", "));
            throw new UserException(lang.invalidChoice(value, choices));
        }

        userData.setLanguage(ident);
    }

    @Override
    protected GameChatResponse responseAfterSet(UserData userData, OsuApiUser apiUser) {
        return userData.usingLanguage(lang -> lang.optionalCommentOnLanguage(apiUser));
    }

    @Override
    @Nonnull
    protected String getCurrentValue(UserData userData) {
        return userData.getLanguageIdentifier().token;
    }
}

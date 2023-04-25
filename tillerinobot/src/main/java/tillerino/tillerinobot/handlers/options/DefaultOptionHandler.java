package tillerino.tillerinobot.handlers.options;

import org.apache.commons.lang3.StringUtils;
import org.tillerino.osuApiModel.OsuApiUser;
import tillerino.tillerinobot.UserDataManager;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.recommendations.RecommendationRequestParser;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.sql.SQLException;

public class DefaultOptionHandler extends OptionHandler {
    private final RecommendationRequestParser requestParser;

    public DefaultOptionHandler(RecommendationRequestParser requestParser) {
        super("Default recommendation settings", "default", null, 0);
        this.requestParser = requestParser;
    }

    @Override
    protected void handleSet(String value, UserDataManager.UserData userData, OsuApiUser apiUser, Language lang) throws UserException, SQLException, IOException {
        if (value.isEmpty()) {
            userData.setDefaultRecommendationOptions(null);
        } else {
            requestParser.parseSamplerSettings(apiUser, value, lang);
            userData.setDefaultRecommendationOptions(value);
        }
    }

    @Nonnull
    @Override
    protected String getCurrentValue(UserDataManager.UserData userData) {
        return StringUtils.defaultString(userData.getDefaultRecommendationOptions(), "-");
    }
}

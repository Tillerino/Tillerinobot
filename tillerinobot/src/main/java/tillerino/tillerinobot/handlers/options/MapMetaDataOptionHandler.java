package tillerino.tillerinobot.handlers.options;

import tillerino.tillerinobot.UserDataManager.UserData;

public class MapMetaDataOptionHandler extends BooleanOptionHandler {
    public MapMetaDataOptionHandler() {
        super("Show map metadata on recommendations", "r-metadata", null, 0);
    }

    @Override
    protected void handleSetBoolean(boolean value, UserData userData) {
        userData.setShowMapMetaDataOnRecommendation(value);
    }

    @Override
    protected boolean getCurrentBooleanValue(UserData userData) {
        return userData.isShowMapMetaDataOnRecommendation();
    }
}

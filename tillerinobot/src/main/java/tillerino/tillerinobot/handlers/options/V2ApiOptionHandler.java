package tillerino.tillerinobot.handlers.options;

import tillerino.tillerinobot.UserDataManager.UserData;

public class V2ApiOptionHandler extends BooleanOptionHandler {
    public V2ApiOptionHandler() {
        super("v2 API", "v2", null, 0);
    }

    @Override
    protected void handleSetBoolean(boolean value, UserData userData) {
        userData.setV2(value);
    }

    @Override
    protected boolean getCurrentBooleanValue(UserData userData) {
        return userData.isV2();
    }
}
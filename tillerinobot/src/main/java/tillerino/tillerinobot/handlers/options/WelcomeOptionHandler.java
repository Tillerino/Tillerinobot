package tillerino.tillerinobot.handlers.options;

import tillerino.tillerinobot.UserDataManager.UserData;

public class WelcomeOptionHandler extends BooleanOptionHandler {
    public WelcomeOptionHandler() {
        super("Welcome Message", "welcome", null, 1);
    }

    @Override
    protected void handleSetBoolean(boolean value, UserData userData) {
        userData.setShowWelcomeMessage(value);
    }

    @Override
    protected boolean getCurrentBooleanValue(UserData userData) {
        return userData.isShowWelcomeMessage();
    }
}

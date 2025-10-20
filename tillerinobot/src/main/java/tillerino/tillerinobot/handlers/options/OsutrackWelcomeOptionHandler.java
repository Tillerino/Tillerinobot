package tillerino.tillerinobot.handlers.options;

import tillerino.tillerinobot.UserDataManager.UserData;

public class OsutrackWelcomeOptionHandler extends BooleanOptionHandler {
    public OsutrackWelcomeOptionHandler() {
        super("osu!track on welcome", "osutrack-welcome", null, 1);
    }

    @Override
    protected void handleSetBoolean(boolean value, UserData userData) {
        userData.setOsuTrackWelcomeEnabled(value);
    }

    @Override
    protected boolean getCurrentBooleanValue(UserData userData) {
        return userData.isOsuTrackWelcomeEnabled();
    }
}

package tillerino.tillerinobot.handlers.options;


import java.io.IOException;
import java.sql.SQLException;

import tillerino.tillerinobot.UserDataManager;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.recommendations.RecommendationsManager;

public class V2ApiOptionHandler extends BooleanOptionHandler {
  private final UserDataManager userDataManager;
  private final RecommendationsManager recommendationsManager;

  public V2ApiOptionHandler(UserDataManager userDataManager, RecommendationsManager recommendationsManager) {
        super("v2 API", "v2", null, 0);
    this.userDataManager = userDataManager;
    this.recommendationsManager = recommendationsManager;
  }

    @Override
    protected void handleSetBoolean(boolean value, UserData userData) throws SQLException, IOException {
        userData.setV2(value);
        userDataManager.saveUserData(userData);
        recommendationsManager.forceUpdateTopScores(userData.getUserid());
    }

    @Override
    protected boolean getCurrentBooleanValue(UserData userData) {
        return userData.isV2();
    }
}
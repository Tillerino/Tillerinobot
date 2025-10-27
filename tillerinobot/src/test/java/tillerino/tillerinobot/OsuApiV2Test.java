package tillerino.tillerinobot;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tillerino.osuApiModel.v2.TokenHelper.Credentials;
import tillerino.tillerinobot.data.ApiBeatmap;
import tillerino.tillerinobot.data.ApiUser;

public class OsuApiV2Test extends TestBase {
    public static final String OSUAPI_V2_MOCK_CLIENT_ID = "12345";
    public static final String OSUAPI_V2_MOCK_CLIENT_SECRET = "jusTAL0OfnumB3r5aNdch4r4CT3Er5Br0PlStAHp";
    public static final Credentials OSUAPI_V2_MOCK_CREDENTIALS =
            new Credentials(OSUAPI_V2_MOCK_CLIENT_ID, OSUAPI_V2_MOCK_CLIENT_SECRET);
    public static final String OSUAPI_V2_MOCK_TOKEN = "osu-oauth-mock-token";

    @Test
    void testGetUserFromName() throws Exception {
        Assertions.assertThat(osuApiV2.getUser("Tillerino", 0)).returns(2070907, ApiUser::getUserId);
    }

    @Test
    void testGetUserFromId() throws Exception {
        Assertions.assertThat(osuApiV2.getUser(2070907, 0)).returns("Tillerino", ApiUser::getUserName);
    }

    @Test
    void testGetUserTop() throws Exception {
        Assertions.assertThat(osuApiV2.getUserTop(2070907, 0, 50)).hasSize(50);
    }

    @Test
    void testGetUserRecent() throws Exception {
        Assertions.assertThat(osuApiV2.getUserRecent(2070907, 0)).isNotNull();
    }

    @Test
    void testGetBeatmap() throws Exception {
        Assertions.assertThat(osuApiV2.getBeatmap(131891, 0)).returns("The Quick Brown Fox", ApiBeatmap::getArtist);
    }
}

package tillerino.tillerinobot;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import tillerino.tillerinobot.data.ApiBeatmap;
import tillerino.tillerinobot.data.ApiUser;

public class OsuApiV1Test extends TestBase {
    public static final String OSUAPI_V1_MOCK_KEY = "1234567890123456789012345678901234567890";

    @Test
    void testGetUserFromName() throws Exception {
        Assertions.assertThat(osuApiV1.getUser("Tillerino", 0)).returns(2070907, ApiUser::getUserId);
    }

    @Test
    void testGetUserFromId() throws Exception {
        Assertions.assertThat(osuApiV1.getUser(2070907, 0)).returns("Tillerino", ApiUser::getUserName);
    }

    @Test
    void testGetUserTop() throws Exception {
        Assertions.assertThat(osuApiV1.getUserTop(2070907, 0, 50)).hasSize(50);
    }

    @Test
    void testGetUserRecent() throws Exception {
        Assertions.assertThat(osuApiV1.getUserRecent(2070907, 0)).isNotNull();
    }

    @Test
    void testGetBeatmap() throws Exception {
        Assertions.assertThat(osuApiV1.getBeatmap(131891, 0)).returns("The Quick Brown Fox", ApiBeatmap::getArtist);
    }
}

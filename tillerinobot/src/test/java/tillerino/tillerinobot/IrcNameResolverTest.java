package tillerino.tillerinobot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import tillerino.tillerinobot.data.UserNameMapping;

public class IrcNameResolverTest extends TestBase {

    @Test
    public void testBasic() throws Exception {
        assertNull(ircNameResolver.resolveIRCName("anybody"));

        db.truncate(UserNameMapping.class);
        MockData.mockUser("anybody", false, 1000, 1000, 1, backend, osuApi, standardRecommender);
        assertNotNull(ircNameResolver.resolveIRCName("anybody"));

        assertThat(db.selectUnique(UserNameMapping.class).execute("where userName = ", "anybody"))
                .hasValueSatisfying(m -> assertThat(m.getUserid()).isEqualTo(1));
    }

    @Test
    public void testFix() throws Exception {
        MockData.mockUser("this_underscore space_bullshit", false, 1000, 1000, 1, backend, osuApi, standardRecommender);
        assertNull(ircNameResolver.resolveIRCName("this_underscore_space_bullshit"));
        ircNameResolver.resolveManually(
                pullThrough.downloadUser("this_underscore space_bullshit").getUserId());
        assertNotNull(ircNameResolver.resolveIRCName("this_underscore_space_bullshit"));
    }
}

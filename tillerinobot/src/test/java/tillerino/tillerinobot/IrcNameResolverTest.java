package tillerino.tillerinobot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import dagger.Component;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.junit.jupiter.api.Test;
import tillerino.tillerinobot.data.UserNameMapping;

public class IrcNameResolverTest extends AbstractDatabaseTest {
    @Component(modules = {DockeredMysqlModule.class, TestBackend.Module.class})
    @Singleton
    interface Injector {
        void inject(IrcNameResolverTest t);
    }

    {
        DaggerIrcNameResolverTest_Injector.create().inject(this);
    }

    @Inject
    TestBackend backend;

    @Inject
    IrcNameResolver resolver;

    @Test
    public void testBasic() throws Exception {
        assertNull(resolver.resolveIRCName("anybody"));

        db.truncate(UserNameMapping.class);
        backend.hintUser("anybody", false, 1000, 1000);
        assertNotNull(resolver.resolveIRCName("anybody"));

        assertThat(db.selectUnique(UserNameMapping.class).execute("where userName = ", "anybody"))
                .hasValueSatisfying(m -> assertThat(m.getUserid()).isEqualTo(1));
    }

    @Test
    public void testFix() throws Exception {
        backend.hintUser("this_underscore space_bullshit", false, 1000, 1000);
        assertNull(resolver.resolveIRCName("this_underscore_space_bullshit"));
        resolver.resolveManually(
                backend.downloadUser("this_underscore space_bullshit").getUserId());
        assertNotNull(resolver.resolveIRCName("this_underscore_space_bullshit"));
    }
}

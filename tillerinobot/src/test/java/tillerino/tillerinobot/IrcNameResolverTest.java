package tillerino.tillerinobot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.inject.Inject;

import org.junit.Test;
import org.tillerino.ppaddict.util.TestModule;

import tillerino.tillerinobot.data.UserNameMapping;

@TestModule(value = TestBackend.Module.class, cache = false)
public class IrcNameResolverTest extends AbstractDatabaseTest {
	@Inject
	TestBackend backend;

	@Inject
	IrcNameResolver resolver;

	@Test
	public void testBasic() throws Exception {
		assertNull(resolver.resolveIRCName("anybody"));

		db.delete(UserNameMapping.class, true);
		backend.hintUser("anybody", false, 1000, 1000);
		assertNotNull(resolver.resolveIRCName("anybody"));

		assertThat(db.loadUnique(UserNameMapping.class, "anybody"))
				.hasValueSatisfying(m -> assertThat(m.getUserid()).isEqualTo(1));
	}

	@Test
	public void testFix() throws Exception {
		backend.hintUser("this_underscore space_bullshit", false, 1000, 1000);
		assertNull(resolver.resolveIRCName("this_underscore_space_bullshit"));
		resolver.resolveManually(backend.downloadUser("this_underscore space_bullshit").getUserId());
		assertNotNull(resolver.resolveIRCName("this_underscore_space_bullshit"));
	}
}

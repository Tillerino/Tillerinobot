package tillerino.tillerinobot.rest;

import static org.hamcrest.CoreMatchers.is;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.tillerino.MockServerRule;
import org.tillerino.MockServerRule.MockServerModule;
import org.tillerino.ppaddict.chat.GameChatClient;
import org.tillerino.ppaddict.rest.AuthenticationServiceImpl.RemoteAuthenticationModule;
import org.tillerino.ppaddict.util.TestClock;
import org.tillerino.ppaddict.util.TestModule;

import io.restassured.RestAssured;
import tillerino.tillerinobot.AbstractDatabaseTest;
import tillerino.tillerinobot.TestBackend;

@TestModule(value = { RemoteAuthenticationModule.class, MockServerModule.class, TestClock.Module.class,
		TestBackend.Module.class }, mocks = { GameChatClient.class, BeatmapsService.class })
public class BeatmapInfoServiceIT extends AbstractDatabaseTest {
	@Inject
	@Rule
	public BotApiRule botApi;

	@Rule
	public MockServerRule mockServer = new MockServerRule();

	@Test
	public void testRegular() throws Exception {
		mockServer.mockJsonGet("/auth/authorization", "{ }", "api-key", "valid-key");

		RestAssured.given().header("api-key", "valid-key")
			.get("/beatmapinfo?wait=2000&beatmapid=129891&mods=0")
			.then()
			.body("beatmapid", is(129891));
	}
}

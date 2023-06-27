package tillerino.tillerinobot.rest;

import static io.restassured.RestAssured.given;
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

		given().header("api-key", "valid-key")
			.get("/beatmapinfo?wait=2000&beatmapid=129891&mods=0")
			.then()
			.body("beatmapid", is(129891));
	}

	@Test
	public void testCors() throws Exception {
		given()
			.header("Origin", "https://tillerino.github.io")
			.options("/botinfo")
			.then()
			.assertThat()
			.statusCode(200)
			.header("Access-Control-Allow-Origin", "https://tillerino.github.io")
			.header("Access-Control-Allow-Headers", "api-key");
	}
}

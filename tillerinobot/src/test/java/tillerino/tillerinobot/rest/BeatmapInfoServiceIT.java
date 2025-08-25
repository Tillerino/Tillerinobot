package tillerino.tillerinobot.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.tillerino.MockServerRule;
import org.tillerino.MockServerRule.MockServerModule;
import org.tillerino.ppaddict.mockmodules.BeatmapsServiceMockModule;
import org.tillerino.ppaddict.mockmodules.GameChatClientMockModule;
import org.tillerino.ppaddict.rest.AuthenticationServiceImpl.RemoteAuthenticationModule;
import org.tillerino.ppaddict.util.TestClock;

import dagger.Component;
import tillerino.tillerinobot.AbstractDatabaseTest;
import tillerino.tillerinobot.TestBackend;

public class BeatmapInfoServiceIT extends AbstractDatabaseTest {
	@Singleton
	@Component(modules = {DockeredMysqlModule.class, RemoteAuthenticationModule.class, MockServerModule.class, TestClock.Module.class,
												TestBackend.Module.class, GameChatClientMockModule.class, BeatmapsServiceMockModule.class })
	interface Injector {
		void inject(BeatmapInfoServiceIT t);
	}
	{
		DaggerBeatmapInfoServiceIT_Injector.create().inject(this);
	}

	@Inject
	@RegisterExtension
	public BotApiRule botApi;

	@RegisterExtension
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

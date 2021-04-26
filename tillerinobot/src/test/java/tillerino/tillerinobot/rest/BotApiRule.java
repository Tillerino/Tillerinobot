package tillerino.tillerinobot.rest;

import javax.inject.Inject;

import io.restassured.RestAssured;

public class BotApiRule extends JdkServerResource {
	@Inject
	public BotApiRule(BotApiDefinition app) {
		super(app, "localhost", 0);
	}

	@Override
	protected void before() throws Throwable {
		super.before();
		RestAssured.baseURI = "http://localhost:" + getPort();
	}
}

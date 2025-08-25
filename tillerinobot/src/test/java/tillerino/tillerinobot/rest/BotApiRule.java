package tillerino.tillerinobot.rest;

import javax.inject.Inject;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.restassured.RestAssured;

public class BotApiRule extends JdkServerResource {
	@Inject
	public BotApiRule(BotApiDefinition app) {
		super(app, "localhost", 0);
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		super.beforeEach(context);
		RestAssured.baseURI = "http://localhost:" + getPort();
	}
}

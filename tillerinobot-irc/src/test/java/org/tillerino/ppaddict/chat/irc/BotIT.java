package org.tillerino.ppaddict.chat.irc;

import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.tillerino.ppaddict.rabbit.RabbitMqContainer;

import io.restassured.RestAssured;

public class BotIT {
	@Before
	public void setUp() throws Exception {
		RabbitMqContainer.RABBIT_MQ.start();
		NgircdContainer.NGIRCD.start();
		IrcContainer.TILLERINOBOT_IRC.start();
		RestAssured.baseURI = "http://" + IrcContainer.TILLERINOBOT_IRC.getHost() + ":"
				+ IrcContainer.TILLERINOBOT_IRC.getMappedPort(8080) + "/";
		Awaitility.await().untilAsserted(() -> RestAssured.when().get("/live").then().statusCode(200));
	}

	@Test
	public void livenessReactsToRabbit() throws Exception {
		RabbitMqContainer.RABBIT_MQ.stop();
		Awaitility.await().untilAsserted(() -> RestAssured.when().get("/live").then().statusCode(503));
		RabbitMqContainer.RABBIT_MQ.start();
		Awaitility.await().untilAsserted(() -> RestAssured.when().get("/live").then().statusCode(200));
	}

	@Test
	public void livenessReactsToNgircd() throws Exception {
		NgircdContainer.NGIRCD.stop();
		Awaitility.await().untilAsserted(() -> RestAssured.when().get("/live").then().statusCode(503));
		NgircdContainer.NGIRCD.start();
		Awaitility.await().untilAsserted(() -> RestAssured.when().get("/live").then().statusCode(200));
	}
}

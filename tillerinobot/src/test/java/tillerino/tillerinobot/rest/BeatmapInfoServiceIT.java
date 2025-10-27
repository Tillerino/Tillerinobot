package tillerino.tillerinobot.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import tillerino.tillerinobot.*;

public class BeatmapInfoServiceIT extends TestBase {
    @RegisterExtension
    public BotApiRule botApi = botApiNoInit;

    @BeforeEach
    public void setUp() throws Exception {
        TestBase.mockBeatmapMetas(diffEstimateProvider);
    }

    @Test
    public void testRegular() {
        wireMock.mockJsonGet("/auth/authorization", "{ }", "api-key", "valid-key");

        given().header("api-key", "valid-key")
                .get("/beatmapinfo?wait=2000&beatmapid=129891&mods=0")
                .then()
                .body("beatmapid", is(129891));
    }

    @Test
    public void testCors() {
        given().header("Origin", "https://tillerino.github.io")
                .options("/botinfo")
                .then()
                .assertThat()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", "https://tillerino.github.io")
                .header("Access-Control-Allow-Headers", "api-key");
    }
}

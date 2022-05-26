package tillerino.tillerinobot.recommendations;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tillerino.MockServerRule;

public class NamePendingApprovalRecommenderTest {
	@Rule
	public final MockServerRule mockServer = new MockServerRule();

	@Test
	public void getRecommendations() throws Exception {
		MockServerRule.mockServer().when(HttpRequest.request("/recommend")
				.withContentType(MediaType.APPLICATION_JSON)
				.withHeader("Authorization", "Bearer my-token")
				.withHeader("User-Agent", "https://github.com/Tillerino/Tillerinobot")
				.withBody(new JsonBody("""
						{
							"topPlays" : [ {
								"beatmapid" : 116128,
								"mods" : 0,
								"pp" : 240.588
							} ],
							"exclude" : [ 456, 116128 ],
							"nomod" : true,
							"requestMods" : 64
						}""")))
				.respond(HttpResponse.response("""
						{
							"recommendations": [ {
								"beatmapId": 2785705,
								"mods": 8,
								"pp": 221,
								"probability": 0.001763579140327404
							} ]
						}
				"""));

		NamePendingApprovalRecommender recommender = new NamePendingApprovalRecommender(
				URI.create(MockServerRule.getExternalMockServerAddress() + "/recommend"), "my-token");
		List<TopPlay> topPlays = List.of(new TopPlay(0, 0, 116128, 0, 240.588));
		Collection<Integer> exclusions = List.of(456);
		Collection<BareRecommendation> loadRecommendations = recommender.loadRecommendations(topPlays, exclusions, Model.NAP, true, 64);
		assertThat(loadRecommendations).singleElement().satisfies(recommendation -> assertThat(recommendation)
				.hasFieldOrPropertyWithValue("beatmapId", 2785705)
				.hasFieldOrPropertyWithValue("mods", 8L)
				.hasFieldOrPropertyWithValue("personalPP", 221)
				.hasFieldOrPropertyWithValue("probability", 0.001763579140327404));
	}
}

package tillerino.tillerinobot.recommendations;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.tillerino.MockServerRule;

public class NamePendingApprovalRecommenderTest {
	@RegisterExtension
	public final MockServerRule mockServer = new MockServerRule();

	@Test
	public void getRecommendations() throws Exception {
		ResponseDefinitionBuilder response = aResponse()
				.withHeader("Content-Type", "application/json")
				.withBody("""
								{
									"recommendations": [ {
										"beatmapId": 2785705,
										"mods": 8,
										"pp": 221,
										"probability": 0.001763579140327404
									} ]
								}
						""");
		MappingBuilder request = post(urlEqualTo("/recommend"))
				.withHeader("Authorization", equalTo("Bearer my-token"))
				.withHeader("User-Agent", equalTo("https://github.com/Tillerino/Tillerinobot"))
				.withRequestBody(equalToJson("""
						{
						"topPlays" : [ {
						"beatmapid" : 116128,
						"mods" : 0,
						"pp" : 240.588
						} ],
						"exclude" : [ 456, 116128 ],
						"nomod" : true,
						"requestMods" : 64
						}"""));
		MockServerRule.mockServer().register(request.willReturn(
				response));

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

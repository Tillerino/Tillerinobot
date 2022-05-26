package tillerino.tillerinobot.recommendations;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.Validate;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.ppaddict.util.MaintenanceException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.UserException;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NamePendingApprovalRecommender implements Recommender {
	private static final ObjectMapper JACKSON = new ObjectMapper()
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

	@Named("nap.url")
	private final URI recommendationsRequestUri;

	@Named("nap.token")
	private final String token;

	@Override
	public List<TopPlay> loadTopPlays(int userId) throws SQLException, MaintenanceException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<BareRecommendation> loadRecommendations(List<TopPlay> topPlays, Collection<Integer> exclude,
			Model model, boolean nomod, long requestMods) throws SQLException, IOException, UserException {
		Validate.isTrue(model == Model.NAP);

		List<Play> mappedPlays = topPlays.stream().map(play -> new Play(play.getBeatmapid(), play.getMods(), play.getPp())).toList();
		List<Integer> allExcludes = Stream.concat(exclude.stream(), topPlays.stream().map(TopPlay::getBeatmapid)).distinct().toList();
		String requestBody = JACKSON.writeValueAsString(new Request(mappedPlays, allExcludes, nomod, requestMods));

		HttpResponse<String> response;
		try {
			response = HttpClient.newHttpClient().send(HttpRequest.newBuilder(recommendationsRequestUri)
					.method("POST", BodyPublishers.ofString(requestBody))
					.header("Content-Type", "application/json")
					.header("Authorization", "Bearer " + token)
					.header("User-Agent", "https://github.com/Tillerino/Tillerinobot")
					.build(),
					BodyHandlers.ofString());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ServiceUnavailableException();
		}
		if (response.statusCode() != 200) {
			log.warn("Received {} {}", response.statusCode(), response.body());
			throw new UserException("This isn't working.");
		}

		return JACKSON.readValue(response.body(), Response.class).recommendations().stream()
			.map(rec -> new BareRecommendation(rec.beatmapId(), rec.mods(), new long[0], (int) rec.pp(), rec.probability()))
			.toList();
	}

	record Request(List<Play> topPlays, List<Integer> exclude, boolean nomod, long requestMods) { }
	record Play(@BeatmapId int beatmapid, @BitwiseMods long mods, double pp) { }

	record Response(List<Recommendation> recommendations) { }
	record Recommendation(@BeatmapId int beatmapId, @BitwiseMods long mods, double probability, double pp) { }
}

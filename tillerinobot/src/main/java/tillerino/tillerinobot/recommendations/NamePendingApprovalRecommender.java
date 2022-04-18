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

import org.apache.commons.lang3.Validate;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.ppaddict.util.MaintenanceException;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.UserException;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NamePendingApprovalRecommender implements Recommender {
	private static final ObjectMapper JACKSON = new ObjectMapper();

	private final URI recommendationsRequestUri;

	@Override
	public List<TopPlay> loadTopPlays(int userId) throws SQLException, MaintenanceException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<BareRecommendation> loadRecommendations(List<TopPlay> topPlays, Collection<Integer> exclude,
			Model model, boolean nomod, long requestMods) throws SQLException, IOException, UserException {
		Validate.isTrue(model == Model.NAP);

		List<Play> mappedPlays = topPlays.stream().map(play -> new Play(play.getBeatmapid(), play.getMods(), play.getPp())).toList();
		List<Integer> allExcludes = Stream.concat(exclude.stream(), topPlays.stream().map(play -> play.getBeatmapid())).distinct().toList();
		String requestBody = JACKSON.writeValueAsString(new Request(mappedPlays, allExcludes, nomod, requestMods));

		HttpResponse<String> response;
		try {
			response = HttpClient.newHttpClient().send(HttpRequest.newBuilder(recommendationsRequestUri)
					.method("POST", BodyPublishers.ofString(requestBody)).header("Content-Type", "application/json").build(),
					BodyHandlers.ofString());
		} catch (InterruptedException e) {
			throw new ServiceUnavailableException();
		}
		if (response.statusCode() != 200) {
			log.warn("Received {}", response.body());
			throw new UserException("This isn't working.");
		}

		return JACKSON.readValue(response.body(), Response.class).recommendations().stream()
			.map(rec -> new BareRecommendation(rec.beatmapId(), rec.mods(), new long[0], null, rec.probability()))
			.toList();
	}

	record Play(@BeatmapId int beatmapid, @BitwiseMods long mods, double pp) { }
	record Request(List<Play> topPlays, List<Integer> exclude, boolean nomod, long requestMods) { }
	record Recommendation(@BeatmapId int beatmapId, @BitwiseMods long mods, double probability) { }
	record Response(List<Recommendation> recommendations) { }
}

package tillerino.tillerinobot.rest;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.RecommendationsManager.GivenRecommendation;

@Singleton
@Path("/history")
public class RecommendationHistoryService {
	private BotBackend backend;
	
	@Inject
	public RecommendationHistoryService(BotBackend server) {
		this.backend = server;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<GivenRecommendation> getHistory(@QueryParam("k") String tillerinobotKey) throws SQLException {
		System.out.println(tillerinobotKey);
		
		Integer userId = backend.resolveUserKey(tillerinobotKey);
		
		if(userId == null)
			return Collections.emptyList();
		
		return backend.loadGivenRecommendations(userId);
	}
}

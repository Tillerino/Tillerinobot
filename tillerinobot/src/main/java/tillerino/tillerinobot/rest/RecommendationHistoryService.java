package tillerino.tillerinobot.rest;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import tillerino.tillerinobot.BotAPIServer;
import tillerino.tillerinobot.RecommendationsManager.GivenRecommendation;

@Path("/history")
public class RecommendationHistoryService {
	private BotAPIServer server;
	
	public RecommendationHistoryService(BotAPIServer server) {
		this.server = server;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<GivenRecommendation> getHistory(@QueryParam("k") String tillerinobotKey) throws SQLException {
		System.out.println(tillerinobotKey);
		
		Integer userId = server.backend.resolveUserKey(tillerinobotKey);
		
		if(userId == null)
			return Collections.emptyList();
		
		return server.backend.loadGivenRecommendations(userId);
	}
}

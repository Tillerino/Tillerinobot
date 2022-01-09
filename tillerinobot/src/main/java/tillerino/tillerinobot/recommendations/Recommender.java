package tillerino.tillerinobot.recommendations;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.util.MaintenanceException;

import tillerino.tillerinobot.UserException;

public interface Recommender {
	List<TopPlay> loadTopPlays(@UserId int userId) throws SQLException, MaintenanceException, IOException;

	/**
	 * @param topPlays base for the recommendation
	 * @param exclude these maps will be excluded (give top50 and previously given recommendations)
	 * @param model selected model
	 * @param nomod don't recommend mods
	 * @param requestMods request specific mods (these will be included, but this won't exclude other mods)
	 */
	public Collection<BareRecommendation> loadRecommendations(List<TopPlay> topPlays, @Nonnull Collection<Integer> exclude, 
			@Nonnull Model model, boolean nomod, @BitwiseMods long requestMods) throws SQLException, IOException, UserException;

}

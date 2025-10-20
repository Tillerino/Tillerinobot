package tillerino.tillerinobot.recommendations;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.RequiredArgsConstructor;
import org.tillerino.ppaddict.util.MaintenanceException;
import tillerino.tillerinobot.UserException;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AllRecommenders implements Recommender {
    @Named("standard")
    private final Recommender standard;

    private final NamePendingApprovalRecommender nap;

    @Override
    public List<TopPlay> loadTopPlays(int userId) throws SQLException, MaintenanceException, IOException {
        return standard.loadTopPlays(userId);
    }

    @Override
    public Collection<BareRecommendation> loadRecommendations(
            List<TopPlay> topPlays, Collection<Integer> exclude, Model model, boolean nomod, long requestMods)
            throws SQLException, IOException, UserException {
        Recommender delegate =
                switch (model) {
                    case NAP -> nap;
                    default -> standard;
                };

        return delegate.loadRecommendations(topPlays, exclude, model, nomod, requestMods);
    }
}

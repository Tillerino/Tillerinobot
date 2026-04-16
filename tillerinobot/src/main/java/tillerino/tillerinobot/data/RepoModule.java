package tillerino.tillerinobot.data;

import org.tillerino.ppaddict.web.data.PpaddictLinkKey;
import org.tillerino.ppaddict.web.data.PpaddictLinkKey$RepoImpl;
import org.tillerino.ppaddict.web.data.PpaddictUser;
import org.tillerino.ppaddict.web.data.PpaddictUser$RepoImpl;

@dagger.Module
public interface RepoModule {
    @dagger.Binds
    ApiScore.Repo apiScoreRepo(ApiScore$RepoImpl impl);

    @dagger.Binds
    ApiUser.Repo apiUserRepo(ApiUser$RepoImpl impl);

    @dagger.Binds
    BotConfig.Repo botConfigRepo(BotConfig$RepoImpl impl);

    @dagger.Binds
    BotUserData.Repo botUserDataRepo(BotUserData$RepoImpl impl);

    @dagger.Binds
    DiffEstimate.Repo diffEstimateRepo(DiffEstimate$RepoImpl impl);

    @dagger.Binds
    Player.Repo playerRepo(Player$RepoImpl impl);

    @dagger.Binds
    ActualBeatmap.Repo actualBeatmapRepo(ActualBeatmap$RepoImpl impl);

    @dagger.Binds
    UserNameMapping.Repo userNameMappingRepo(UserNameMapping$RepoImpl impl);

    @dagger.Binds
    UserTop50Entry.Repo userTop50EntryRepo(UserTop50Entry$RepoImpl impl);

    @dagger.Binds
    GivenRecommendation.Repo givenRecommendationRepo(GivenRecommendation$RepoImpl impl);

    @dagger.Binds
    ApiBeatmap.Repo apiBeatmapRepo(ApiBeatmap$RepoImpl impl);

    @dagger.Binds
    PpaddictLinkKey.Repo ppaddictLinkKeyRepo(PpaddictLinkKey$RepoImpl impl);

    @dagger.Binds
    PpaddictUser.Repo ppaddictUserRepo(PpaddictUser$RepoImpl impl);
}

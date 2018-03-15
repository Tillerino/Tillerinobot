package tillerino.tillerinobot.handlers;

import org.slf4j.MDC;
import org.tillerino.osuApiModel.OsuApiUser;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.IRCBot;
import tillerino.tillerinobot.UserDataManager;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.osutrack.OsutrackDownloader;
import tillerino.tillerinobot.osutrack.Highscore;
import tillerino.tillerinobot.osutrack.UpdateResult;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class OsuTrackHandler extends CommandHandler.WithShorthand {
    private final OsutrackDownloader osutrackDownloader;

    @Inject
    public OsuTrackHandler(OsutrackDownloader osutrackDownloader) {
        super("update");
        this.osutrackDownloader = osutrackDownloader;
    }

    @Override
    public Response handleArgument(String remaining, OsuApiUser apiUser, UserDataManager.UserData userData) throws UserException, IOException, SQLException, InterruptedException {
        MDC.put(IRCBot.MDC_HANDLER, "u");

        String username = remaining.isEmpty() ? apiUser.getUserName() : remaining.trim();
        UpdateResult update = osutrackDownloader.getUpdate(username);

        return updateResultToResponse(update);
    }

    public static Response updateResultToResponse(UpdateResult update) {
        if (!update.isExists()) {
            return new Success(String.format("The user %s can't be found.  Try replaced spaces with underscores and try again.", update.getUsername()));
        }

        if (update.isFirst()) {
            return new Success(String.format("%s is now tracked.  Gain some PP and !update again!", update.getUsername()));
        }

        String mainMessage = String.format(Locale.US, "Rank: %+d (%+1.2f pp) in %d plays. | View detailed data on [https://ameobea.me/osutrack/user/%s osu!track].",
                update.getPpRank()*-1,
                update.getPpRaw(),
                update.getPlayCount(),
                update.getUsername()
        );
        Response response = new Success(mainMessage);
        if (update.getNewHighscores() != null && !update.getNewHighscores().isEmpty()) {
            List<Highscore> newHighscores = update.getNewHighscores();
            StringBuilder highscoreMessageBuilder = new StringBuilder();
            highscoreMessageBuilder.append(newHighscores.size());
            highscoreMessageBuilder.append(" new highscore");
            if (newHighscores.size() > 1) {
                highscoreMessageBuilder.append('s');
            }
            highscoreMessageBuilder.append(newHighscores.size() < 4 ? ':' : '.');
            int count = 0;
            for (Highscore newHighscore : newHighscores) {
                if (count <= 2) {
                    highscoreMessageBuilder.append(String.format(Locale.US, "[https://osu.ppy.sh/b/%d #%d]: %1.2fpp; ",
                            newHighscore.getBeatmapId(),
                            newHighscore.getRanking() + 1,
                            newHighscore.getPp()
                    ));
                    ++count;
                } else if(count == 3) {
                    highscoreMessageBuilder.append("More omitted. ");
                    break;
                }
            }

            highscoreMessageBuilder.append(String.format("View your recent hiscores on [https://ameobea.me/osutrack/user/%s osu!track].", update.getUsername()));
            response = response.then(new Message(highscoreMessageBuilder.toString()));
        }
        if (update.isLevelUp()) {
            response = response.then(new Message("Congratulations on leveling up!"));
        }
        return response;
    }
}

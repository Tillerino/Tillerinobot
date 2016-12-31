package tillerino.tillerinobot.handlers;

import lombok.RequiredArgsConstructor;
import org.tillerino.osuApiModel.OsuApiUser;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.UserDataManager;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.osutrack.Downloader;
import tillerino.tillerinobot.osutrack.Highscore;
import tillerino.tillerinobot.osutrack.UpdateResult;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.SQLException;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class OsuTrackHandler implements CommandHandler {
    private final Downloader osutrackDownloader;


    @Override
    public Response handle(String originalCommand, OsuApiUser apiUser, UserDataManager.UserData userData) throws UserException, IOException, SQLException, InterruptedException {
        String lowerCase = originalCommand.toLowerCase();

        // welp that copy from RecommendHandler :P
        final String username;
        searchRecommend:
        {
            if (lowerCase.equals("u")) {
                username = apiUser.getUserName();
                break searchRecommend;
            }
            if (getLevenshteinDistance(lowerCase, "update") <= 2) {
                username = apiUser.getUserName();
                break searchRecommend;
            }
            if (lowerCase.startsWith("u ")) {
                username = originalCommand.substring(2);
                break searchRecommend;
            }
            if (lowerCase.contains(" ")) {
                int pos = lowerCase.indexOf(' ');
                if (getLevenshteinDistance(lowerCase.substring(0, pos), "update") <= 2) {
                    username = originalCommand.substring(pos + 1);
                    break searchRecommend;
                }
            }
            return null;
        }

        UpdateResult update = osutrackDownloader.getUpdate(username.trim());
        if (!update.isExists()) {
            return new Success(String.format("The user %s can't be found.  Try replaced spaces with underscores and try again.", update.getUsername()));
        } else if (update.isFirst()) {
            return new Success(String.format("%s is now tracked.  Gain some PP and !update again!", update.getUsername()));
        } else {
            String mainMessage = String.format("Rank: %s (%s pp) in %d plays. | View detailed data on [https://ameobea.me/osutrack/user/%s osu!track].",
                    addSign(update.getPpRank() * -1),
                    addSign(update.getPpRaw()),
                    update.getPlayCount(),
                    update.getUsername()
            );
            Response response = new Success(mainMessage);
            if (update.getNewHighscores() != null && update.getNewHighscores().length > 0) {
                Highscore[] newHighscores = update.getNewHighscores();
                StringBuilder highscoreMessageBuilder = new StringBuilder();
                highscoreMessageBuilder.append(newHighscores.length);
                highscoreMessageBuilder.append(" new highscore");
                if (newHighscores.length > 1) {
                    highscoreMessageBuilder.append('s');
                }
                highscoreMessageBuilder.append(newHighscores.length < 4 ? ':' : '.');
                int count = 0;
                for (Highscore newHighscore : newHighscores) {
                    if (count <= 2) {
                        highscoreMessageBuilder.append(String.format("[https://osu.ppy.sh/b/%d #%d]: %fpp; ",
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

    private String addSign(int i) {
        StringBuilder sb = new StringBuilder();
        if (i >= 0) {
            sb.append('+');
        }
        return sb.append(i).toString();
    }

    private String addSign(float f) {
        StringBuilder sb = new StringBuilder();
        if (f >= 0) {
            sb.append('+');
        }
        return sb.append(f).toString();
    }
}

package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ServerErrorException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Message;
import org.tillerino.ppaddict.chat.GameChatResponse.Success;
import org.tillerino.ppaddict.util.MdcUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.UserDataManager;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.osutrack.Highscore;
import tillerino.tillerinobot.osutrack.OsutrackDownloader;
import tillerino.tillerinobot.osutrack.UpdateResult;

public class OsuTrackHandler extends CommandHandler.WithShorthand {
    private final OsutrackDownloader osutrackDownloader;
    private final BotBackend backend;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "injection")
    @Inject
    public OsuTrackHandler(OsutrackDownloader osutrackDownloader, BotBackend backend) {
        super("update");
        this.osutrackDownloader = osutrackDownloader;
        this.backend = backend;
    }

    @Override
    public GameChatResponse handleArgument(String originalCommand, String remaining, OsuApiUser apiUser, UserDataManager.UserData userData, Language lang) throws UserException, IOException, SQLException, InterruptedException {
        MDC.put(MdcUtils.MDC_HANDLER, "u");

        int userId;
        if (remaining.isEmpty()) {
            userId = apiUser.getUserId();
        } else {
            // query someone else
            OsuApiUser otherApiUser = backend.downloadUser(remaining);
            if(otherApiUser == null) {
                return new Success(String.format("User %s does not exist", remaining));
            }
            userId = otherApiUser.getUserId();
        }

        UpdateResult update;
        try {
            update = osutrackDownloader.getUpdate(userId);
        } catch (NotFoundException e) {
            throw new UserException("osu!track doesn't know you. Try searching for your user here first: https://ameobea.me/osutrack/");
        } catch (ServerErrorException e) {
            throw new UserException("osu!track doesn't seem to be working right now. Maybe try your luck on the website: https://ameobea.me/osutrack/");
        } catch (Exception e) {
            // i/o exceptions are generally retryable
            if (ExceptionUtils.getRootCause(e) instanceof IOException) {
                throw new UserException("I couldn't reach osu!track. Try again?");
            }
            throw e;
        }

        return updateResultToResponse(update);
    }

    public static GameChatResponse updateResultToResponse(UpdateResult update) {
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
                URLEncoder.encode(update.getUsername(), StandardCharsets.UTF_8)
        );
        GameChatResponse response = new Success(mainMessage);
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

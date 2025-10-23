package tillerino.tillerinobot.handlers;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Success;
import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.util.MdcUtils;
import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.diff.DiffEstimateProvider;
import tillerino.tillerinobot.diff.PercentageEstimates;
import tillerino.tillerinobot.lang.Language;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NPHandler implements CommandHandler {
    static final Pattern npPattern = Pattern.compile("(?:is listening to|is watching|is playing|is editing)"
            + " \\[https?://osu.ppy.sh"
            // new style: mode is optional, set urls have no beatmap id.
            + "(/beatmapsets/\\d+(#(?<mode>[a-z]+)?/?(?<newid>\\d+))?"
            // old style
            + "|/(?<idtype>b|s)/(?<oldid>\\d+))"
            + ".*\\]"
            + "(?<mods>(?: "
            + "(?:"
            + "-Easy|-NoFail|-HalfTime"
            + "|\\+HardRock|\\+SuddenDeath|\\+Perfect|\\+DoubleTime|\\+Nightcore|\\+Hidden|\\+Flashlight"
            + "|~Relax~|~AutoPilot~|-SpunOut|\\|Autoplay\\|" + "))*)");

    private final LiveActivity live;
    private final DiffEstimateProvider diffEstimateProvider;

    @Override
    public GameChatResponse handle(String message, OsuApiUser apiUser, UserData userData, Language lang)
            throws UserException, IOException, SQLException, InterruptedException {
        MDC.put(MdcUtils.MDC_HANDLER, "np");

        BeatmapWithMods pair = parseNP(message, lang);

        if (pair == null) return null;

        MdcUtils.getLong(MdcUtils.MDC_EVENT).ifPresent(eventId -> live.propagateMessageDetails(eventId, "/np"));

        pair = pair.withMods(userData.addLazer(pair.mods()));
        BeatmapMeta beatmap = diffEstimateProvider.loadBeatmap(pair.beatmap(), pair.mods(), lang);

        if (beatmap == null) {
            throw new UserException(lang.unknownBeatmap());
        }

        PercentageEstimates estimates = beatmap.getEstimates();

        String addition = null;
        if (estimates.getMods() != pair.mods()) {
            addition = "(" + lang.noInformationForModsShort() + ")";
        }
        userData.setLastSongInfo(new BeatmapWithMods(pair.beatmap(), beatmap.getMods()));
        return new Success(beatmap.formInfoMessage(false, true, addition, userData.getHearts(), null, null, null))
                .then(lang.optionalCommentOnNP(apiUser, beatmap));
    }

    @CheckForNull
    @SuppressFBWarnings(value = "TQ", justification = "parser")
    public BeatmapWithMods parseNP(String message, Language lang) throws UserException {
        Matcher m = npPattern.matcher(message);

        if (!m.matches()) {
            return null;
        }

        String anyId = StringUtils.defaultIfBlank(m.group("oldid"), m.group("newid"));
        if (anyId == null || "s".equals(m.group("idtype"))) {
            throw new UserException(lang.isSetId());
        }

        if (!StringUtils.defaultIfBlank(m.group("mode"), "osu").equals("osu")) {
            throw new UserException("where osu");
        }

        int beatmapid = Integer.parseInt(anyId);

        long mods = 0;

        Pattern words = Pattern.compile("\\w+");

        Matcher mWords = words.matcher(m.group("mods"));

        while (mWords.find()) {
            Mods mod = Mods.valueOf(mWords.group());

            if (mod.isEffective()) mods |= Mods.getMask(mod);
        }

        return new BeatmapWithMods(beatmapid, mods);
    }
}

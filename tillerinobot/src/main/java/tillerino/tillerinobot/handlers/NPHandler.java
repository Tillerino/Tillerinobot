package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.inject.Inject;

import org.slf4j.MDC;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.IRCBot;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.diff.PercentageEstimates;
import tillerino.tillerinobot.lang.Language;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class NPHandler implements CommandHandler {
	static final Pattern npPattern = Pattern
			.compile("(?:is listening to|is watching|is playing|is editing) \\[https?://osu.ppy.sh/(?<idtype>b|s)/(?<id>\\d+).*\\](?<mods>(?: "
					+ "(?:"
					+ "-Easy|-NoFail|-HalfTime"
					+ "|\\+HardRock|\\+SuddenDeath|\\+Perfect|\\+DoubleTime|\\+Nightcore|\\+Hidden|\\+Flashlight"
					+ "|~Relax~|~AutoPilot~|-SpunOut|\\|Autoplay\\|" + "))*)");

	BotBackend backend;

	@Inject
	public NPHandler(BotBackend backend) {
		super();
		this.backend = backend;
	}

	@Override
	public Response handle(String message, OsuApiUser apiUser, UserData userData) throws UserException, IOException, SQLException, InterruptedException {
		MDC.put(IRCBot.MDC_HANDLER, "np");
		
		Language lang = userData.getLanguage();

		BeatmapWithMods pair = parseNP(message, lang);

		if (pair == null)
			return null;

		BeatmapMeta beatmap = backend.loadBeatmap(pair.getBeatmap(),
				pair.getMods(), lang);

		if (beatmap == null) {
			throw new UserException(lang.unknownBeatmap());
		}

		PercentageEstimates estimates = beatmap.getEstimates();

		String addition = null;
		if (estimates.getMods() != pair.getMods()) {
			addition = "(" + lang.noInformationForModsShort() + ")";
		}

		return new Success(beatmap.formInfoMessage(false,
				addition, userData.getHearts(), null, null, null)).thenRun(
				() -> userData.setLastSongInfo(new BeatmapWithMods(pair
						.getBeatmap(), beatmap.getMods()))).then(
				lang.optionalCommentOnNP(apiUser, beatmap));
	}

	@CheckForNull
	@SuppressFBWarnings(value = "TQ", justification = "parser")
	public BeatmapWithMods parseNP(String message, Language lang) throws UserException {
		Matcher m = npPattern.matcher(message);

		if (!m.matches()) {
			return null;
		}
		
		if(m.group("idtype").equals("s")) {
			throw new UserException(lang.isSetId());
		}

		int beatmapid = Integer.parseInt(m.group("id"));

		long mods = 0;

		Pattern words = Pattern.compile("\\w+");

		Matcher mWords = words.matcher(m.group("mods"));

		while (mWords.find()) {
			Mods mod = Mods.valueOf(mWords.group());

			if (mod.isEffective())
				mods |= Mods.getMask(mod);
		}

		return new BeatmapWithMods(beatmapid, mods);
	}
}

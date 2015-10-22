package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.BeatmapMeta.PercentageEstimates;
import tillerino.tillerinobot.IRCBot.CommandHandler;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.lang.Language;

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
	public boolean handle(String message, IRCBotUser user, OsuApiUser apiUser,
			UserData userData) throws UserException, IOException, SQLException, InterruptedException {
		Language lang = userData.getLanguage();

		BeatmapWithMods pair = parseNP(message, lang);

		if (pair == null)
			return false;

		BeatmapMeta beatmap = backend.loadBeatmap(pair.getBeatmap(),
				pair.getMods(), lang);

		if (beatmap == null) {
			user.message(lang.unknownBeatmap());
			return true;
		}

		PercentageEstimates estimates = beatmap.getEstimates();

		String addition = null;
		if (estimates.getMods() != pair.getMods()) {
			addition = "(" + lang.noInformationForModsShort() + ")";
		}

		if (user.message(beatmap.formInfoMessage(false, addition,
				userData.getHearts(), null, null, null))) {
			userData.setLastSongInfo(new BeatmapWithMods(pair.getBeatmap(),
					beatmap
					.getMods()));

			lang.optionalCommentOnNP(user, apiUser, beatmap);
		}

		return true;
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

		BeatmapWithMods pair = new BeatmapWithMods(beatmapid, mods);
		return pair;
	}
}

package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.slf4j.MDC;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.IRCBot;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.UserException.RareUserException;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.lang.Language;

public class AccHandler implements CommandHandler {
	BotBackend backend;
	
	@Inject
	public AccHandler(BotBackend backend) {
		super();
		this.backend = backend;
	}

	static Pattern extended = Pattern.compile("(\\d+(?:\\.\\d+)?)%?\\s+(\\d+)x\\s+(\\d+)m", Pattern.CASE_INSENSITIVE);
	static Pattern superExtended = Pattern.compile("(\\d+)x100\\s+(?:(\\d+)x50\\s+)?(\\d+)x\\s+(\\d+)m", Pattern.CASE_INSENSITIVE);

	@Override
	public Response handle(String message, OsuApiUser apiUser,
			UserData userData) throws UserException,
			IOException, SQLException, InterruptedException {
		if (!message.toLowerCase().startsWith("acc")) {
			return null;
		}
		
		MDC.put(IRCBot.MDC_HANDLER, "acc");
		
		BeatmapWithMods lastSongInfo = userData.getLastSongInfo();
		Language lang = userData.getLanguage();
		if (lastSongInfo == null) {
			throw new UserException(lang.noLastSongInfo());
		}
		BeatmapMeta beatmap = backend.loadBeatmap(lastSongInfo.getBeatmap(), lastSongInfo.getMods(), lang);
		if (beatmap == null) {
			throw new RareUserException(lang.excuseForError());
		}

		message = message.substring(3).trim().replace(',', '.');
		Matcher extendedMatcher = extended.matcher(message);
		Matcher superExtendedMatcher = superExtended.matcher(message);
		if(extendedMatcher.matches()) {
			double acc = parseAcc(extendedMatcher.group(1), lang);
			int combo = parseInt(extendedMatcher.group(2), lang);
			int misses = parseInt(extendedMatcher.group(3), lang);
			return new Success(beatmap.formInfoMessage(false, null, userData.getHearts(), acc, combo, misses));
		} else if (superExtendedMatcher.matches()) {
			// we're now in superExtended matching, aka drop % and add x100 and x50
			int x100 = parseInt(superExtendedMatcher.group(1), lang);
			String group2 = superExtendedMatcher.group(2);
			int x50 = group2 == null ? 0 : parseInt(group2, lang);
			int combo = parseInt(superExtendedMatcher.group(3), lang);
			int misses = parseInt(superExtendedMatcher.group(4), lang);
			return new Success(beatmap.formInfoMessage(false, null, userData.getHearts(), x100, x50, combo, misses));
		} else {
			if(message.endsWith("%")) {
				message = message.substring(0, message.length() - 1);
			}
			double acc = parseAcc(message, lang);
			return new Success(beatmap.formInfoMessage(false, null, userData.getHearts(), acc, null, null));
		}
	}

	public static double parseAcc(String accString, Language lang) throws UserException {
		double acc;
		try {
			acc = Double.parseDouble(accString);
		} catch (Exception e) {
			throw new UserException(lang.invalidAccuracy(accString));
		}
		if (!(acc >= 0 && acc <= 100)) {
			throw new UserException(lang.invalidAccuracy(accString));
		}
		acc = Math.round(acc * 100) / 10000d;
		return acc;
	}
	
	public static int parseInt(String string, Language lang) throws UserException {
		try {
			return Integer.parseInt(string);
		} catch(NumberFormatException e) {
			throw new UserException(lang.invalidChoice(string, "1, 2, 3, ..."));
		}
	}
}

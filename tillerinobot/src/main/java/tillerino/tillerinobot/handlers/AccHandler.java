package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.IRCBot.CommandHandler;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
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

	@Override
	public boolean handle(String message, IRCBotUser user,
			OsuApiUser apiUser, UserData userData) throws UserException,
			IOException, SQLException, InterruptedException {
		if (!message.toLowerCase().startsWith("acc")) {
			return false;
		}
		
		BeatmapWithMods lastSongInfo = userData.getLastSongInfo();
		Language lang = userData.getLanguage();
		if (lastSongInfo == null) {
			throw new UserException(lang.noLastSongInfo());
		}
		message = message.substring(3).trim().replace(',', '.');
		Matcher extendedMatcher = extended.matcher(message);
		if(extendedMatcher.matches()) {
			double acc = parseAcc(extendedMatcher.group(1), lang);
			int combo = parseInt(extendedMatcher.group(2), lang);
			int misses = parseInt(extendedMatcher.group(3), lang);
			
			BeatmapMeta beatmap = backend.loadBeatmap(lastSongInfo.getBeatmap(), lastSongInfo.getMods(), lang);
			if (beatmap == null) {
				throw new RareUserException(lang.excuseForError());
			}
			user.message(beatmap.formInfoMessage(false, null, userData.getHearts(), acc, combo, misses));
		} else {
			if(message.endsWith("%")) {
				message = message.substring(0, message.length() - 1);
			}
			double acc = parseAcc(message, lang);
			BeatmapMeta beatmap = backend.loadBeatmap(lastSongInfo.getBeatmap(), lastSongInfo.getMods(), lang);
			if (beatmap == null) {
				throw new RareUserException(lang.excuseForError());
			}
			user.message(beatmap.formInfoMessage(false, null, userData.getHearts(), acc, null, null));
		}
		return true;
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

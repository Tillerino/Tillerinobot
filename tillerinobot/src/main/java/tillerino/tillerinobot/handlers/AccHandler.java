package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Inject;

import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.IRCBot.CommandHandler;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.BeatmapMeta.PercentageEstimates;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.lang.Language;

public class AccHandler implements CommandHandler {
	BotBackend backend;
	
	@Inject
	public AccHandler(BotBackend backend) {
		super();
		this.backend = backend;
	}

	@Override
	public boolean handle(String message, IRCBotUser user,
			OsuApiUser apiUser, UserData userData) throws UserException,
			IOException, SQLException {
		if (message.toLowerCase().startsWith("acc")) {
			BeatmapWithMods lastSongInfo = userData.getLastSongInfo();
			Language lang = userData.getLanguage();
			if (lastSongInfo == null) {
				throw new UserException(lang.noLastSongInfo());
			}
			message = message.substring(3).trim().replace(',', '.');
			Double acc = null;
			try {
				acc = Double.parseDouble(message);
			} catch (Exception e) {
				throw new UserException(lang.invalidAccuracy(message));
			}
			if (!(acc >= 0 && acc <= 100)) {
				throw new UserException(lang.invalidAccuracy(message));
			}
			acc = Math.round(acc * 100) / 10000d;
			BeatmapMeta beatmap = backend.loadBeatmap(lastSongInfo.getBeatmap(), lastSongInfo.getMods(), lang);
			if (beatmap == null) {
				throw new UserException(lang.excuseForError());
			}
			user.message(beatmap.formInfoMessage(false, null, userData.getHearts(), acc));
			return true;
		}
		return false;
	}
}

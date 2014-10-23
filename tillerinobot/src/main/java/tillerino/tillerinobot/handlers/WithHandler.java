package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Inject;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.IRCBot.CommandHandler;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Language;

public class WithHandler implements CommandHandler {
	BotBackend backend;

	@Inject
	public WithHandler(BotBackend backend) {
		super();
		this.backend = backend;
	}

	@Override
	public boolean handle(String message, IRCBotUser user,
			OsuApiUser apiUser, UserData userData) throws UserException,
			IOException, SQLException {
		if (message.toLowerCase().startsWith("with")) {
			Language lang = userData.getLanguage();
			
			BeatmapWithMods lastSongInfo = userData.getLastSongInfo();
			if(lastSongInfo == null) {
				throw new UserException(lang.noLastSongInfo());
			}
			message = message.substring(4).trim();
			
			Long mods = Mods.fromShortNamesContinuous(message);
			if (mods == null || mods == 0) {
				throw new UserException(lang.malformattedMods(message));
			}
			BeatmapMeta beatmap = backend.loadBeatmap(lastSongInfo.getBeatmap(), mods, lang);
			if(beatmap.getMods() == 0) {
				throw new UserException(lang.noInformationForMods());
			}
			
			if(user.message(beatmap.formInfoMessage(false, null, userData.getHearts(), null))) {
				lang.optionalCommentOnWith(user, apiUser, beatmap);

				userData.setLastSongInfo(new BeatmapWithMods(beatmap.getBeatmap().getBeatmapId(), beatmap.getMods()));
			}
			return true;
		}
		return false;
	}

}

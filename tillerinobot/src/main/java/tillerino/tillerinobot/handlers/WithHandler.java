package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;

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
import tillerino.tillerinobot.UserException.RareUserException;
import tillerino.tillerinobot.lang.Language;

public class WithHandler implements CommandHandler {
	BotBackend backend;

	@Inject
	public WithHandler(BotBackend backend) {
		super();
		this.backend = backend;
	}

	@Override
	public Response handle(String message, OsuApiUser apiUser,
			UserData userData) throws UserException,
			IOException, SQLException, InterruptedException {
		if (!message.toLowerCase().startsWith("with")) {
			return null;
		}

		MDC.put(IRCBot.MDC_HANDLER, "with");
		
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
		if (beatmap == null) {
			throw new RareUserException(lang.excuseForError());
		}
		if (beatmap.getMods() == 0) {
			throw new UserException(lang.noInformationForMods());
		}
		
		return new Message(beatmap.formInfoMessage(false, null,
				userData.getHearts(), null, null, null)).thenRun(
				() -> userData.setLastSongInfo(new BeatmapWithMods(beatmap
						.getBeatmap().getBeatmapId(), beatmap.getMods())))
				.then(lang.optionalCommentOnWith(apiUser, beatmap));
	}

}

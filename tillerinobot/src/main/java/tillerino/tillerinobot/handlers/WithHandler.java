package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Inject;

import org.slf4j.MDC;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Message;
import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.util.MdcUtils;

import lombok.RequiredArgsConstructor;
import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.UserException.RareUserException;
import tillerino.tillerinobot.lang.Language;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WithHandler implements CommandHandler {
	private final BotBackend backend;

	private final LiveActivity live;

	@Override
	public GameChatResponse handle(String originalMessage, OsuApiUser apiUser,
			UserData userData, Language lang) throws UserException,
			IOException, SQLException, InterruptedException {
		if (!originalMessage.toLowerCase().startsWith("with")) {
			return null;
		}

		MDC.put(MdcUtils.MDC_HANDLER, "with");

		BeatmapWithMods lastSongInfo = userData.getLastSongInfo();
		if(lastSongInfo == null) {
			throw new UserException(lang.noLastSongInfo());
		}
		String message = originalMessage.substring(4).trim();
		
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

		MdcUtils.getLong(MdcUtils.MDC_EVENT).ifPresent(eventId -> live.propagateMessageDetails(eventId, "!" + originalMessage));

		userData.setLastSongInfo(new BeatmapWithMods(beatmap
				.getBeatmap().getBeatmapId(), beatmap.getMods()));
		return new Message(beatmap.formInfoMessage(false, null,
				userData.getHearts(), null, null, null))
				.then(lang.optionalCommentOnWith(apiUser, beatmap));
	}

}

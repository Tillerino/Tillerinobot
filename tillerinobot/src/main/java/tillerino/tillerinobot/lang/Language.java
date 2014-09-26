package tillerino.tillerinobot.lang;

import java.util.List;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.IRCBot.IRCBotUser;

/**
 * 
 * 
 * @author Tillerino
 */
public interface Language {
	String unknownBeatmap();

	String exception(String marker);

	String noInformationForModsShort();

	void welcomeUser(IRCBotUser user, OsuApiUser apiUser, long inactiveTime);

	String unknownCommand(String command);

	String noInformationForMops();

	String malformattedMods();

	String noLastSongInfo();

	String tryWithMods();

	String tryWithMods(List<Mods> mods);

	String unresolvableName(String exceptionMarker);

	String excuseForError();

	String complaint();

	void hug(IRCBotUser user, OsuApiUser apiUser);

	String help();

	String faq();

	String unknownRecommendationParameter(String param);

	String featureRankRestricted(String feature, int minRank);

	String mixedNomodAndMods();

	String outOfRecommendations();

	String notRanked();

}
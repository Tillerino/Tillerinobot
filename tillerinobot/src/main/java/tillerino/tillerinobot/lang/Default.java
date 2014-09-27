package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;

public class Default implements Language {

	@Override
	public String unknownBeatmap() {
		return "I'm sorry, I don't know that map. It might be very new, very hard, unranked or not standard osu mode.";
	}

	@Override
	public String exception(String marker) {
		return "Something went wrong. If this keeps happening, tell Tillerino to look after incident "
				+ marker + ", please.";
	}

	@Override
	public String noInformationForModsShort() {
		return "no data for requested mods";
	}

	@Override
	public void welcomeUser(IRCBotUser user, OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			user.message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			user.message("Welcome back, " + apiUser.getUsername() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			user.message(apiUser.getUsername() + "...");
			user.message("...is that you? It's been so long!");
			user.message("It's good to have you back. Can I interest you in a recommendation?");
		} else {
			String[] messages = {
					"you look like you want a recommendation.",
					"how nice to see you! :)",
					"my favourite human. (Don't tell the other humans!)",
					"what a pleasant surprise! ^.^",
					"I was hoping you'd show up. All the other humans are lame, but don't tell them I said that! :3",
					"what do you feel like doing today?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			user.message(apiUser.getUsername() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "unknown command " + command
				+ ". type !help if you need help!";
	}

	@Override
	public String noInformationForMods() {
		return "Sorry, I can't provide information for those mods at this time.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "those mods don't look right. mods can be any combination of DT HR HD HT EZ NC FL SO NF. Combine them without any spaces or special chars. Example: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "I don't remember you getting any song info...";
	}

	@Override
	public String tryWithMods() {
		return "Try this map with some mods!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Try this map with " + Mods.toShortNamesContinuous(mods);
	}

	@Override
	public String unresolvableName(String exceptionMarker, String name) {
		return "Your name is confusing me. Did you recently change it? If not, pls contact me and say " + exceptionMarker;
	}

	@Override
	public String excuseForError() {
		return "I'm sorry, there was this beautiful sequence of ones and zeros and I got distracted. What did you want again?";
	}

	@Override
	public String complaint() {
		return "Your complaint has been filed. Tillerino will look into it when he can.";
	}

	@Override
	public void hug(final IRCBotUser user, OsuApiUser apiUser) {
		user.message("Come here, you!");
		user.action("hugs " + apiUser.getUsername());
	}

	@Override
	public String help() {
		return "Hi! I'm the robot who killed Tillerino and took over his account. Jk, but I'm still using the account."
				+ " Check https://twitter.com/Tillerinobot for status and updates!"
				+ " See https://github.com/Tillerino/Tillerinobot/wiki for commands!";
	}

	@Override
	public String faq() {
		return "See https://github.com/Tillerino/Tillerinobot/wiki/FAQ for FAQ!";
	}

	@Override
	public String unknownRecommendationParameter(String param) {
		return "I don't know what \"" + param + "\" is supposed to mean. Try !help if you need some pointers.";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Sorry, at this point " + feature + " is only available for players who have surpassed rank " + minRank + ".";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "nomod with mods?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "I've recommended everything that I can think of. Try again to start over!";
	}

	@Override
	public String notRanked() {
		return "looks like that beatmap is not ranked.";
	}

	@Override
	public void optionalCommentOnNP(IRCBotUser user,
			OsuApiUser apiUser, BeatmapMeta meta) {
		// regular Tillerino doesn't comment on this
	}

	@Override
	public void optionalCommentOnWith(IRCBotUser user, OsuApiUser apiUser,
			BeatmapMeta meta) {
		// regular Tillerino doesn't comment on this
	}

	@Override
	public void optionalCommentOnRecommendation(IRCBotUser user,
			OsuApiUser apiUser, Recommendation meta) {
		// regular Tillerino doesn't comment on this
	}
}

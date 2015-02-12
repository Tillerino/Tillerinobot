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
		return "Maaf, tetapi aku tidak tahu beatmap tersebut. Ia mungkin baru, terlalu susah, belum dikemukakan ataupun bukan mod standard osu.";
	}

	@Override
	public String internalException(String marker) {
		return "Alamak... Tillerino versi manusia mengacau pendawaian aku."
				+ " Jika dia tidak tahu, bolehkah anda [https://github.com/Tillerino/Tillerinobot/wiki/Contact beritahu dia]? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Apa terjadi ini? Aku hanya dapat pengangguan dari pelayan osu. Bolehkah kamu beritahu aku maksud semuanya ini? 0011101001010000"
				+ " Tillerino versi manusia beritahu, anda tidak perlu risau tentang apa-apa, dan kita hanya perlu mencuba sekali lagi."
				+ " Jika anda terlalu risau, anda boleh [https://github.com/Tillerino/Tillerinobot/wiki/Contact beritahu dia] tentang perkara tersebut. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "Tiada data untuk mod yang diminta.";
	}

	@Override
	public void welcomeUser(IRCBotUser user, OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			user.message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			user.message("Selamat datang kembali, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			user.message(apiUser.getUserName() + "...");
			user.message("...adakah itu kamu? Wah, memang lama sudah!");
			user.message("Memang baguslah jumpa kamu lagi. Adakah anda berminat untuk mendengar cadangan aku?");
		} else {
			String[] messages = {
					"Anda kelihatan untuk hendak cadangan.",
					"Memang baiklah berjumpa dengan kamu! :)",
					"Manusia kegemaran aku. (Jangan beritahu manusia yang lain!)",
					"Ini adalah kejutan yang menyenangkan! ^.^",
					"Aku memang berharap yang anda datang. Manusia-manusia yang lain terlalu membosankan saya, tetapi jangan beritahu yang lain yang aku cakap begini! :3",
					"Apakah anda mahu buat hari ini?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			user.message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "unknown command \"" + command
				+ "\". Taip !help jika anda mahu bantuan!";
	}

	@Override
	public String noInformationForMods() {
		return "Maaf, aku tidak boleh menyediakan maklumat untuk mod tersebut untuk masa sekarang.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Kebanyakkan mod tersebut tidak nampak bagus. Mod-mod boleh dikombinasikan dengan DT HR HD HT EZ NC FL SO NF. Anda boleh menggabungkan mod-mod tanpa ruang atau karakter istimewa. Contoh: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Aku tidak ingat anda memberi infomasi lagu tersebut...";
	}

	@Override
	public String tryWithMods() {
		return "Cubalah map ini dengan mod tersebut!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Cubalah map ini dengan " + Mods.toShortNamesContinuous(mods);
	}

	/**
	 * The user's IRC nick name could not be resolved to an osu user id. The
	 * message should suggest to contact @Tillerinobot or /u/Tillerino.
	 * 
	 * @param exceptionMarker
	 *            a marker to reference the created log entry. six or eight
	 *            characters.
	 * @param name
	 *            the irc nick which could not be resolved
	 * @return
	 */
	public String unresolvableName(String exceptionMarker, String name) {
		return "Nama anda membuatkan saya keliru. Adakah kamu dilarangkan? Jika tidak : [https://github.com/Tillerino/Tillerinobot/wiki/Contact hubung Tillerino]. (reference "
				+ exceptionMarker + ")";
	}

	@Override
	public String excuseForError() {
		return "Maafkan saya, ada perkara urutan indah daripada kosong dan satu dan aku diganggu. Apa sudah yang anda mahu?";
	}

	@Override
	public String complaint() {
		return "Your complaint has been filed. Tillerino will look into it when he can.";
	}

	@Override
	public void hug(final IRCBotUser user, OsuApiUser apiUser) {
		user.message("Come here, you!");
		user.action("hugs " + apiUser.getUserName());
	}

	@Override
	public String help() {
		return "Hi! I'm the robot who killed Tillerino and took over his account. Just kidding, but I do use the account a lot."
				+ " [https://twitter.com/Tillerinobot status and updates]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki commands]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact contact]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Frequently asked questions]";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Sorry, at this point " + feature + " is only available for players who have surpassed rank " + minRank + ".";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "What do you mean nomod with mods?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "I've recommended everything that I can think of."
				+ " Try other recommendation options or use !reset. If you're not sure, check !help.";
	}

	@Override
	public String notRanked() {
		return "Looks like that beatmap is not ranked.";
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
	
	@Override
	public boolean isChanged() {
		return false;
	}

	@Override
	public void setChanged(boolean changed) {
		
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "Invalid accuracy: \"" + acc + "\"";
	}

	@Override
	public void optionalCommentOnLanguage(IRCBotUser user, OsuApiUser apiUser) {
		user.message("So you like me just the way I am :)");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "I'm sorry, but \"" + invalid
				+ "\" does not compute. Try these: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "The syntax to set a parameter is !set option value. Try !help if you need more pointers.";
	}
}

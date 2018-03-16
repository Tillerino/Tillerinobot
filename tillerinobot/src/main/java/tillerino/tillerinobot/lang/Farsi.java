package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

public class Farsi implements Language {

	@Override
	public String unknownBeatmap() {
		return "Bebaghshid, wali man in map-o nemishnaasam. Mitoone hamin alaan daroomade baashe, besiaar sakht baashe, yaa shaayad ham asan ranked ya wase standard mode nabaashe!";
	}

	@Override
	public String internalException(String marker) {
		return "Mesike Tillerino man-o ghaati kard."
				+ " Age foori nafahmid, mitooni behesh begi? @Tillerino ya /u/Tillerino? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return " Injaa che khabare? Server-e osu faghat alaki chizmiz behem mide. To mifahmi in yani chi? 0011101001010000"
				+ " Agha Tillerino mige ke hich chizi nashod, kaari ke mikhaasti bokoni dobaare bokon."
				+ " Age waaghan mitarsi chizi kharaabe, behesh begoo: @Tillerino or /u/Tillerino. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "Waase in mod-ha chizi maloom nist.";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			return new Message("bip boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Khosh aamadi, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...in toi? Kheiliwakhte hamdigaro nadidim!"))
				.then(new Message("Didamet hal kardam. Mikhai recommendation-i behet bedam?"));
		} else {
			String[] messages = {
					"to hatman ye recommendation-i mikhai, haan?",
					"baabaajaan, hei naaro dige! :)",
					"ensaane mahboobam. (wali be digaraan nagi ha!)",
					"ajab! ^.^",
					"Kheiliwakhte montazeram. Az hame ensaanhaie dige hoselam sar mire, wali be unha nagi ha! :3",
					"chekarha mikhai bokoni?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "\"" + command
				+ "\" yani chi? !help type kon age komak laazem dari.";
	}

	@Override
	public String noInformationForMods() {
		return "Bebaghshid, wali man alan nemitoonam info-i bedam waase in mod-ha.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "In mod-haro eshteba type nakardi? Mitoonan ye tarkibe DT HR HD HT EZ NC FL SO NF bashe. Combine-eshoon kon bedoone chizi dige, masalan: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Man yaadam nist info-i behet bedam...";
	}

	@Override
	public String tryWithMods() {
		return "In map-o baa chandta mod try kon!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "In map-o try kon baa " + Mods.toShortNamesContinuous(mods);
	}

	@Override
	public String excuseForError() {
		return "Waiiiiiiiiiiiii~ hawaasam part shod! Bebaghshid, lotfan ye baare dige, chi gofti?";
	}

	@Override
	public String complaint() {
		return "Damet garm, ferestaadam. Tillerino negaa mikone ke to dobaare chiro kharaab kardi ;)";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("Bia injaa binam!")
			.then(new Action("ra " + apiUser.getUserName() + " baghal mikone"));
	}

	@Override
	public String help() {
		return "Bedrood~ Man aankesiam ke Tillerino-ro kosht wa account-esho dozdid. Shookhi kardam, wali man hanooz account-amo estefaade mikonam."
				+ " https://twitter.com/Tillerinobot ro check kon waase status-o updates!"
				+ " https://github.com/Tillerino/Tillerinobot/wiki ro bebin waase command-ha!";
	}

	@Override
	public String faq() {
		return "https://github.com/Tillerino/Tillerinobot/wiki/FAQ ra bebin waaseye FAQ!";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Bebaghshid, wali aalan " + feature + " felan player-ha mitoonan estefaade konan ke rank-eshoon baalaatar az " + minRank + " hast.";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "Hawaaseto jam kon, nomod ba mods yani chi~?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Man dige fekram nemirese, harchi toonestam recommend kardam]."
				+ " Age mikhaai, option-haye digei estefaade kon, yaa type kon !reset. Nemidooni chekaar koni, daad bezan !help";
	}

	@Override
	public String notRanked() {
		return "In beatmap ranked ke nist!";
	}
	
	@Override
	public String invalidAccuracy(String acc) {
		return "Accuracy-ro eshtebaa type nakardi? \"" + acc + "\" yani chi?";
	}

	@Override
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		return new Message("ikhebepicmuis Farsi-harfzadano yaadamdaad!"); 
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Bebaghshid, wali natoonestam ino darbiaaram: \"" + invalid
				+ "\". Inaaro estefaade kon: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "Age mikhaai ye setting-io awaz koni, syntax-esh ine: !set option value. Age nemidooni chekhabare, daad bezan !help.";
	}
	
	@Override
	public String apiTimeoutException() {
		return new Default().apiTimeoutException();
	}
	
	@Override
	public String noRecentPlays() {
		return new Default().noRecentPlays();
	}
	
	@Override
	public String isSetId() {
		return new Default().isSetId();
	}
}

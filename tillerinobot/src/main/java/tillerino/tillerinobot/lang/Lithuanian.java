package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;

/**
 * @author Haganenno https://osu.ppy.sh/u/4692344 https://github.com/peraz
 */
public class Lithuanian implements Language {

	@Override
	public String unknownBeatmap() {
		return "Atsiprašau, aš nežinau šito grajaus. Jis yra naujas, labai sunkus, nepatvirtintas arba ne standartiniame Osu žaidimo režime.";
	}

	@Override
	public String internalException(String marker) {
		return "Blemba... žmogus Tillerino sujaukė mano laidus."
				+ " Jeigu jis artimiausiu metu to nepastebės, ar gali apie tai [https://github.com/Tillerino/Tillerinobot/wiki/Contact jį informuoti]? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Kas darosi? Aš gaunu tik nesąmones iš Osu! serverio. Ar galėtum pasakyti, ką tai reiškia? 0011101001010000."
				+ " Žmogus Tillerino sako, kad nėra dėl ko nerimauti ir siūlo pabandyti dar kartą."
				+ " Jeigu dėl kažkokios priežasties jaudiniesi, gal gali [https://github.com/Tillerino/Tillerinobot/wiki/Contact jį informuoti]?. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "Nėra duomenų apie pateiktus modus.";
	}

	@Override
	public void welcomeUser(IRCBotUser user, OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			user.message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			user.message("Sveikas sugrįžęs, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			user.message(apiUser.getUserName() + "...");
			user.message("...ar čia tu? Šimtas metų!");
			user.message("Gera tave vėl matyti. Ar galiu tave sudominti pasiūlymu?");
		} else {
			String[] messages = {
					"atrodo, kad tu nori rekomendacijos.",
					"kaip malonu tave matyti! :)",
					"mano mėgstamiausias žmogus. (Nesakyk kitiems žmonėms!)",
					"koks malonus siurprizas! ^.^",
					"ikėjausi, jog pasirodysi. Kiti žmonės mane užknisa, bet nesakyk, jog aš tai sakiau! :3",
					"ką ruošiesi šiandien veikti?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			user.message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "unknown command \"" + command
				+ "\". Įvesk !help, jei nori pagalbos!";
	}

	@Override
	public String noInformationForMods() {
		return "Atleisk, dabar negaliu pasidalinti informacija apie šiuos modus.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Turbūt ne taip suvedei šiuos modus. Įvedant modus, gali įrašyti bet kokią kombinaciją šių modų: DT HR HD HT EZ NC FL SO NF. Rašyk juos kartu be jokių tarpų, pvz.: !with DTEZ, !with HDHR.";
	}

	@Override
	public String noLastSongInfo() {
		return "Nepamenu, jog tu būtum manęs paklausęs informacijos apie kokią dainą...";
	}

	@Override
	public String tryWithMods() {
		return "Pabandyk šį grajų su kokiais nors modais!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Pabandyk šį grajų su " + Mods.toShortNamesContinuous(mods);
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
		return "Tavo vardas mane trikdo. Gal tu esi užblokuotas? Jei ne, prašau [https://github.com/Tillerino/Tillerinobot/wiki/Contact susisiekti su Tillerino]. (reference "
				+ exceptionMarker + ")";
	}

	@Override
	public String excuseForError() {
		return "Atleisk, radau grąžų sakinį, sudaryta iš nulių ir vienetų ir aš išsiblaškiau. Pakartok dar kartą, ko norėjai?";
	}

	@Override
	public String complaint() {
		return "Tavo nusiskundimas buvo pateiktas. Tillerino jį peržiūrės, kai turės laiko.";
	}

	@Override
	public void hug(final IRCBotUser user, OsuApiUser apiUser) {
		user.message("Ei, tu, ateik čia!");
		user.action("apkabina " + apiUser.getUserName());
	}

	@Override
	public String help() {
		return "Sveikas! Aš esu robotas, kuris nužudė Tillerino ir pavogė jo paskyrą. Nepergyvenk, aš tik juokauju, bet aš tikrai dažnai naudojuosi šia paskyra."
				+ " [https://twitter.com/Tillerinobot būsena ir atnaujinimai]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki komandos]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact kontaktai]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Dažniausiai užduodami klausimai]";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Atsiprašau, šiuo metu " + feature + " yra prieinama žaidėjams, kurie yra pasiekę " + minRank + " vietą.";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "Ką tu turi omeny, sakydamas nomod su modais?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "Aš pasiūliau, viską, ką galiu."
				+ " Pabandyk kitus pasiūlų pasirinkimus arba naudok !reset funkciją. Jei neesi tikras, rašyk !help.";
	}

	@Override
	public String notRanked() {
		return "Panašu, jog šis grajus dar nėra patvirtintas.";
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
		return "Negalimas tikslumas: \"" + acc + "\"";
	}

	@Override
	public void optionalCommentOnLanguage(IRCBotUser user, OsuApiUser apiUser) {
		user.message("Haganenno išmokė mane lietuvių kalbos :)");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Atleisk, bet \"" + invalid
				+ "\" nesiskaito, pabandyk " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "Komanda, kuri nustato parametrą yra !set nustatymo reikšmė. Pabandyk !help, jeigu tau reikia daugiau patarimų. ";
	}
}

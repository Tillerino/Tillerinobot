package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;

/**
 * @author https://osu.ppy.sh/u/Polarni https://github.com/Polarni
 */
public class Czech implements Language {

	@Override
	public String unknownBeatmap() {
		return "Omlouvám se, ale beatmapa není k dispozici. Možná je moc nová, těžká, nehodnocená nebo nepatří do osu! standard.";
	}

	@Override
	public String internalException(String marker) {
		return "Uff... Vypadá to že lidský Tillerino zvoral mé instalace."
				+ " Pokud si toho brzy nevšimne, mohl bys [https://github.com/Tillerino/Tillerinobot/wiki/Contact ho upozornit]? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Co se děje? Dostávám jen nesmyly z osu! serveru. Můžeš mi říct, co to znamená? 0011101001010000"
				+ " Lidský Tillerino říká že se nejedná o nic o co by jsme se museli starat a měli bychom se pokusit znovu."
				+ " Pokud se extra obáváš z nějakého důvodu, můžeš [https://github.com/Tillerino/Tillerinobot/wiki/Contact mu říct] o tom. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "žádné data pro požadovaný mody";
	}

	@Override
	public void welcomeUser(IRCBotUser user, OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			user.message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			user.message("Vítej zpět, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			user.message(apiUser.getUserName() + "...");
			user.message("...jsi to ty? Už je to nějaká doba!");
			user.message("To je dobře, že jsi zpátky. Mohu vás zaujmout doporučením?");
		} else {
			String[] messages = {
					"vypadáš jako že chceš doporučení.",
					"rád tě vidím! :)",
					"můj oblíbený člověk. (Neříkej to ostatním lidem!)",
					"to je příjemné překvapení! ^.^",
					"Doufal jsem že se ukážeš. Ostatní lidi jsou lamy, ale neříkej jim to! :3",
					"Jak se máš?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			user.message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "neznámý příkaz \"" + command
				+ "\". Zadej !help pokud potřebuješ pomoc!";
	}

	@Override
	public String noInformationForMods() {
		return "Omlouvám se, Nemohu poskytnout informace pro tyto mody v tuto chvíli.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Tyto mody nevypadaji dobre. Mody můžou být různé kombinace DT HR HD HT EZ NC FL SO NF. Zkombinuj je bez mezer a speciálních znaků. Například: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Nepamatuji si že můžete dostat info o jakýkoliv písničce...";
	}

	@Override
	public String tryWithMods() {
		return "Zkus tuto mapu s některými mody!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Zkus tuto mapu s " + Mods.toShortNamesContinuous(mods);
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
		return "Tvoje jméno mě mate. Jdi zabanovaný? Pokun n, prosím [https://github.com/Tillerino/Tillerinobot/wiki/Contact kontaktuj Tillerino]. (reference "
				+ exceptionMarker + ")";
	}

	@Override
	public String excuseForError() {
		return "Omlouvám se, byla tam krásná posloupnost jedniček a nul a nechal jsem se rozptýlit. Ješte jednou prosím.";
	}

	@Override
	public String complaint() {
		return "Tvoje stížnost byla podána. Tillerino se na ní podívá hned jak bude moct.";
	}

	@Override
	public void hug(final IRCBotUser user, OsuApiUser apiUser) {
		user.message("Pojď sem, ty!");
		user.action("objetí " + apiUser.getUserName());
	}

	@Override
	public String help() {
		return "Ahoj! Já jsem robot který zabil Tillerino a převzal jeho účet. Dělám si srandu, ale já používám tento účet hodně."
				+ " [https://twitter.com/Tillerinobot status a aktualizace]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki příkazy]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact kontakt]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Často kladené otázky]";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Je mi líto, v tuto chvíli " + feature + " je jen přístup pro hráče, kteří překonali rank " + minRank + ".";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "Co myslíš tím bez modu s modama?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "Doporučil jsem vše, co jsem vymyslel."
				+ " Zkus ostatní možnosti doporučení nebo použij !reset. Pokud si nejsi jistý koukni na !help.";
	}

	@Override
	public String notRanked() {
		return "Vypadá to že beatmapa není hodnocená.";
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
		return "Neplatná přesnost: \"" + acc + "\"";
	}

	@Override
	public void optionalCommentOnLanguage(IRCBotUser user, OsuApiUser apiUser) {
		user.message("Polarni mi pomohl naučit se česky.");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Omlouvám se, ale \"" + invalid
				+ "\" se nepočítá. Zkus tyto: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "Syntaxe pro nastavení parametru je !set option (nastavení) value (hodnota). Zkus !help pokud potřebujete další ukazatele.";
	}
}

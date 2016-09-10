package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

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
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Vítej zpět, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...jsi to ty? Už je to nějaká doba!"))
				.then(new Message("To je dobře, že jsi zpátky. Mohu vás zaujmout doporučením?"));
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
			
			return new Message(apiUser.getUserName() + ", " + message);
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

	@Override
	public String excuseForError() {
		return "Omlouvám se, byla tam krásná posloupnost jedniček a nul a nechal jsem se rozptýlit. Ješte jednou prosím.";
	}

	@Override
	public String complaint() {
		return "Tvoje stížnost byla podána. Tillerino se na ní podívá hned jak bude moct.";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("Pojď sem, ty!")
			.then(new Action("objetí " + apiUser.getUserName()));
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
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Doporučil jsem vše, co jsem vymyslel]."
				+ " Zkus ostatní možnosti doporučení nebo použij !reset. Pokud si nejsi jistý koukni na !help.";
	}

	@Override
	public String notRanked() {
		return "Vypadá to že beatmapa není hodnocená.";
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
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		return new Message("Polarni mi pomohl naučit se česky.");
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
	
	@Override
	public String getPatience() {
		return new Default().getPatience();
	}
}

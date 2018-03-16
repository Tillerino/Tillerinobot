package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

/**
 * @author https://github.com/jerossen https://osu.ppy.sh/u/jeross
 */
public class Dansk implements Language {

	@Override
	public String unknownBeatmap() {
		return "Undskyld, jeg kender ikke den sang. Den er måske meget ny, meget svær, unranked eller ikke standard osu mode.";
	}

	@Override
	public String internalException(String marker) {
		return "Erh... Ser ud til at menneske-Tillerino har skruet mig forkert sammen."
				+ " Hvis han ikke opdager det snart, kan du så ikke lige [https://github.com/Tillerino/Tillerinobot/wiki/Contact informere ham]? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Hvad sker der? Jeg modtager kun vrøvl fra osu serveren. Kan du fortælle mig hvad det her betyder? 0011101001000100"
				+ " Menneske-Tillerino siger at vi ikke skal bekymre os, og at vi bare skal prøve igen."
				+ " Hvis du er meget bekymret af en eller anden grund, kan du [https://github.com/Tillerino/Tillerinobot/wiki/Contact skrive til ham] om det. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "ingen data for forespurgte mods";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Velkommen tilbage, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...er det dig? Det er så længe siden!"))
				.then(new Message("Det er godt at have dig tilbage. Kan jeg interessere dig til en anbefaling?"));
		} else {
			String[] messages = {
					"du ser ud til at ville have en anbefaling.",
					"hvor dejligt at se dig! :)",
					"mit yndlings menneske. (Sig ikke til de andre mennesker!)",
					"hvilken dejlig overraskelse! ^.^",
					"jeg håbede på du ville dukke op. Alle de andre mennesker er kedelige, men sig ikke jeg har sagt det! :3",
					"hvad har du lyst til at lave i dag?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "Ukendt kommando \"" + command
				+ "\". Skriv !help hvis du har brug for hjælp!";
	}

	@Override
	public String noInformationForMods() {
		return "Undskyld, jeg kan ikke levere information til de mods endnu.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "De mods ser ikke rigtige ud. Mods kan være hvilken som helst kombination af DT HR HD HT EZ NC FL SO NF. Kombinér dem uden mellemrum eller specialtegn. Eksempel: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Jeg kan ikke huske du har fået nogen sang information...";
	}

	@Override
	public String tryWithMods() {
		return "Prøv dette beatmap med nogle mods!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Prøv dette beatmep med " + Mods.toShortNamesContinuous(mods);
	}

	@Override
	public String excuseForError() {
		return "Undskyld, der var lige en smuk sekvens af et- og nultaller og jeg blev distraheret. Hvad ville du igen?";
	}

	@Override
	public String complaint() {
		return "Din klage er modtaget. Tillerino ser på det når han kan.";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("Kom her!")
			.then(new Action("krammer " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "Hej! Jeg er robotton der dræbte Tillerino og overtog hans konto. Ej det er gas, men jeg bruger kontoen ret meget."
				+ " [https://twitter.com/Tillerinobot status og opdateringer]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki kommandoer]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact kontakt]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Ofte stillede spørgsmål]";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Undskyld, i øjeblikket er " + feature + " kun tilgængeligt for spillere som er over rang " + minRank + ".";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "Hvad mener du med nomod med mods?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Jeg har anbefalet alt jeg kan komme i tanke om]."
				+ " Prøv en anden anbefaling eller brug !reset. Hvis du ikke er sikker, tjek !help.";
	}

	@Override
	public String notRanked() {
		return "Ser ud til at det beatmap ikke er ranked.";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "Ugyldig accuracy: \"" + acc + "\"";
	}

	@Override
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		return new Message("jeross har hjulpet mig med at lære dansk.");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Undskyld, men \"" + invalid
				+ "\" fungerer ikke. Prøv disse: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "Syntaksen for at sætte en paramereter er !set (indstilling) (variabel). Prøv !help hvis du skal have flere ledetråde.";
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

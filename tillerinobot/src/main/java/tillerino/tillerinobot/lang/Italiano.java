package tillerino.tillerinobot.lang;
 
import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

/**
 * Italian Language implementation. Thanks go to
 * https://osu.ppy.sh/u/marcostudios and https://osu.ppy.sh/u/Howl for this.
 */
public class Italiano implements Language {
 
	@Override
	public String unknownBeatmap() {
		return "Mi spiace, Non conosco quella mappa. Potrebbe essere nuovissima, molto difficile, non rankata oppure potrebbe non essere per la modalità standard di osu.";
	}
 
	@Override
	public String internalException(String marker) {
		return "Ugh... Sembra che Tillerino umano abbia incasinato il programma."
				+ "Se non lo nota subito, puoi informarlo? @Tillerino o /u/Tillerino (in inglese)? (codice di riferimento "
				+ marker + ")";
	}
 
	@Override
	public String externalException(String marker) {
		return "Che succede? Sto ricevendo solo risposte senza senso dai server di osu. Puoi dirmi cosa significa? 0011101001010000"
				+ " - il Tillerino umano dice che non c'è nulla di cui preoccuparsi, e che dovremmo riprovare."
				+ " Se sei super preoccupato per qualche motivo, puoi dirlo a @Tillerino o /u/Tillerino (in inglese). (codice di riferimento "
				+ marker + ")";
	}
 
	@Override
	public String noInformationForModsShort() {
		return "nessun dato per le mod richieste";
	}
 
	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Bentornato, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...sei davvero tu? È passato così tanto tempo!"))
				.then(new Message("È bello rivederti. Ti interessa una raccomandazione?"));
		} else {
			String[] messages = {
					"sembra che tu voglia una raccomandazione.",
					"è bello rivederti! :)",
					"il mio umano preferito. (Non dirlo ad altri umani!)",
					"che piacevole sorpresa! ^.^",
					"speravo che tu arrivassi. Tutti gli altri umani sono stupidi, ma non dire agli altri cosa ti ho detto! :3",
					"come stai oggi?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			return new Message(apiUser.getUserName() + ", " + message);
		}
	}
 
	@Override
	public String unknownCommand(String command) {
		return "\"" + command
				+ "\" è un comando sconosciuto. scrivi !help se hai bisogno di aiuto!";
	}
 
	@Override
	public String noInformationForMods() {
		return "Spiacente, non posso darti informazioni per quelle mod al momento.";
	}
 
	@Override
	public String malformattedMods(String mods) {
		return "Queste mod non sembrano corrette. Le mod possono essere una combinazione di DT HR HD HT EZ NC FL SO NF. Combinale senza alcuno spazio e senza alcun carattere speciale. Esempio: !with HDHR, !with DTEZ";
	}
 
	@Override
	public String noLastSongInfo() {
		return "Non ricordo che tu mi abbia chiesto qualche informazione su delle canzoni...";
	}
 
	@Override
	public String tryWithMods() {
		return "Prova questa mappa con alcune mod!";
	}
 
	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Prova questa mappa con " + Mods.toShortNamesContinuous(mods);
	}
 
	@Override
	public String excuseForError() {
		return "Scusa, c'era una bellissima sequenza di uno e zero e mi sono distratto. Puoi ripetermi che cosa volevi?";
	}
 
	@Override
	public String complaint() {
		return "La tua segnalazione è stato inviata. Tillerino lo guarderà appena riesce.";
	}
 
	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("Ehi tu! Vieni qui!")
			.then(new Action("abbraccia " + apiUser.getUserName()));
	}
 
	@Override
	public String help() {
		return "Ciao! Sono il robot che ha ucciso Tillerino e ha preso possesso del suo account. Scherzo, ma uso lo stesso il suo account."
				+ " Controlla https://twitter.com/Tillerinobot per informazioni sullo stato e aggiornamenti!"
				+ " Guarda https://github.com/Tillerino/Tillerinobot/wiki per i comandi!";
	}
 
	@Override
	public String faq() {
		return "Vedi https://github.com/Tillerino/Tillerinobot/wiki/FAQ per le FAQ!";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Spiacente, per il momento " + feature + " è disponibile per i giocatori che hanno superato il rank " + minRank + ".";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "Cosa intendi per nomod con le mod?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Ti ho raccomandato tutte le mappe che so]."
				+ " Prova altre opzioni o usa !reset. Se non sei sicuro, usa !help.";
	}
 
	@Override
	public String notRanked() {
		// Translation note: marco usa i diocristo di congiuntivi
		// Con tanto amore,
		//						  -- Howl
		return "Sembra che quella mappa non sia rankata.";
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
		return "Accuracy invalida: \"" + acc + "\"";
	}
 
	@Override
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		return new Message("marcostudios e Howl mi hanno insegnato l'italiano <3");
	}
 
	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Mi spiace, ma \"" + invalid
				+ "\" non funziona. Prova questi: " + choices + "!";
	}
 
	@Override
	public String setFormat() {
		return "La sintassi per impostare un parametro è !set opzione valore. Usa !help se ti servono più indicazioni.";
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

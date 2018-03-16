package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

/**
 * @author Till https://github.com/nikosbks https://osu.ppy.sh/u/3619345
 */
public class Greek implements Language {

	@Override
	public String unknownBeatmap() {
		return "Συγνώμη, δεν γνωρίζω αυτό το τραγούδι. Ίσως είναι αρκετά νεο, πολυ δύσκολο, μη εγκεκριμένο ή να μην είναι για το osu standard mode." ;
	}

	@Override
	public String internalException(String marker) {
		return "Εχ... μάλλον φαίνεται ότι ο ανθρώπινος Tillerino έκανε μαντάρα την σύνδεσή μου."
				+" Εάν δεν το παρατηρήσει σύντομα, μπορείς [https://github.com/Tillerino/Tillerinobot/wiki/Contact να τον ενημερώσεις]; (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Τί συμβαίνει; Παίρνω μονο παραλογίες από τον server του osu. Μπορείς να μου πείς τι σημαίνει αυτο; 0011101001010000"
				+ " Ο ανθρώπινος Tillerino λέει ότι δεν υπάρχει κάτι για να ανησυχείς, και ότι πρέπει να ξαναπροσπαθήσουμε."
				+ " Εάν ανησυχείς πάρα  πολύ για κάποιο λογο, μπορείς να [https://github.com/Tillerino/Tillerinobot/wiki/Contact του το πείς]. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "Δεν υπάρχουν δεδομένα για τα ζητούμενα mods." ;

	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Καλώς ήρθες πίσω," + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...είσαι εσύ αυτός; Πάει πολύς καιρός!"))
				.then(new Message("Είναι ωραίο να σε έχουμε πίσω. Μπορώ να σε ενδιαφέρω με μια πρόταση;"));
		} else {
			String[] messages = {
				        "Φαίνεσαι σαν να θες μια πρόταση.",
					"Πόσο ωραίο να σε βλέπω :)",
					"Ο αγαπημένος μου άνθρωπος. (Μην το πείς στούς άλλους!)",
					"Τι ευχάριστη έκπληξη! ^.^",
					"Περίμενα ότι θα εμφανιστείς. Όλοι οι άλλοι άνθρωποι ειναι μπούφοι, αλλα μην τους πείς ότι το ειπα! :3",
					"Τι έχεις την διάθεση να κάνεις σήμερα;",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "Άγνωστη εντολή \"" + command
				+ "\". Πληκτρολόγησε !help αν χρειάζεσαι βοήθεια!";
	}

	@Override
	public String noInformationForMods() {
		return "Συγνώμη, δεν μπορώ να παρέχω πληροφορίες για αυτά τα mods αυτή τη στιγμή";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Αυτα τα mods δεν φαίνονται σωστά. Τα mods μπορεί να είναι ενας συνδυασμός από DT HR HD HT EZ NC FL SO NF.Συνδυάζοντάς τα χωρίς κενά ή ειδικούς χαρακτήρες. Παράδειγμα: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Δεν θυμάμαι να πήρες καμία πληροφορία τραγουδιού...";
	}

	@Override
	public String tryWithMods() {
		return "Δοκίμασε αυτό το τραγούδι με μερικά mods!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Δοκίμασε αυτό το τραγούδι με " + Mods.toShortNamesContinuous(mods);
	}

	@Override
	public String excuseForError() {
		return "Συγνώμη, υπήρχε αυτή η όμορφη σειρά από άσσους και μηδενικά και παρασύρθηκα. Τί ήθελες ξανα;";
	}

	@Override
	public String complaint() {
		return "Το παράπονό σου κατατέθηκε. Ο Tillerino θα το κοιτάξει όταν μπορέσει.";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("Έλα εδώ εσυ!")
			.then(new Action("Αγκαλιάζει " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "Γειά! Είμαι το ρομπότ που σκότωσε τον Tillerino και πήρε τον λογαριασμό του. Πλάκα κάνω, αλλά όντως χρησιμοποιώ τον λογαριασμό αρκετά."
				+ " [https://twitter.com/Tillerinobot status και updates]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki εντολές]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact επικοινωνία]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Συχνά ερωτώμενες ερωτήσεις]";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Συγνώμη, σε αυτό το σημείο " + feature + "είναι μόνο διαθέσιμο για παίκτες που εχουν ξεπερασμένη τάξη " + minRank + ".";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "Τί εννοείς nomods με mods;";
	}
	
	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Έχω προτίνει ό,τι μπορώ να σκεφτώ]. "
				+ " Προσπάθησε άλλες επιλογές προτάσεων ή χρησιμοποίησε το  !rest. Εάν δεν είσαι σίγουρος, έλεγξε το !help.";
	}

	@Override
	public String notRanked() {
		return "Απ' ότι φαίνεται αυτό το τραγούδι δεν είναι εγκεκριμένο.";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "Άκυρη ακρίβεια: \"" + acc + "\"";
	}

	@Override
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		return new Message("Ο N for Niko με βοήθησε να μάθω Ελληνικά");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Συγνώμη, αλλά \"" + invalid
				+ "\" δεν υπολογίζει. Προσπάθησε αυτά: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "Η σύνταξη για να ρυθμιστεί η παράμετρος είναι !set ρύθμιση ποσού. Δοκίμασε !help εάν χρειάζεσαι περισσότερες υποδείξεις.";
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

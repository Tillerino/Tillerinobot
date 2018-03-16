package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

/**
 * TRANSLATION NOTE:
 * 
 * Please put some contact data into the following tag. If any additional
 * messages are required, I'll use the English version in all translations and
 * notify the authors.
 * 
 * @author "Rumberkren" rrr.ee.uu.bb.ee.nnn@gmail.com https://github.com/Rumberkren https://osu.ppy.sh/u/3925053
 * @author "Rendyindo" rendyarya22@gmail.com https://github.com/Rendyindo https://osu.ppy.sh/u/3378391
 */
public class Indonesian extends AbstractMutableLanguage {
	private static final long serialVersionUID = 1L;
	static final Random rnd = new Random();

	@Override
	public String unknownBeatmap() {
		return "Maaf, saya tidak mengetahui map itu. Mungkin itu adalah map yang baru, terlalu sulit, tidak dalam status di rank atau bukan mode osu standar.";
	}

	@Override
	public String internalException(String marker) {
		return "Umm... Sepertinya Tillerino asli mengacaukan pengaturan saya."
				+ " Jika dia tidak segera mengetahuinya, bisakah kau [https://github.com/Tillerino/Tillerinobot/wiki/Contact memberitahunya]? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Apa yang terjadi? Saya hanya mendapatkan omong kosong dari server osu. Bisakah kau memberitahu saya apa yang terjadi? 0011101001010000"
				+ " Tillerino asli mengatakan bahwa ini tidak perlu dikhawatirkan, dan kita harus mencoba lagi."
				+ " Jika kau mencemaskan ini, kau bisa [https://github.com/Tillerino/Tillerinobot/wiki/Contact memberitahunya] tentang hal ini. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "Tidak ada data untuk mod yang diminta.";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if (inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if (inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Selamat datang kembali, " + apiUser.getUserName() + ".");
		} else if (inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...apakah itu kau? Sudah lama sekali!"))
				.then(new Message("Senang bisa menemuimu kembali. Bisakah saya menarik minat anda dalam sebuah rekomendasi?"));
		} else {
			String[] messages = {
					"kau terlihat seperti kau ingin sebuah rekomendasi.",
					"sangat senang bisa menemuimu! :)",
					"manusia favoritku. (Jangan beritahu manusia lain!)",
					"kejutan yang ramah! ^.^",
					"saya berharap kau akan datang. Semua manusia lain payah, tapi jangan beritahu yang lain bahwa saya berkata seperti itu! :3",
					"apa yang anda rasa ingin anda lakukan hari ini?",
			};

			Random random = new Random();

			String message = messages[random.nextInt(messages.length)];

			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "Perintah tidak diketahui \"" + command
				+ "\". Ketik !help jika kau butuh bantuan!";
	}

	@Override
	public String noInformationForMods() {
		return "Maaf, Saya tidak bisa menyediakan informasi untuk mod itu saat ini.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Mod itu tidak terlihat benar. Mod bisa dikombinasi antara DT HR HD HT EZ NC FL SO NF. Kombinasikan mereka tanpa spasi atau karakter spesial. Contoh: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Aku tak ingat memberimu rekomendasi lagu...";
	}

	@Override
	public String tryWithMods() {
		return "Coba map ini dengan beberapa mod!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "coba map ini dengan " + Mods.toShortNamesContinuous(mods) + "!";
	}

	@Override
	public String excuseForError() {
		return "Maaf, ada rangkaian indah dari satu dan nol dan pikiran saya teralihkan. Apa yang tadi kau inginkan?";
	}

	@Override
	public String complaint() {
		return "Komplain anda telah terkirim. Tillerino akan mengeceknya saat dia bisa.";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("Sini, kau!")
			.then(new Action("memeluk " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "Hai! Saya adalah bot yang membunuh Tillerino dan mengambil alih akunnya. Hanya bercanda, tetapi aku sering memakai akun ini."
				+ " [https://twitter.com/Tillerinobot status dan perbaruan]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki perintah]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact Kontak]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Pertanyaan yang sering diajukan (FAQ)]";
	}

	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Maaf, sampai saat ini " + feature + " hanya dapat dipakai untuk pengguna yang telah melewati peringkat " + minRank + ".";
	}

	@Override
	public String mixedNomodAndMods() {
		return "Apa yang anda maksud nomod dengan mod?";
	}

	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Saya telah merekomendasikan apa yang saya bisa pikirkan]."
				+ " Coba gunakan opsi rekomendasi lain atau gunakan !reset. Jika anda tidak yakin, cek !help.";
	}

	@Override
	public String notRanked() {
		return "Sepertinya map itu tidak dalam status di rank.";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "Akurasi yang tidak sah: \"" + acc + "\"";
	}

	@Override
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		/*
		 * TRANSLATION NOTE: This line is sent to the user right after they have
		 * chosen this Language implementation. The English version refers to
		 * itself as the default version ("just the way I am"), so translating
		 * the English message doesn't make any sense.
		 * 
		 * Instead, we've been using the line
		 * "*Translator* helped me learn *Language*." in translations. Replace
		 * *Translator* with your osu name and *Language* with the name of the
		 * language that you are translating to, and translate the line into the
		 * new language. This serves two purposes: It shows that the language
		 * was changed and gives credit to the translator.
		 * 
		 * You don't need to use the line above, and you don't have have to give
		 * yourself credit, but you should show that the language has changed.
		 * For example, in the German translation, I just used the line
		 * "Nichts leichter als das!", which translates literally to
		 * "Nothing easier than that!", which refers to German being my first
		 * language.
		 * 
		 * Tillerino
		 * 
		 * P.S. you can put a link to your profile into the line like this:
		 * [https://osu.ppy.sh/u/2070907 Tillerino]
		 */
		return new Message("Halo! [https://osu.ppy.sh/u/3925053 Rumberkren] dan [https://osu.ppy.sh/u/3378391 Error-] membantuku mempelajari Bahasa Indonesia");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Maaf, tapi \"" + invalid
				+ "\" tidak bisa diperhitungkan. Cobalah ini: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "Syntax untuk mengeset parameter adalah !set opsi nilai. Coba !help jika anda membutuhkan petunjuk.";
	}
	
	StringShuffler apiTimeoutShuffler = new StringShuffler(rnd);
	
	@Override
	public String apiTimeoutException() {
		registerModification();
		final String message = "Server osu! sedang sangat lambat sekarang, jadi tidak ada yang bisa saya lakukan sekarang. ";
		return message + apiTimeoutShuffler.get(
				"Kapan kau terakhir kali bercakap dengan nenekmu?",
				"Bagaimana jika kau membersihkan kamarmu dan tanya lagi?",
				"Kau akan suka untuk jalan-jalan sekarang. Kau tahu... keluar?",
				"Aku baru tahu bahwa engkau memiliki banyak hal untuk dikerjakan. Bagaimana dengan mengerjakannya sekarang?",
				"Kau terlihat seperti kau membutuhkan tidur sebentar.",
				"Cek laman yang sangat menarik di [https://en.wikipedia.org/wiki/Special:Random wikipedia]!",
				"Mari kita cek jika seseorang yang hebat [http://www.twitch.tv/directory/game/Osu! sedang siaran langsung] sekarang!",
				"Ini adalah [http://dagobah.net/flash/Cursor_Invisible.swf game] lain yang mungkin anda payah juga!",
				"Ini seharusnya memberimu waktu yang banyak untuk belajar [https://github.com/Tillerino/Tillerinobot/wiki petunjuk saya].",
				"Jangan khawatir, [https://www.reddit.com/r/osugame dank memes] ini seharusnya bisa melewatkan waktu.",
				"Selagi anda bosan, cobalah [http://gabrielecirulli.github.io/2048/ 2048]!",
				"Pertanyaan: Jika harddrive anda rusak sekarang juga, seberapa banyak data personal anda yang akan hilang selamanya?",
				"Jadi... Sudahkah kamu mencoba [https://www.google.de/search?q=bring%20sally%20up%20push%20up%20challenge Tantangan Sally Up Push Up]?",
				"Anda bisa lakukan hal lain atau kita bisa menatap mata satu sama lain. Dengan diam."
				);
	}

	@Override
	public String noRecentPlays() {
		return "Saya tidak melihatmu main akhir-akhir ini.";
	}
	
	@Override
	public String isSetId() {
		return "Ini merefrensikan sebuah set beatmap, bukan satu beatmap.";
	}
}

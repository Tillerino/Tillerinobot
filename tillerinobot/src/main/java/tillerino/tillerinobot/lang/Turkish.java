package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;

/**
 * @author https://github.com/Lucri https://osu.ppy.sh/u/Lucri
 */
public class Turkish implements Language {

	@Override
	public String unknownBeatmap() {
		return "Özür dilerim,Bu şarkıyı bilmiyorum.Şarkı ya çok yeni olabilir,ya çok zor, ya unranked yada standart osu modu için geçerli değil.";
	}

	@Override
	public String internalException(String marker) {
		return "Ugh... Görünüşe göre insan Tillerino yine bir şeyleri berbat etti.."
				+ " Eğer yakın zamanda farkına varmazsa, Buradan [https://github.com/Tillerino/Tillerinobot/wiki/Contact ona ulaşır mısın]? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Neler Oluyor ? Osu serverinden sadece saçma sapan şeyler alıyorum. Bunun ne anlama geldiğini söyleyebilir misin ? 0011101001010000"
				+ " İnsan Tillerino diyor ki : Bunun için endişelenme, daha sonra tekrar denemelisin."
				+ " Eğer bir sebepten dolayı çok endişelendiysen, Ona buradan [https://github.com/Tillerino/Tillerinobot/wiki/Contact ne olduğunu] söyleyebilirsin. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "Talep edilen mod için bilgi bulunamadı.";
	}

	@Override
	public void welcomeUser(IRCBotUser user, OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			user.message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			user.message("Hoşgeldin!, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			user.message(apiUser.getUserName() + "...");
			user.message("...Bu sen misin? Görmeyeli çok uzun zaman oldu!");
			user.message("Seni tekrardan görmek güzel. Sana bir öneride bulunabilir miyim?");
		} else {
			String[] messages = {
					"Sanki bir tavsiye istiyormuş gibi görünüyorsun.",
					"Seni görmek ne kadar da güzel :)",
					"Benim favori insanım. (Öteki insanlara söyleme!)",
					"Ne hoş sürpriz ama! ^.^",
					"Tamda senin gelmeni umuyordum. Diğer tüm insanlar utanç verici, ama sakın onlar için böyle dediğimi söyleme! :3",
					"Bugün canın ne yapmayı istiyor ?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			user.message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "Bilinmeyen komut \"" + command
				+ "\". Eğer yardıma ihtiyacın varsa !help yazabilirsin!";
	}

	@Override
	public String noInformationForMods() {
		return "Üzgünüm, Şuan bu modlar için bilgi sağlayamıyorum.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Bu modlar doğru gözükmüyor. Modlar sadece DT HR HD HT EZ NC FL SO NF kombinasyonu olabilir . Boşluk olmadan ve özel karakter içermeden kombine etmeyi dene. Örnek: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Daha önce şarkı hakkında bilgi aldığını zannetmiyorum.";
	}

	@Override
	public String tryWithMods() {
		return "Bu mapı modlarla dene!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Bu mapı şu modlarla dene: " + Mods.toShortNamesContinuous(mods);
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
		return "İsmin benim aklımı karıştırıyor.Banlandın mı ? Eğer değilse, Lütfen [https://github.com/Tillerino/Tillerinobot/wiki/Contact ulaş Tillerino]. (reference "
				+ exceptionMarker + ")";
	}

	@Override
	public String excuseForError() {
		return "Üzgünüm, şurada bir dizi çok güzel sıfırlar ve birler vardı ve aklım karıştı. Tekrar ne istediğini söyler misin ?";
	}

	@Override
	public String complaint() {
		return "Şikayetin dosyalandı. Tillerino bakabildiği zaman bir göz atacak.";
	}

	@Override
	public void hug(final IRCBotUser user, OsuApiUser apiUser) {
		user.message("Hey sen, buraya gel !");
		user.action("sarılır " + apiUser.getUserName());
	}

	@Override
	public String help() {
		return "Merhaba! Ben Tillerino'yu öldüren ve hesabını ele geçiren robotum. Şakaydı, ama hesabı baya kullanıyorum ha."
				+ " [https://twitter.com/Tillerinobot status and updates]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki commands]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact contact]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Sıkça sorulan sorular]";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Üzgünüm, Şu anda " + feature + " özelliği sadece belli rütbedekiler için geçerli. " + minRank + ".";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "Nomod with mods derken ne demeye çalışıyorsun ?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "Elimde olan tüm şarkıları sana tavsiye etmiş bulunmaktayım."
				+ " Başka tavsiye seçeneklerini kullan,yada sıfırlamak için !reset yaz. Eğer emin değilsen, !help yazıp göz atıver.";
	}

	@Override
	public String notRanked() {
		return "Görünüşe göre bu beatmap ranked değil.";
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
		return "Geçersiz accuracy: \"" + acc + "\"";
	}

	@Override
	public void optionalCommentOnLanguage(IRCBotUser user, OsuApiUser apiUser) {
		user.message("Lucri bana Türkçe'yi öðrenmeme yardým etti.");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Üzgünüm, fakat \"" + invalid
				+ "\" hesaplanamıyor. Şunları dene: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "Syntaxı ayarlamak için gerekli parametre kod ayarı !set. Eğer daha fazla yardıma ihtiyacın varsa !help dene.";
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

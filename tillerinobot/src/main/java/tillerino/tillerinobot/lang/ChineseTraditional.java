package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

/**
 * @author Tomoka Rin leoyao321@gmail.com https://osu.ppy.sh/u/125308
 */
public class ChineseTraditional extends AbstractMutableLanguage {
	private static final long serialVersionUID = 1L;
	static final Random rnd = new Random();

	@Override
	public String unknownBeatmap() {
		return "抱歉，查詢不到這個譜面的資料唷。這個譜面可能是尚未Rank、過新、太難或非標準模式用的譜面。";
	}

	@Override
	public String internalException(String marker) {
		return "Tillerino搞砸了...。"
				+ " 請稍等片刻，如果沒有復原的話請至 [https://github.com/Tillerino/Tillerinobot/wiki/Contact 告訴我!]。 (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "發生了無法預期的錯誤。"
				+ "Tillerino：不用擔心，讓我們再試一次吧!"
				+ " 如果還是有問題的話請至 [https://github.com/Tillerino/Tillerinobot/wiki/Contact 告訴我!]。 (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "目前找不到該Mod的資料。";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if (inactiveTime < 60 * 1000) {
			return new Message("哈囉!");
		} else if (inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("歡迎回來!， " + apiUser.getUserName() + "。");
		} else if (inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("好久不見了呢!"))
				.then(new Message("很高興看到你回來，需要我推薦你幾首歌重溫感覺嗎?"));
		} else {
			String[] messages = {
					"看起來你很想要我推薦你譜面呢!",
					"很高興能再次看見你上線!",
					"你可是我最關注的人! (噓!別跟別人說!)",
					"這是多麼感動的驚喜!",
					"很高興能夠見到你，千萬別跟別人說呀!",
					"今天準備做什麼呢?",
			};

			Random random = new Random();

			String message = messages[random.nextInt(messages.length)];

			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "不明的指令 \"" + command
				+ "\". 請輸入 !help 獲取更多資訊。";
	}

	@Override
	public String noInformationForMods() {
		return " 抱歉，目前無法提供該Mod的資訊。";
	}

	@Override
	public String malformattedMods(String mods) {
		return "看起來你輸入的Mod有錯唷，以下是各Mod的簡寫(DT、HR、HD、HT、EZ、NC、FL、SO、NF)。可以選擇輸入其中一種，或是組合其中幾個模式; 例如: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "我不記得有給過你任何歌曲資訊呢...";
	}

	@Override
	public String tryWithMods() {
		return "試著加入Mod挑戰這首歌吧!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "試著加上" + Mods.toShortNamesContinuous(mods) + "挑戰這首歌!";
	}

	@Override
	public String excuseForError() {
		return "抱歉，腦海中浮現了大量的1跟0讓我分散了注意力...可以再說一次你的請求嗎?";
	}

	@Override
	public String complaint() {
		return "回報已經送出了! Tillerino有空就會看的!";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("可以來我這邊一下嗎?")
			.then(new Action("抱" + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "嗨!Tillerino已經死亡了...開玩笑的。這個帳號已經被我改造成機器人使用囉。"
				+ " [https://twitter.com/Tillerinobot 更新情報看這邊。]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki 各式指令。]"
				+ " - [http://ppaddict.tillerino.org/ 歌曲詳細資訊]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact 聯絡我。]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ 問與答。]";
	}

	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "抱歉在這個時間 " + feature + " 只開放給 " + minRank + "以上的人使用。";
	}

	@Override
	public String mixedNomodAndMods() {
		return "就是Nomod";
	}

	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " 我會推薦所有想得到適合你的譜面]。"
				+ " 試試看其他推薦譜面的方法或是用 !reset 來重置推薦的譜面難度。如果你不確定功能該如何使用，可以輸入 !help來幫助你。";
	}

	@Override
	public String notRanked() {
		return "這譜面還沒Rank呢。";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "無效的Acc數值。: \"" + acc + "\"";
	}

	@Override
	public Message optionalCommentOnLanguage(OsuApiUser apiUser) {
		return new Message("是Tomoka Rin教我中文的，請多多指教。");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "很抱歉，但 \"" + invalid
				+ "\" 是不正確的。 試試看這個: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return " 要設定變數的話 請輸入 !set option value(數值) 來做更動。"
				+ "更多資訊請輸入 !help 來幫助你。";
	}
	
	StringShuffler apiTimeoutShuffler = new StringShuffler(rnd);
	
	@Override
	public String apiTimeoutException() {
		registerModification();
		final String message = "Osu!伺服器目前連線緩慢，所以我跟你一樣什麼事都不能做...你可以試著...";
		return message + apiTimeoutShuffler.get(
				"泡杯咖啡看看最近有什麼新聞打發時間。",
				"去小睡一下再回來!",
				"來看看現在有誰在實況吧! [http://www.twitch.tv/directory/game/Osu! streaming]",
				"別擔心 ! [https://www.reddit.com/r/osugame dank memes] 這或許能幫助你打發時間!"
				);
	}

	@Override
	public String noRecentPlays() {
		return "有一段時間沒有看到你玩了呢";
	}
	
	@Override
	public String isSetId() {
		return "這似乎是好幾個譜面的資料組成，而不是單一譜面。";
	}
}

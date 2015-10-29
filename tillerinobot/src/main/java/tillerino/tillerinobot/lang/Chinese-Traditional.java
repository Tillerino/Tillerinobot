package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;

/**
 * @author Tomoka Rin leoyao321@gmail.com https://osu.ppy.sh/u/125308
 */
public class Default implements Language {
	static final Random rnd = new Random();

	@Override
	public String unknownBeatmap() {
		return "抱歉，查詢不到這個譜面的資料唷。這譜面可能是尚未Rank、過新、太難、非標準模式用的譜面。";
	}

	@Override
	public String internalException(String marker) {
		return "Tillerino搞砸了...。"
				+ " 稍等片刻，如果沒有復原的話至 [https://github.com/Tillerino/Tillerinobot/wiki/Contact 請告訴我吧!]。 (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "發生了無法預期的錯誤。"
				+ "Tillerino：不用擔心，讓我們再試一次吧!"
				+ " 如果還是很擔心的話至 [https://github.com/Tillerino/Tillerinobot/wiki/Contact 請告訴我吧!]。 (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "目前找不到該Mod的資料D:。";
	}

	@Override
	public void welcomeUser(IRCBotUser user, OsuApiUser apiUser, long inactiveTime) {
		if (inactiveTime < 60 * 1000) {
			user.message("哈囉!");
		} else if (inactiveTime < 24 * 60 * 60 * 1000) {
			user.message("歡迎回來!， " + apiUser.getUserName() + "。");
		} else if (inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			user.message(apiUser.getUserName() + "...");
			user.message(":D 好久不見了呢!");
			user.message("很高興看到你回來，需要讓我推薦你幾首歌重溫感覺嗎?");
		} else {
			String[] messages = {
					"看起來你想要我推薦你譜面呢!",
					"很高興再次看見你上線!",
					"你可是我最關注的人! (噓!別跟別人說!)",
					"這是多麼感動的驚喜!",
					"很高興能夠見到你，千萬別跟別人說呀!",
					"今天準備做什麼呢?",
			};

			Random random = new Random();

			String message = messages[random.nextInt(messages.length)];

			user.message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "不明的指令 \"" + command
				+ "\". 輸入 !help 獲取更多資訊。";
	}

	@Override
	public String noInformationForMods() {
		return " 抱歉D:，目前無法提供該Mod的資訊。";
	}

	@Override
	public String malformattedMods(String mods) {
		return "看起來輸入的Mod字有錯唷，以下是各Mod的簡稱 DT HR HD HT EZ NC FL SO NF。可以選擇輸入單一模式，或是組合其中幾個模式詞句; 例如: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "我不記得有給過你任何歌曲資訊呢...";
	}

	@Override
	public String tryWithMods() {
		return "試著加入Mod挑戰這首歌!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "試著加上" + Mods.toShortNamesContinuous(mods) + "挑戰這首歌!";
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
		return "這個名字讓我困惑了，請問有被Ban過嗎?如果是我誤會了至 [https://github.com/Tillerino/Tillerinobot/wiki/Contact 請連絡我吧!]. (reference "
				+ exceptionMarker + ")";
	}

	@Override
	public String excuseForError() {
		return "抱歉，剛腦海中浮現了大量的1跟0讓我分散了注意力...可以再說一次你的請求嗎?";
	}

	@Override
	public String complaint() {
		return "回報已經送出了! Tillerino有空就會看的!(大概:D ";
	}

	@Override
	public void hug(final IRCBotUser user, OsuApiUser apiUser) {
		user.message("可以來到我這邊一下嗎?");
		user.action("抱" + apiUser.getUserName());
	}

	@Override
	public String help() {
		return "嗨!Tillerino已經死亡了...開玩笑的:D 這個帳號已經被我改造成機器人使用囉。"
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
		return "抱歉在這個時間 " + feature + " 只開放給至 " + minRank + "以上的人使用。";
	}

	@Override
	public String mixedNomodAndMods() {
		return "None類型";
	}

	@Override
	public String outOfRecommendations() {
		return "我會推薦所有想得到合適你的譜面。"
				+ " 試試看其他推薦譜面的方法或是用 !reset 來重置推薦的譜面難度。如果你不確定功能該如何使用，可以輸入 !help來幫助你。";
	}

	@Override
	public String notRanked() {
		return "這譜面還沒Rank呢D:。";
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
		return "無效的Acc數值。: \"" + acc + "\"";
	}

	@Override
	public void optionalCommentOnLanguage(IRCBotUser user, OsuApiUser apiUser) {
		user.message("是Tomoka Rin教我中文的，請多多指教。");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "很抱歉，但 \"" + invalid
				+ "\" 是不正確的。 試試看這個: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return " 要設定變數的話 請輸入 !set option value(數值) 來做更動。
				 更多資訊請輸入 !help 來幫助你。";
	}
	
	StringShuffler doSomething = new StringShuffler(rnd);
	
	@Override
	public String apiTimeoutException() {
		final String message = "Osu!伺服器目前連線緩慢，所以我跟你一樣什麼事都不能做...你可以試著...";
		return message + doSomething.get(
				"泡杯咖啡看看最近有什麼新聞打發時間。",
				"你看起來需要小睡一下呢!",
				"來看看現在有誰在實況吧! [http://www.twitch.tv/directory/game/Osu! streaming]",
				"別擔心 ! [https://www.reddit.com/r/osugame dank memes] 這或許能幫助你打發時間!"
				);
	}

	@Override
	public String noRecentPlays() {
		return "一段時間沒有看到你玩了呢:D";
	}
	
	@Override
	public String isSetId() {
		return "這似乎是好幾個譜面的資料組成，而不是單一譜面。";
	}
	
	@Override
	public String getPatience() {
		return "稍後...";
	}
}

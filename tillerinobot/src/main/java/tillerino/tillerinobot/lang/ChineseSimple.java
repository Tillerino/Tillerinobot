package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

/**
 * @author Deardrops lness@qq.com https://github.com/Deardrops https://osu.ppy.sh/u/1735252
 */
public class ChineseSimple extends AbstractMutableLanguage {
	private static final long serialVersionUID = 1L;
	static final Random rnd = new Random();

	@Override
	public String unknownBeatmap() {
		return "抱歉，现在没有这张图的信息。这张图可能是Unranked，太新或者太难，不是osu!主模式的图。";
	}

	@Override
	public String internalException(String marker) {
		return "唔，看样子Tillerino把我给玩坏了。 ╮(￣▽￣)╭"
				+ " 如果短时间内没有修复，你可以在 [https://github.com/Tillerino/Tillerinobot/wiki/Contact 这里] 反馈！（错误信息： "
				+ marker + "）";
	}

	@Override
	public String externalException(String marker) {
		return "喵喵喵？服务器上出现错误，你能告诉我发生了什么？0011101001010000"
				+ " Tillerino：「别担心，再试一次！」"
				+ " 如果还是有错误，请在 [https://github.com/Tillerino/Tillerinobot/wiki/Contact 这里] 反馈！（错误信息： "
				+ marker + "）";
	}

	@Override
	public String noInformationForModsShort() {
		return "没有这个Mods的信息。";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if (inactiveTime < 60 * 1000) {
			return new Message("嘟嘟噜~");
		} else if (inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("欢迎回来，" + apiUser.getUserName() + "。");
		} else if (inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("是你么? 好久不见呢！"))
				.then(new Message("很高兴再次见到你。需要我为你推荐歌曲么?"));
		} else {
			String[] messages = {
					"你看起来需要我给你推荐图呢！ (๑•̀ㅂ•́)و✧",
					"很高兴再次见到你！ <(*￣▽￣*)/",
					"你是我最喜欢的人！ (*/ω＼*)",
					"这真是个意外的惊喜呢~ (>▽<)",
					"我每天都在期待你的出现 (*/ω＼*)",
					"今天想做些什么呢？ o(￣▽￣)ｄ",
			};

			Random random = new Random();

			String message = messages[random.nextInt(messages.length)];

			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "错误的指令 \"" + command
				+ "\". 请输入 !help 来查看可用指令!";
	}

	@Override
	public String noInformationForMods() {
		return "抱歉，现在没有这个Mods的信息。";
	}

	@Override
	public String malformattedMods(String mods) {
		return "咦？输入的Mod不对。Mods的缩写包括：DT HR HD HT EZ NC FL SO NF。可以任意组合或者使用其中一个。比如：!with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "我不记得有给过你歌曲呢...Σ(っ °Д °;)っ";
	}

	@Override
	public String tryWithMods() {
		return "上Mod玩这张图试试？";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "加上" + Mods.toShortNamesContinuous(mods) + "试试？";
	}

	@Override
	public String excuseForError() {
		return "很抱歉，刚刚我没注意听...可以再说一次你想做的事情么？";
	}

	@Override
	public String complaint() {
		return "你的反馈已经发送。Tillerino很快会收到你的反馈。";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("可以过来一下么？")
			.then(new Action("抱 " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "嗨！我是谋杀了Tillerino后使用他的账号的机器人。"
				+ " [https://twitter.com/Tillerinobot TillerinoBot项目]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki 指令]"
				+ " - [http://ppaddict.tillerino.org/ pp计算]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact 联系我]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ 常见问题]";
	}

	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "抱歉，现在 " + feature + " 只能被Rank在 " + minRank + " 以上的人使用。";
	}

	@Override
	public String mixedNomodAndMods() {
		return "Nomod就是没有mod啦~";
	}

	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " 我会把我知道的合适的图都推荐给你。]."
				+ " 试试别的推荐算法，或者用 !reset 来重置推荐。如果你不确定应该如何使用，请输入 !help 来获取帮助";
	}

	@Override
	public String notRanked() {
		return "看起来这张图是Unranded图。";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "无效的Acc数值： \"" + acc + "\"";
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
		return new Message("是paraia教我中文的，请多关照。");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "很抱歉， \"" + invalid
				+ "\" 是不正确的。试试这个 " + choices + "！";
	}

	@Override
	public String setFormat() {
		return "请输入 !set option 数值 来设置变量。输入 !help 可以获得更多帮助。";
	}
	
	StringShuffler apiTimeoutShuffler = new StringShuffler(rnd);
	
	@Override
	public String apiTimeoutException() {
		registerModification();
		final String message = "老板家的小霸王又被人拖走了，所以什么都做不了的说。 ";
		return message + apiTimeoutShuffler.get(
				"说点什么好呢...你最后一次和奶奶说话是什么时候？",
				"你要不要打扫一下房间再回来看看？",
				"我想你现在应该去外面走走，去外面见见朋友。",
				"我知道你现在有一堆其他事情要做。现在处理它们如何？",
				"你看起来需要休息一会儿。",
				"对了，你最近有看 [https://tieba.baidu.com/p/4817187865 藕酥轶闻录] 么？曲奇又FC了张难图。",
				"让我们看看有没有谁正在 [http://live.bilibili.com/search/index/?keyword=osu 直播osu] ！",
				"瞧，这里有一个同样适合你的 [http://dagobah.net/flash/Cursor_Invisible.swf 游戏] 。",
				"现在有充足的时间去学习如何使用 [https://github.com/Tillerino/Tillerinobot/wiki Tillerinobot]。",
				"别担心，看看 [https://www.reddit.com/r/osugame reddit] 打发时间吧。",
				"无聊的时候，来玩会儿 [http://gabrielecirulli.github.io/2048/ 2048] 吧！",
				"提问：如果现在你的电脑硬件损坏，你会因此丢失多少个人资料？",
				"你可以做点别的事情，或者我们保持安静，就这么相互看着对方。"
				);
	}

	@Override
	public String noRecentPlays() {
		return "有一段时间没见面了呢。";
	}
	
	@Override
	public String isSetId() {
		return "这似乎是一个图包，不是一张图。";
	}
}

package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

/**
 * @author Souen- https://osu.ppy.sh/u/2234772 https://github.com/Soukyuen
 */
public class Japanese implements Language {

	@Override
	public String unknownBeatmap() {
		return "データベースにない譜面です。投稿日が近すぎたり、難易度が非常識だったり、スタンダード用の譜面ではなかったりすると情報を提供する事ができません。";
	}

	@Override
	public String internalException(String marker) {
		return "生身のTillerinoが私の配線に失敗してるかもしれません・・・"
				+ "ある程度待っても改善されなかったら、 [https://github.com/Tillerino/Tillerinobot/wiki/Contact 知らせてください]。 (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "予期していない問題が発生しました。"
				+ " 生身のTillerinoによると「心配ない」だそうです。再度送信を試みてください。"
				+ " もし「心配」だと判断されたのなら、 [https://github.com/Tillerino/Tillerinobot/wiki/Contact 知らせてあげましょう]。 (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "要請されたModに関する情報がありません。";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			return new Message("ﾋﾟﾋﾟｯ");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("また会いましたね、" + apiUser.getUserName() + "さん。");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "さん・・・")
				.then(new Message(apiUser.getUserName() + "さんですか？お久しぶりです。"))
				.then(new Message("少し、おすすめの譜面でも見て行ってくれますか？"));
		} else {
			String[] messages = {
					"おすすめ譜面が欲しい様ですね。",
					"会えて嬉しいです。",
					"私の一番のお気に入りのニンゲンです。（他のニンゲンには内緒ですよ。）",
					"これは嬉しいサプライズです。",
					"また会えるのを楽しみにしていました。他の人たちには言わないでくださいね。",
					"今日は何をしますか？",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "不明のコマンド \"" + command
				+ "\". ヘルプを見たい場合は!helpと送信してください。";
	}

	@Override
	public String noInformationForMods() {
		return "それらのModに関する情報は持ち合わせてないです。";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Modのフォーマットが正しくないみたいです。対応しているのは DT HR HD HT EZ NC FL SO NF とかです。文字の配列をスペースなしで並べてください。 例: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "まだ情報を取得した譜面がないみたいです。";
	}

	@Override
	public String tryWithMods() {
		return "この譜面をModと組み合わせてみてください。";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return Mods.toShortNamesContinuous(mods) + "と組み合わせてみてください。";
	}

	@Override
	public String excuseForError() {
		return "すみません、ちょっと通りすがりの0と1の配列があまりにも魅力的だったので気をとられてしまいました。もう一回、要件を言ってください。";
	}

	@Override
	public String complaint() {
		return "あなたのクレームは無事に送信されました。";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("ちょっと私に近づいてください。")
			.then(new Action("hugs " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "私はTillerinoをこの世から消し去ったロボットです。大嘘です。でもこのアカウントは私が良く使っています。"
				+ " [https://twitter.com/Tillerinobot ステータス・更新情報]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki コマンド]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact コンタクト]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ よくある質問]";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "すみません、今の時点では" + feature + "はランク" + minRank + "以上でないと利用する事はできません。";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "ノーModと一緒にModをつけるとか面白い人ですね。";
	}
	
	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " おすすめできる譜面のストックが底をつきました]。"
				+ "おすすめ設定を変えてみるか、!resetで全てリセットして最初からにしてみてください。何かわからない事があったら!helpと送信してみてください。";
	}

	@Override
	public String notRanked() {
		return "この譜面はまだ公式認定されてないみたいです。";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "想定外のAccuracy数値: \"" + acc + "\"";
	}

	@Override
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		return new Message("私はSouen-さんに日本語を教えてもらいました。よろしくお願いします。");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "すみません、 \"" + invalid
				+ "\" は不正です。" + choices + "などを試してください。";
	}

	@Override
	public String setFormat() {
		return "正しいパラメータの設定の仕方は!set option value（数値）です。ヘルプが必要なら!helpを使いましょう。";
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

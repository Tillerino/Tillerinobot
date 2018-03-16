package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

/**
 * @author https://github.com/jamkevin https://osu.ppy.sh/u/jamkevin
 */
public class Korean implements Language {

	@Override
	public String unknownBeatmap() {
		return "죄송합니다. 이 맵은 새로 랭크되었거나, 매우 어렵거나, 랭크되지 않았거나, 스탠다드 osu! 모드가 아닙니다.";
	}

	@Override
	public String internalException(String marker) {
		return "어... 인간 틸레리노가 제 회로를 망쳐놓은 것 같아요."
				+ " 그가 이것을 눈치채지 못한다면 [https://github.com/Tillerino/Tillerinobot/wiki/Contact 그에게 알려주시겠어요?]? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "무엇이 일어나고 있는 거죠? 저는 osu! 서버에서 아무것도 수신하지 못하고 있어요. 0011101001010000이 뭘 의미하는지 알려주실 수 있나요?"
				+ " 인간 틸레리노가 이것은 아무 걱정이 아니라면서 다시 시도해보라고 하고 있네요."
				+ " 만약 당신이 어떤 이유로 무척 걱정된다면, 당신은 그에게 이것에 대해  [https://github.com/Tillerino/Tillerinobot/wiki/Contact 전해줄 수 있어요]. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "이 모드에 대해서 정보가 없습니다.";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			return new Message("띵동");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("다시 오신 것을 환영합니다, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...당신인가요? 오랜만이네요!"))
				.then(new Message("당신을 다시 만나게 되어 기뻐요. 몇 가지 곡을 추천해드릴까요?"));
		} else {
			String[] messages = {
					"곡을 추천받고 싶어하는 것 같네요.",
					"당신을 만나서 정말 기뻐요! :)",
					"제가 가장 좋아하는 인간이네요. (다른 인간에게는 말하지 마세요!)",
					"즐거운 놀람이군요! ^.^",
					"당신이 오길 기다리고 있었어요. 다른 인간들은 전혀 보이지 않더라구요. 제가 말했다고 하지 마세요! :3",
					"오늘 기분이 어때요?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "unknown command \"" + command
				+ "\".도움을 원하시면 !help 라고 쳐보세요!";
	}

	@Override
	public String noInformationForMods() {
		return "죄송합니다. 이 모드들에 대해서 지금은 알려드릴 수가 없네요.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "모드를 제대로 입력하셨나요? 모드들은 DT HR HD HT EZ NC FL SO NF의 결합으로 이루어져 있어야 해요. 띄어쓰기나 다른 문자를 쓰지 않고 시도해보세요. 예: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "먼저 /np를 쳐주세요!";
	}

	@Override
	public String tryWithMods() {
		return "이 맵을 모드와 같이 해보세요!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "이 맵을 이 모드들과 해보세요:" + Mods.toShortNamesContinuous(mods);
	}

	@Override
	public String excuseForError() {
		return "죄송합니다, 0과 1의 향연에 빠져 아무것도 하지 못하고 있었어요. 뭐라고 하셨는지 다시 한번만 말해주시겠어요?";
	}

	@Override
	public String complaint() {
		return "요구가 접수되었습니다. 틸레리노가 할 수 있을 때 검토해 볼 거에요.";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("이리 오세요!")
			.then(new Action("(포옹) " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "안녕하세요! 저는 틸레리노를 죽이고 그의 계정을 접수한 로봇입니다! 장난이고요, 전 단지 그의 계정을 자주 쓸 뿐이에요."
				+ " [https://twitter.com/Tillerinobot 상태와 업데이트]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki 커맨드]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact 연락]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ FAQ]";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "죄송합니다, 현재는  " + feature + " 가  " + minRank + " 이상인 사람에게만 가능해요.";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "논모드로 모드를 한다는게 무슨 소리죠?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " 제가 추천드릴 수 있는 모든 곡을 추천드렸어요]."
				+ " 다른 옵션이나 !reset을 시도해보세요. 확실하지 않다면, !help를 쳐주세요.";
	}

	@Override
	public String notRanked() {
		return "랭크되지 않은 비트맵이에요.";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "가능하지 않은 정확도입니다.: \"" + acc + "\"";
	}

	@Override
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		return new Message("언어가 한국어로 전환되었습니다. 번역: jamkevin:)");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "죄송합니다, 그러나 \"" + invalid
				+ "\" 가 작동하지 않아요. 이런 것들을 시도해보세요: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "파라미터를 설정하기 위한 방법은 !set 옵션 값 이에요. !help로 자세한 정보를 확인하세요.";
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

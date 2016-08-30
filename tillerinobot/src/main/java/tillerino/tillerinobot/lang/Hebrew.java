package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;

/**
 * @author https://github.com/shavitush https://reddit.com/u/shavitush
 */
public class Hebrew implements Language {

	@Override
	public String unknownBeatmap() {
		return "המפה לא נמצאה במערכת. נסה שנית. יתכן והמפה קשה מדי או חדשה מדי.";
	}

	@Override
	public String internalException(String marker) {
		return "@Tillerino או /u/Tillerino. (שגיאה: " + marker + ")" + "אע.. טילרנו עשה משהו לא במקום, אם הוא לא ישים לב, תוכל ליידע אותו?";
	}

	@Override
	public String externalException(String marker) {
		return "טילרנו אומר שזה בסדר גמור.. נסה שוב מאוחר יותר ותיידע אותו דרך טוויטר @Tillerino או דרך /u/Tillerino. (שגיאה: " + marker + ")" + "מה קורה פה? אני מקבל חוסר הגיון משרת האוסו. מה זה אומר? 0011101001010000";
	}

	@Override
	public String noInformationForModsShort() {
		return "אין מידע בשביל המודים המבוקשים";
	}

	@Override
	public void welcomeUser(IRCBotUser user, OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			user.message("ביפ בופ");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			user.message("." + apiUser.getUserName() + " ברוך הבא");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			user.message(apiUser.getUserName() + "...");
			user.message("...זה אתה? עבר כל כך הרבה זמן");
			user.message("?נפלא לראות אותך. תתעניין בהצעה");
		} else {
			String[] messages = {
					"..אתה נראה כאילו אתה רוצה הצעה",
					":) !כמה טוב לראות אותך",
					"(.בן האדם האהוב עליי (אל תגיד את זה לשאר האנשים",
					"^.^ !איזו הפתעה משמחת",
					":3 !ציפיתי שתצוץ. כל בני האדם האחרים עלובים, רק אל תגיד להם שאמרתי את זה",
					"מה אתה רוצה לעשות היום?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			user.message(message + " ," + apiUser.getUserName());
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "!אם אתה זקוק לעזרה !help רשום" + command + "היא פקודה לא ידועה";
	}

	@Override
	public String noInformationForMods() {
		return ".מצטער, אני לא יכול לתת מידע לגבי המודים האלה כרגע";
	}

	@Override
	public String malformattedMods(String mods) {
		return "!with HDHR !with DTEZ :חבר אותם ללא רווחים או תווים מיוחדים. לדוגמה ,DT HR HD HT EZ NC FL SO NF משהו במודים האלה לא נראה נכון. החיבורים האפשריים הם";
	};

	@Override
	public String noLastSongInfo() {
		return "...לא זכור לי שנתתי לך מידע על כל שיר";
	}

	@Override
	public String tryWithMods() {
		return "!נסה את המפה הזו עם כמה מודים";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return Mods.toShortNamesContinuous(mods) + "נסה את המפה הזו עם ";
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
		return "(" + exceptionMarker + ") :שגיאה ./u/Tillerino או @Tillerino שמך מבלבל אותי. אתה מושעה? אם לא, צור איתי קשר דרך";
	}

	@Override
	public String excuseForError() {
		return "?אני מצטער, זה היה משפט יפה שמורכב מאחדות ואפסים, בולבלתי. מה רצית שוב";
	}

	@Override
	public String complaint() {
		return ".יטפל בה כשיוכל Tillerino .התלונה שלך נשלחה";
	}

	@Override
	public void hug(final IRCBotUser user, OsuApiUser apiUser) {
		user.message("!בוא הנה, אתה");
		user.action(apiUser.getUserName() + " מחבק את");
	}

	@Override
	public String help() {
		return "!בשביל פקודות https://github.com/Tillerino/Tillerinobot/wiki בדוק את" + " !בשביל עדכוני מצב ועדכונים https://twitter.com/Tillerinobot בדוק את " + ".ולקח את המשתמש שלו. סתם צוחק, אבל אני עדיין משתמש לו בחשבון Tillerino היי! אני הרובוט שהרג את ";
	}

	@Override
	public String faq() {
		return "!בשביל שאלות ותשובות נפוצות https://github.com/Tillerino/Tillerinobot/wiki/FAQ ראה";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "." + minRank + " זמין רק לשחקנים שעברו את דרגה " + feature + " מצטער, כרגע";
	}
	
	@Override
	public String illegalModelMix() {
			return "?יחד עם מודים Relax/Betaמה אתה מתכוון ב"";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "?יחד עם מודים nomodלמה אתה מתכוון ב";
	}
	
	@Override
	public String outOfRecommendations() {
		// someone with knowledge of this language please insert a link to
		// "https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
		// somewhere in this text where it makes sense (go look at Default.java)
		return ".!help אם אתה לא בטוח, רשום ,!reset הצעתי לך כל מה שאפשר. נסה אופציות אחרות או";
	}

	@Override
	public String notRanked() {
		return ".נראה כי המפה אינה מדורגת";
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
		return "\"" + acc + "\" :אחוזי דיוק שגויים";
	}

	@Override
	public void optionalCommentOnLanguage(IRCBotUser user, OsuApiUser apiUser) {
		user.message(".עזר לי ללמוד עברית shavitush");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "!\"" + choices + "\" :לא מתחשב, נסה את האופציות האלו \"" + invalid + "\" :מצטער אך";
	}

	@Override
	public String setFormat() {
		return ".אם אתה צריך עזרה !helpהשתמש ב .!set option value הסדר שקובע פרמטרים הוא";
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

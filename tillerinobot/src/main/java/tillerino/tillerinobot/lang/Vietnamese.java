package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Action;
import org.tillerino.ppaddict.chat.GameChatResponse.Message;

/**
 * TRANSLATION NOTE:
 *
 * <p>Please put some contact data into the following tag. If any additional messages are required, I'll use the English
 * version in all translations and notify the authors.
 *
 * @author Xuân Hải Trần, fb.com/xuanhai.tran.184, BCraftMG, https://osu.ppy.sh/users/13456818
 */
public class Vietnamese extends AbstractMutableLanguage {
    @Override
    public String unknownBeatmap() {
        return "Xin lỗi, tôi không biết map này. Có thể do map quá mới, quá khó, chưa được xếp hạng hoặc không phải chế độ standard.";
    }

    @Override
    public String internalException(String marker) {
        return "Ugh... Có vẻ như tên Tillerino kia lại nghịch đống dây nối của tôi rồi."
                + " Nếu anh ta không nhận ra, liệu bạn có thể [https://github.com/Tillerino/Tillerinobot/wiki/Contact liên hệ] với anh ta không? (reference "
                + marker + ")";
    }

    @Override
    public String externalException(String marker) {
        return "Cái quái gì đang xảy ra vậy? Tôi đang nhận được toàn thứ linh tinh từ server osu! Bạn có thể nói với tôi đây là gì không? [https://www.youtube.com/watch?v=dQw4w9WgXcQ 0011101001010000]"
                + " \"Ông\" Tillerino nói rằng không có gì đáng lo ngại cả và rằng chúng ta nên thử lại"
                + " Nếu bạn đang cảm thấy lo lắng vì một lí do nào đấy, bạn có thể [https://github.com/Tillerino/Tillerinobot/wiki/Contact liên hệ] với ổng về việc này. (reference "
                + marker + ")";
    }

    @Override
    public String noInformationForModsShort() {
        return "không có dữ liệu cho mod được yêu cầu";
    }

    @Override
    public GameChatResponse welcomeUser(OsuApiUser apiUser, long inactiveTime) {
        if (inactiveTime < 60 * 1000) {
            return new Message("beep boop");
        } else if (inactiveTime < 24 * 60 * 60 * 1000) {
            return new Message("Mừng trở lại, " + apiUser.getUserName() + ".");
        } else if (inactiveTime > 7L * 24 * 60 * 60 * 1000) {
            return new Message(apiUser.getUserName() + "...")
                    .then(new Message("Là bạn sao? Lâu lắm mới gặp lại! :D"))
                    .then(new Message(
                            "Thật tốt khi bạn đã trở lại. Bạn có muốn tôi gợi ý một vài beatmap cho bạn không?"));
        } else {
            String[] messages = {
                "có vẻ như bạn cần một sự gợi ý.",
                "thật tốt khi được gặp bạn :)",
                "người tôi thích nhất :D (Đứng nói với ai nhé!)",
                "thật bất ngờ! ^.^",
                "tôi đợi bạn mãi! Mấy người kia thật kì cục, nhưng đừng bảo họ là tôi nói đấy :3",
                "bạn muốn làm gì hôm nay?",
            };

            String message = messages[ThreadLocalRandom.current().nextInt(messages.length)];

            return new Message(apiUser.getUserName() + ", " + message);
        }
    }

    @Override
    public String unknownCommand(String command) {
        return "Lệnh không tồn tại \"" + command + "\". Gõ !help nếu bạn cần giúp đỡ!";
    }

    @Override
    public String noInformationForMods() {
        return "Xin lỗi, tôi không thể đưa thông tin đối với những mod bạn đã chọn lúc này.";
    }

    @Override
    public String malformattedMods(String mods) {
        return "Mấy mod này có vẻ không đúng. Mod có thể là sự kết hợp giữa DT HR HD HT EZ NC FL SO NF. Kết hợp chúng mà không có khoảng cách hay các kí tự đặc biệt ở giữa. Ví dụ: !with HDHR, !with DTEZ";
    }

    @Override
    public String noLastSongInfo() {
        return "Tôi không nhớ đã đưa bạn thông tin về bất cứ bài hát nào...";
    }

    @Override
    public String tryWithMods() {
        return "Hãy thử map này với một vài mod!";
    }

    @Override
    public String tryWithMods(List<Mods> mods) {
        return "Hãy thử map này với " + Mods.toShortNamesContinuous(mods) + "!";
    }

    @Override
    public String excuseForError() {
        return "Xin lỗi, vừa mới có một hàng số 0 và 1 lướt qua làm tôi mất tập trung. Bạn cần gì vậy?";
    }

    @Override
    public String complaint() {
        return "Khiếu nại (phàn nàn) của bạn đã được ghi lại. Tillerino sẽ xem chúng khi anh ấy rảnh.";
    }

    @Override
    public GameChatResponse hug(OsuApiUser apiUser) {
        return new Message("Lại đây nào!").then(new Action("/ôm/ " + apiUser.getUserName()));
    }

    @Override
    public String help() {
        return "Heyo! Tôi là con robot đã giết Tillerino và lấy tài khoản của anh ta. Đùa đấy, nhưng tôi cũng sử dụng tài khoản này nhiều lắm."
                + " [https://twitter.com/Tillerinobot các cập nhật]"
                + " - [https://github.com/Tillerino/Tillerinobot/wiki lệnh]"
                + " - [http://ppaddict.tillerino.org/ nghiện pp?]"
                + " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact liên hệ]";
    }

    @Override
    public String faq() {
        return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Những câu hỏi thường gặp]";
    }

    @Override
    public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
        return "Xin lỗi, hiện tại tính năng " + feature + " chỉ có sẵn đối với những người từ rank " + minRank
                + " trở lên.";
    }

    @Override
    public String mixedNomodAndMods() {
        return "Ý bạn là gì khi kết hợp mod với nomod?";
    }

    @Override
    public String outOfRecommendations() {
        return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
                + " Tôi đã gợi ý tất cả những gì tôi có.]."
                + " Hãy thử những tùy chọn gợi ý khác hoặc !reset. Nếu bạn không chắc, hãy xem thử !help.";
    }

    @Override
    public String notRanked() {
        return "Có vẻ như beatmap này chưa được xếp hạng.";
    }

    @Override
    public String invalidAccuracy(String acc) {
        return "Độ chính xác không hợp lệ: \"" + acc + "\"";
    }

    @Override
    public GameChatResponse optionalCommentOnLanguage(OsuApiUser apiUser) {
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
        return new Message(
                "Đã chuyển sang tiếng Việt. Người dịch:[https://osu.ppy.sh/users/13456818 BCraftMG] FB:[fb.com/xuanhai.tran.184 Xuân Hải Trần]. Xin hãy góp ý thêm về bản dịch.");
    }

    @Override
    public String invalidChoice(String invalid, String choices) {
        return "Xin lỗi, nhưng tùy chọn \"" + invalid + "\" không hợp lệ. Hãy thử " + choices + "!";
    }

    @Override
    public String setFormat() {
        return "Mẫu sử dụng lệnh !set là  \"!set <giá trị>\". Hãy thử !help nếu bạn cần trợ giúp thêm về tùy chọn.";
    }

    StringShuffler apiTimeoutShuffler = new StringShuffler(ThreadLocalRandom.current());

    @Override
    public String apiTimeoutException() {
        registerModification();
        final String message = "Server osu! đang cực kì chậm nên tôi không thể giúp gì cho bạn ngay bây giờ. ";
        return message
                + apiTimeoutShuffler.get(
                        "Kể với tôi lần cuối cùng bạn nói chuyện với bà của bạn là lúc nào đi?",
                        "Bạn có thể đi dọn phòng của bạn đi rồi hỏi lại. Bạn biết đấy, trong khi server đang chậm chạp như này?",
                        "Dám cá là bạn muốn một chuyến đi bộ. Bạn biết thế giới bên ngoài chứ?",
                        "Tôi biết là bạn có rất nhiều thứ khác để làm mà. Tại sao không làm nó ngay lúc này chứ?",
                        "Đằng nào bạn cũng cần nghỉ ngơi mà.",
                        "Hãy xem thử trang này trên [https://en.wikipedia.org/wiki/Special:Random wikipedia]!",
                        "Hãy xem thử xem có ai đang [http://www.twitch.tv/directory/game/Osu! stream] ngay lúc này!",
                        "Hãy chơi thử cái [http://dagobah.net/flash/Cursor_Invisible.swf game] mà chắc chắn bạn chơi rất tệ.",
                        "This should give you plenty of time to study [https://github.com/Tillerino/Tillerinobot/wiki hướng dẫn của tôi].",
                        "Đừng lo, mấy cái [https://www.reddit.com/r/osugame dank memes] sẽ giúp bạn tiêu tốn chút thời gian.",
                        "Khi đang chán, bạn có thể thử [http://gabrielecirulli.github.io/2048/ 2048]!",
                        "Đố vui: Nếu ổ cứng của bạn chết ngay lúc này, bao nhiêu dữ liệu cá nhân của bạn sẽ biến mất mãi mãi?",
                        "Thế bạn đã bao giờ thử [https://www.google.de/search?q=bring%20sally%20up%20push%20up%20challenge sally up push up challenge] chưa?",
                        "Bạn có thể đi làm việc gì khác hoặc chúng ta có thể nhìn chằm chằm vào mắt nhau. Trong im lặng.");
    }

    @Override
    public String noRecentPlays() {
        return "Không tìm thấy lượt chơi gần đây.";
    }

    @Override
    public String isSetId() {
        return "Tùy chọn này muốn nói đến một nhóm beatmap, không phải một beatmap duy nhất.";
    }
}

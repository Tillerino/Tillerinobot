package tillerino.tillerinobot.lang;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;
import tillerino.tillerinobot.BeatmapMetaTest;

public class AllLanguagesTest {
    @ParameterizedTest
    @EnumSource(LanguageIdentifier.class)
    public void testPlain(LanguageIdentifier ident) throws Exception {
        Language lang = ident.cls.getConstructor().newInstance();
        lang.apiTimeoutException();
        lang.complaint();
        lang.excuseForError();
        lang.help();
        lang.isSetId();
        lang.mixedNomodAndMods();
        lang.malformattedMods("XXX");
        lang.noInformationForMods();
        lang.noInformationForModsShort();
        lang.noLastSongInfo();
        lang.noRecentPlays();
        lang.notRanked();
        lang.outOfRecommendations();
        lang.setFormat();
        lang.tryWithMods();
        lang.unknownBeatmap();
    }

    @ParameterizedTest
    @EnumSource(LanguageIdentifier.class)
    public void testOptionals(LanguageIdentifier ident) throws Exception {
        Language lang = ident.cls.getConstructor().newInstance();
        lang.optionalCommentOnLanguage(new OsuApiUser());
        lang.optionalCommentOnNP(new OsuApiUser(), BeatmapMetaTest.fakeBeatmapMeta(101));
        // lang.optionalCommentOnRecommendation(new OsuApiUser(), recommendation)
        lang.optionalCommentOnWith(new OsuApiUser(), BeatmapMetaTest.fakeBeatmapMeta(101));
    }

    @ParameterizedTest
    @EnumSource(LanguageIdentifier.class)
    public void testExternalException(LanguageIdentifier ident) throws Exception {
        Language lang = ident.cls.getConstructor().newInstance();
        assertThat(lang.externalException("ABCDEF")).as("External exception").contains("ABCDEF");
    }

    @ParameterizedTest
    @EnumSource(LanguageIdentifier.class)
    public void testFAQ(LanguageIdentifier ident) throws Exception {
        Language lang = ident.cls.getConstructor().newInstance();
        assertThat(lang.faq()).as("FAQ").contains("https://github.com/Tillerino/Tillerinobot/wiki/FAQ");
    }

    @ParameterizedTest
    @EnumSource(LanguageIdentifier.class)
    public void testFeatureRankRestricted(LanguageIdentifier ident) throws Exception {
        Language lang = ident.cls.getConstructor().newInstance();
        assertThat(lang.featureRankRestricted("THE_FEATURE", 1234, new OsuApiUser()))
                .as("Rank-restricted feature")
                .contains("THE_FEATURE", "1234");
    }

    @ParameterizedTest
    @EnumSource(LanguageIdentifier.class)
    public void testHug(LanguageIdentifier ident) throws Exception {
        Language lang = ident.cls.getConstructor().newInstance();
        for (int attempt = 0; attempt < 1000; attempt++) {
            assertThat(lang.hug(new OsuApiUser())).isNotEqualTo(GameChatResponse.none());
        }
    }

    @ParameterizedTest
    @EnumSource(LanguageIdentifier.class)
    public void testInternalException(LanguageIdentifier ident) throws Exception {
        Language lang = ident.cls.getConstructor().newInstance();
        assertThat(lang.internalException("ABCDEF")).as("Internal exception").contains("ABCDEF");
    }

    @ParameterizedTest
    @EnumSource(LanguageIdentifier.class)
    public void testInvalidAccuracy(LanguageIdentifier ident) throws Exception {
        Language lang = ident.cls.getConstructor().newInstance();
        String message = lang.invalidAccuracy("XXX");
        if (!ident.toString().toLowerCase().contains("tsundere")) {
            assertThat(message).as("Invalid Accuracy").contains("XXX");
        }
    }

    @ParameterizedTest
    @EnumSource(LanguageIdentifier.class)
    public void testInvalidChoice(LanguageIdentifier ident) throws Exception {
        Language lang = ident.cls.getConstructor().newInstance();
        assertThat(lang.invalidChoice("XXX", "A, B, C")).as("Invalid Choice").contains("XXX", "A, B, C");
    }

    @ParameterizedTest
    @EnumSource(LanguageIdentifier.class)
    public void testTryWithmods(LanguageIdentifier ident) throws Exception {
        Language lang = ident.cls.getConstructor().newInstance();
        assertThat(lang.tryWithMods(Arrays.asList(Mods.DoubleTime, Mods.HardRock)))
                .as("Try with mods")
                .contains("DT", "HR");
    }

    @ParameterizedTest
    @EnumSource(LanguageIdentifier.class)
    public void testUnknownCommand(LanguageIdentifier ident) throws Exception {
        Language lang = ident.cls.getConstructor().newInstance();
        assertThat(lang.unknownCommand("THE_COMMAND")).as("Unknown command").contains("THE_COMMAND");
    }

    @ParameterizedTest
    @EnumSource(LanguageIdentifier.class)
    public void testWelcome(LanguageIdentifier ident) throws Exception {
        Language lang = ident.cls.getConstructor().newInstance();
        for (long inactiveTime : new long[] {
            60 * 1000 - 1, 24 * 60 * 60 * 1000 - 1, 7L * 24 * 60 * 60 * 1000, 7L * 24 * 60 * 60 * 1000 + 1
        }) lang.welcomeUser(new OsuApiUser(), inactiveTime);
    }
}

package tillerino.tillerinobot.lang;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

public class HebrewTest {
    final Hebrew lang = new Hebrew();

    final OsuApiUser apiUser = mock(OsuApiUser.class);

    final boolean print = false;

    @BeforeEach
    public void setupMocks() {
        when(apiUser.getUserName()).thenReturn("username");
    }

    @Test
    public void testUnknownBeatmap() {
        println("unknownBeatmap");
        println(lang.unknownBeatmap());
    }

    @Test
    public void testInternalException() {
        println("internalException");
        println(lang.internalException("gd67ad678"));
    }

    @Test
    public void testExternalException() {
        println("externalException");
        println(lang.externalException("gd56dd"));
    }

    @Test
    public void testNoInformationForModsShort() {
        println("noInformationForModsShort");
        println(lang.noInformationForModsShort());
    }

    @Test
    public void testWelcomeUser() {
        println("welcomeUser 10 seconds");
        println(lang.welcomeUser(apiUser, 10 * 1000));
        println("welcomeUser 5 hours");
        println(lang.welcomeUser(apiUser, 5 * 3600 * 1000));
        println("welcomeUser 2 days");
        println(lang.welcomeUser(apiUser, 2L * 24 * 3600 * 1000));
        println("welcomeUser 10 days");
        println(lang.welcomeUser(apiUser, 10L * 24 * 3600 * 1000));
    }

    @Test
    public void testUnknownCommand() {
        println("unknownBeatmap");
        println(lang.unknownBeatmap());
    }

    @Test
    public void testNoInformationForMods() {
        println("noInformationForMods");
        println(lang.noInformationForMods());
    }

    @Test
    public void testMalformattedMods() {
        println("malformattedMods");
        println(lang.malformattedMods("DZ+ET"));
    }

    @Test
    public void testNoLastSongInfo() {
        println("noLastSongInfo");
        println(lang.noLastSongInfo());
    }

    @Test
    public void testTryWithMods() {
        println("tryWithMods unspecific");
        println(lang.tryWithMods());
    }

    @Test
    public void testTryWithModsSpecific() {
        println("tryWithMods specific");
        println(lang.tryWithMods(Arrays.asList(Mods.Hidden, Mods.HardRock)));
    }

    @Test
    public void testExcuseForError() {
        println("excuseForError");
        println(lang.excuseForError());
    }

    @Test
    public void testComplaint() {
        println("complaint");
        println(lang.complaint());
    }

    @Test
    public void testHug() {
        println("hug");
        println(lang.hug(apiUser));
    }

    @Test
    public void testHelp() {
        println("help");
        println(lang.help());
    }

    @Test
    public void testFaq() {
        println("faq");
        println(lang.faq());
    }

    @Test
    public void testFeatureRankRestricted() {
        println("featureRankRestricted");
        println(lang.featureRankRestricted("gamma", 100000, apiUser));
    }

    @Test
    public void testMixedNomodAndMods() {
        println("mixedNomodAndMods");
        println(lang.mixedNomodAndMods());
    }

    @Test
    public void testOutOfRecommendations() {
        println("outOfRecommendations");
        println(lang.outOfRecommendations());
    }

    @Test
    public void testNotRanked() {
        println("notRanked");
        println(lang.notRanked());
    }

    @Test
    public void testOptionalCommentOnNP() {
        println("optionalCommentOnNP");
        println(lang.optionalCommentOnNP(apiUser, null));
    }

    @Test
    public void testOptionalCommentOnWith() {
        println("optionalCommentOnWith");
        println(lang.optionalCommentOnWith(apiUser, null));
    }

    @Test
    public void testOptionalCommentOnRecommendation() {
        println("optionalCommentOnRecommendation");
        println(lang.optionalCommentOnRecommendation(apiUser, null));
    }

    @Test
    public void testInvalidAccuracy() {
        println("invalidAccuracy");
        println(lang.invalidAccuracy("lOl.3%"));
    }

    @Test
    public void testOptionalCommentOnLanguage() {
        println("optionalCommentOnLanguage");
        println(lang.optionalCommentOnLanguage(apiUser));
    }

    @Test
    public void testInvalidChoice() {
        println("invalidChoice");
        println(lang.invalidChoice("ice cream", "cookies, chocolate"));
    }

    @Test
    public void testSetFormat() {
        println("setFormat");
        println(lang.setFormat());
    }

    private void println(Object s) {
        if (print) {
            System.out.println(s);
        }
    }
}

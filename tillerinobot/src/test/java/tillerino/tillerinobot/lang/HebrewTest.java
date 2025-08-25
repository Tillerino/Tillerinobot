package tillerino.tillerinobot.lang;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

public class HebrewTest {
	Hebrew lang = new Hebrew();

	OsuApiUser apiUser = mock(OsuApiUser.class);

	boolean print = false;

	@BeforeEach
	public void setupMocks() {
		when(apiUser.getUserName()).thenReturn("username");
	}

	@Test
	public void testUnknownBeatmap() throws Exception {
		println("unknownBeatmap");
		println(lang.unknownBeatmap());
	}

	@Test
	public void testInternalException() throws Exception {
		println("internalException");
		println(lang.internalException("gd67ad678"));
	}

	@Test
	public void testExternalException() throws Exception {
		println("externalException");
		println(lang.externalException("gd56dd"));
	}

	@Test
	public void testNoInformationForModsShort() throws Exception {
		println("noInformationForModsShort");
		println(lang.noInformationForModsShort());
	}
	
	@Test
	public void testWelcomeUser() throws Exception {
		println("welcomeUser 10 seconds");
		println(lang.welcomeUser(apiUser, 10 * 1000));
		println("welcomeUser 5 hours");
		println(lang.welcomeUser(apiUser, 5 * 3600 * 1000));
		println("welcomeUser 2 days");
		println(lang.welcomeUser(apiUser, 2l * 24 * 3600 * 1000));
		println("welcomeUser 10 days");
		println(lang.welcomeUser(apiUser, 10l * 24 * 3600 * 1000));
	}

	@Test
	public void testUnknownCommand() throws Exception {
		println("unknownBeatmap");
		println(lang.unknownBeatmap());
	}

	@Test
	public void testNoInformationForMods() throws Exception {
		println("noInformationForMods");
		println(lang.noInformationForMods());
	}

	@Test
	public void testMalformattedMods() throws Exception {
		println("malformattedMods");
		println(lang.malformattedMods("DZ+ET"));
	}

	@Test
	public void testNoLastSongInfo() throws Exception {
		println("noLastSongInfo");
		println(lang.noLastSongInfo());
	}

	@Test
	public void testTryWithMods() throws Exception {
		println("tryWithMods unspecific");
		println(lang.tryWithMods());
	}

	@Test
	public void testTryWithModsSpecific() throws Exception {
		println("tryWithMods specific");
		println(lang.tryWithMods(Arrays.asList(Mods.Hidden, Mods.HardRock)));
	}

	@Test
	public void testExcuseForError() throws Exception {
		println("excuseForError");
		println(lang.excuseForError());
	}

	@Test
	public void testComplaint() throws Exception {
		println("complaint");
		println(lang.complaint());
	}

	@Test
	public void testHug() throws Exception {
		println("hug");
		println(lang.hug(apiUser));
	}

	@Test
	public void testHelp() throws Exception {
		println("help");
		println(lang.help());
	}

	@Test
	public void testFaq() throws Exception {
		println("faq");
		println(lang.faq());
	}

	@Test
	public void testFeatureRankRestricted() throws Exception {
		println("featureRankRestricted");
		println(lang.featureRankRestricted("gamma", 100000, apiUser));
	}

	@Test
	public void testMixedNomodAndMods() throws Exception {
		println("mixedNomodAndMods");
		println(lang.mixedNomodAndMods());
	}

	@Test
	public void testOutOfRecommendations() throws Exception {
		println("outOfRecommendations");
		println(lang.outOfRecommendations());
	}

	@Test
	public void testNotRanked() throws Exception {
		println("notRanked");
		println(lang.notRanked());
	}

	@Test
	public void testOptionalCommentOnNP() throws Exception {
		println("optionalCommentOnNP");
		println(lang.optionalCommentOnNP(apiUser, null));
	}

	@Test
	public void testOptionalCommentOnWith() throws Exception {
		println("optionalCommentOnWith");
		println(lang.optionalCommentOnWith(apiUser, null));
	}

	@Test
	public void testOptionalCommentOnRecommendation() throws Exception {
		println("optionalCommentOnRecommendation");
		println(lang.optionalCommentOnRecommendation(apiUser, null));
	}

	@Test
	public void testInvalidAccuracy() throws Exception {
		println("invalidAccuracy");
		println(lang.invalidAccuracy("lOl.3%"));
	}

	@Test
	public void testOptionalCommentOnLanguage() throws Exception {
		println("optionalCommentOnLanguage");
		println(lang.optionalCommentOnLanguage(apiUser));
	}

	@Test
	public void testInvalidChoice() throws Exception {
		println("invalidChoice");
		println(lang.invalidChoice("ice cream", "cookies, chocolate"));
	}

	@Test
	public void testSetFormat() throws Exception {
		println("setFormat");
		println(lang.setFormat());
	}

	private void println(Object s) {
		if (print) {
			System.out.println(s);
		}
	}
}

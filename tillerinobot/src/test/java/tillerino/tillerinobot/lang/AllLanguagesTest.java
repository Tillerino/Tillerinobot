package tillerino.tillerinobot.lang;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import lombok.RequiredArgsConstructor;
import tillerino.tillerinobot.BestmapMetaTest;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.lang.LanguageIdentifier;

@RequiredArgsConstructor
@RunWith(Parameterized.class)
public class AllLanguagesTest {
	@Parameters(name = "{index}: {0}")
	public static Iterable<Object[]> data() {
		return Stream.of(LanguageIdentifier.values()).map(x -> new Object[] { x }).collect(toList());
	}

	private final LanguageIdentifier ident;

	private Language lang;

	@Before
	public void instantiate() throws Exception {
		lang = ident.cls.newInstance();
	}

	@Test
	public void testPlain() throws Exception {
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

	@Test
	public void testOptionals() throws Exception {
		lang.optionalCommentOnLanguage(new OsuApiUser());
		lang.optionalCommentOnNP(new OsuApiUser(), BestmapMetaTest.fakeBeatmapMeta(101));
		// lang.optionalCommentOnRecommendation(new OsuApiUser(), recommendation)
		lang.optionalCommentOnWith(new OsuApiUser(), BestmapMetaTest.fakeBeatmapMeta(101));
	}

	@Test
	public void testExternalException() throws Exception {
		assertThat(lang.externalException("ABCDEF")).as("External exception").contains("ABCDEF");
	}

	@Test
	public void testFAQ() throws Exception {
		assertThat(lang.faq()).as("FAQ").contains("https://github.com/Tillerino/Tillerinobot/wiki/FAQ");
	}

	@Test
	public void testFeatureRankRestricted() throws Exception {
		assertThat(lang.featureRankRestricted("THE_FEATURE", 1234, new OsuApiUser())).as("Rank-restricted feature")
				.contains("THE_FEATURE", "1234");
	}

	@Test
	public void testHug() throws Exception {
		for (int attempt = 0; attempt < 1000; attempt++) {
			assertThat(lang.hug(new OsuApiUser())).isNotEqualTo(new CommandHandler.NoResponse());
		}
	}

	@Test
	public void testInternalException() throws Exception {
		assertThat(lang.internalException("ABCDEF")).as("Internal exception").contains("ABCDEF");
	}

	@Test
	public void testInvalidAccuracy() throws Exception {
		String message = lang.invalidAccuracy("XXX");
		if (!ident.toString().toLowerCase().contains("tsundere")) {
			assertThat(message).as("Invalid Accuracy").contains("XXX");
		}
	}

	@Test
	public void testInvalidChoice() throws Exception {
		assertThat(lang.invalidChoice("XXX", "A, B, C")).as("Invalid Choice").contains("XXX", "A, B, C");
	}

	@Test
	public void testTryWithmods() throws Exception {
		assertThat(lang.tryWithMods(Arrays.asList(Mods.DoubleTime, Mods.HardRock))).as("Try with mods").contains("DT",
				"HR");
	}

	@Test
	public void testUnknownCommand() throws Exception {
		assertThat(lang.unknownCommand("THE_COMMAND")).as("Unknown command").contains("THE_COMMAND");
	}

	@Test
	public void testWelcome() throws Exception {
		for (long inactiveTime : new long[] {
				60 * 1000 - 1,
				24 * 60 * 60 * 1000 - 1,
				7l * 24 * 60 * 60 * 1000,
				7l * 24 * 60 * 60 * 1000 + 1 })
			lang.welcomeUser(new OsuApiUser(), inactiveTime);
	}
}

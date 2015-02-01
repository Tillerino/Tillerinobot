package tillerino.tillerinobot.lang;

import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.IRCBot.IRCBotUser;

public class HebrewTest {
	Hebrew lang = new Hebrew();

	@Test
	public void testUnknownBeatmap() throws Exception {
		System.out.println("unknownBeatmap");
		System.out.println(lang.unknownBeatmap());
	}

	@Test
	public void testInternalException() throws Exception {
		System.out.println("internalException");
		System.out.println(lang.internalException("gd67ad678"));
	}

	@Test
	public void testExternalException() throws Exception {
		System.out.println("externalException");
		System.out.println(lang.externalException("gd56dd"));
	}

	@Test
	public void testNoInformationForModsShort() throws Exception {
		System.out.println("noInformationForModsShort");
		System.out.println(lang.noInformationForModsShort());
	}

	@Mock
	IRCBotUser user;
	
	@Mock
	OsuApiUser apiUser;
	
	public HebrewTest() {
		MockitoAnnotations.initMocks(this);
		
		when(user.getNick()).thenReturn("username");
		when(user.message(anyString())).then(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				System.out.println("Tillerino sends: " + invocation.getArguments()[0]);
				return null;
			}
		});
		when(user.action(anyString())).then(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				System.out.println("Tillerino action: " + invocation.getArguments()[0]);
				return null;
			}
		});
		
		when(apiUser.getUserName()).thenReturn("username");
	}
	
	@Test
	public void testWelcomeUser() throws Exception {
		System.out.println("welcomeUser 10 seconds");
		lang.welcomeUser(user, apiUser, 10 * 1000);
		System.out.println("welcomeUser 5 hours");
		lang.welcomeUser(user, apiUser, 5 * 3600 * 1000);
		System.out.println("welcomeUser 2 days");
		lang.welcomeUser(user, apiUser, 2l * 24 * 3600 * 1000);
		System.out.println("welcomeUser 10 days");
		lang.welcomeUser(user, apiUser, 10l * 24 * 3600 * 1000);
	}

	@Test
	public void testUnknownCommand() throws Exception {
		System.out.println("unknownBeatmap");
		System.out.println(lang.unknownBeatmap());
	}

	@Test
	public void testNoInformationForMods() throws Exception {
		System.out.println("noInformationForMods");
		System.out.println(lang.noInformationForMods());
	}

	@Test
	public void testMalformattedMods() throws Exception {
		System.out.println("malformattedMods");
		System.out.println(lang.malformattedMods("DZ+ET"));
	}

	@Test
	public void testNoLastSongInfo() throws Exception {
		System.out.println("noLastSongInfo");
		System.out.println(lang.noLastSongInfo());
	}

	@Test
	public void testTryWithMods() throws Exception {
		System.out.println("tryWithMods unspecific");
		System.out.println(lang.tryWithMods());
	}

	@Test
	public void testTryWithModsSpecific() throws Exception {
		System.out.println("tryWithMods specific");
		System.out.println(lang.tryWithMods(Arrays.asList(Mods.Hidden, Mods.HardRock)));
	}

	@Test
	public void testUnresolvableName() throws Exception {
		System.out.println("unresolvableName");
		System.out.println(lang.unresolvableName("6v3gg63v", "herpaderpa"));
	}

	@Test
	public void testExcuseForError() throws Exception {
		System.out.println("excuseForError");
		System.out.println(lang.excuseForError());
	}

	@Test
	public void testComplaint() throws Exception {
		System.out.println("complaint");
		System.out.println(lang.complaint());
	}

	@Test
	public void testHug() throws Exception {
		System.out.println("hug");
		lang.hug(user, apiUser);
	}

	@Test
	public void testHelp() throws Exception {
		System.out.println("help");
		System.out.println(lang.help());
	}

	@Test
	public void testFaq() throws Exception {
		System.out.println("faq");
		System.out.println(lang.faq());
	}

	@Test
	public void testFeatureRankRestricted() throws Exception {
		System.out.println("featureRankRestricted");
		System.out.println(lang.featureRankRestricted("gamma", 100000, apiUser));
	}

	@Test
	public void testMixedNomodAndMods() throws Exception {
		System.out.println("mixedNomodAndMods");
		System.out.println(lang.mixedNomodAndMods());
	}

	@Test
	public void testOutOfRecommendations() throws Exception {
		System.out.println("outOfRecommendations");
		System.out.println(lang.outOfRecommendations());
	}

	@Test
	public void testNotRanked() throws Exception {
		System.out.println("notRanked");
		System.out.println(lang.notRanked());
	}

	@Test
	public void testOptionalCommentOnNP() throws Exception {
		System.out.println("optionalCommentOnNP");
		lang.optionalCommentOnNP(user, apiUser, null);
	}

	@Test
	public void testOptionalCommentOnWith() throws Exception {
		System.out.println("optionalCommentOnWith");
		lang.optionalCommentOnWith(user, apiUser, null);
	}

	@Test
	public void testOptionalCommentOnRecommendation() throws Exception {
		System.out.println("optionalCommentOnRecommendation");
		lang.optionalCommentOnRecommendation(user, apiUser, null);
	}

	@Test
	public void testInvalidAccuracy() throws Exception {
		System.out.println("invalidAccuracy");
		System.out.println(lang.invalidAccuracy("lOl.3%"));
	}

	@Test
	public void testNoPercentageEstimates() throws Exception {
		System.out.println("noPercentageEstimates");
		System.out.println(lang.noPercentageEstimates());
	}

	@Test
	public void testOptionalCommentOnLanguage() throws Exception {
		System.out.println("optionalCommentOnLanguage");
		lang.optionalCommentOnLanguage(user, apiUser);
	}

	@Test
	public void testInvalidChoice() throws Exception {
		System.out.println("invalidChoice");
		System.out.println(lang.invalidChoice("ice cream", "cookies, chocolate"));
	}

	@Test
	public void testSetFormat() throws Exception {
		System.out.println("setFormat");
		System.out.println(lang.setFormat());
	}
}

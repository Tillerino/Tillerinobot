package org.tillerino.ppaddict.server;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.apache.commons.lang3.function.Failable;
import org.junit.Test;
import org.tillerino.ppaddict.util.TestClock;
import org.tillerino.ppaddict.util.TestModule;

import tillerino.tillerinobot.AbstractDatabaseTest;

@TestModule(TestClock.Module.class)
public class PpaddictUserDataServiceTest extends AbstractDatabaseTest {
	@Inject
	TestClock clock;

	@Inject
	PpaddictUserDataService userDataService;

	@Test
	public void testServerUserData() throws Exception {
		PersistentUserData userData = new PersistentUserData();

		userData.setLinkedOsuId(12345);
		userData.getSettings().setHighAccuracy(97);

		userDataService.saveUserData("id:identifier", userData);

		assertThat(userDataService.loadUserData("id:identifier"))
				.hasValueSatisfying(loaded -> assertThat(loaded)
						.hasFieldOrPropertyWithValue("linkedOsuId", 12345)
						.hasFieldOrPropertyWithValue("settings.highAccuracy", 97D));

		userData.getSettings().setHighAccuracy(98);
		userDataService.saveUserData("id:identifier", userData);
		assertThat(userDataService.loadUserData("id:identifier"))
				.hasValueSatisfying(loaded -> assertThat(loaded).hasFieldOrPropertyWithValue("settings.highAccuracy", 98D));
	}

	@Test
	public void testLinking() throws Exception {
		String token = userDataService.getLinkString("auth-provider:uniqueid", "Authenticated Person");
		assertThat(userDataService.tryLinkToPpaddict(token, 12345)).contains("Authenticated Person");
		assertThat(userDataService.loadUserData("auth-provider:uniqueid"))
				.hasValueSatisfying(data -> assertThat(data).hasFieldOrPropertyWithValue("linkedOsuId", 12345));
		assertThat(userDataService.loadUserData("osu:12345"))
				.hasValueSatisfying(data -> assertThat(data).hasFieldOrPropertyWithValue("linkedOsuId", 12345));
	}

	@Test
	public void cannotLinkTwice() throws Exception {
		String token = userDataService.getLinkString("auth-provider:uniqueid", "Authenticated Person");
		assertThat(userDataService.tryLinkToPpaddict(token, 12345)).contains("Authenticated Person");

		// not with the same token
		assertThat(userDataService.tryLinkToPpaddict(token, 6789)).isEmpty();

		// not with a new token
		token = userDataService.getLinkString("auth-provider:uniqueid", "Authenticated Person");
		assertThat(userDataService.tryLinkToPpaddict(token, 6789)).isEmpty();
	}

	@Test
	public void cannotChainLink() throws Exception {
		String token = userDataService.getLinkString("osu:123", "An osu account");
		assertThat(userDataService.tryLinkToPpaddict(token, 573489)).isEmpty();
	}

	@Test
	public void userDataIsCopied() throws Exception {
		{
			PersistentUserData userData = new PersistentUserData();
			userData.getSettings().setHighAccuracy(95);
			userDataService.saveUserData("auth-provider:uniqueid", userData);
		}
		{
			String token = userDataService.getLinkString("auth-provider:uniqueid", "Authenticated Person");
			assertThat(userDataService.tryLinkToPpaddict(token, 12345)).contains("Authenticated Person");
		}
		assertThat(userDataService.loadUserData("osu:12345"))
				.hasValueSatisfying(data -> assertThat(data).hasFieldOrPropertyWithValue("settings.highAccuracy", 95D));
	}

	@Test
	public void userDataIsTaken() throws Exception {
		{
			PersistentUserData userData = new PersistentUserData();
			userData.getSettings().setHighAccuracy(94);
			userDataService.saveUserData("osu:12345", userData);
		}
		{
			String token = userDataService.getLinkString("auth-provider:uniqueid", "Authenticated Person");
			assertThat(userDataService.tryLinkToPpaddict(token, 12345)).contains("Authenticated Person");
		}
		assertThat(userDataService.loadUserData("auth-provider:uniqueid"))
				.hasValueSatisfying(data -> assertThat(data).hasFieldOrPropertyWithValue("settings.highAccuracy", 94D));
	}

	@Test
	public void changesAreSavedWhenLinked() throws Exception {
		{
			String token = userDataService.getLinkString("auth-provider:uniqueid", "Authenticated Person");
			assertThat(userDataService.tryLinkToPpaddict(token, 12345)).contains("Authenticated Person");
		}
		userDataService.loadUserData("auth-provider:uniqueid").ifPresent(Failable.asConsumer(data -> {
			data.getSettings().setHighAccuracy(99);
			userDataService.saveUserData("auth-provider:uniqueid", data);
		}));
		assertThat(userDataService.loadUserData("auth-provider:uniqueid"))
				.hasValueSatisfying(data -> assertThat(data).hasFieldOrPropertyWithValue("settings.highAccuracy", 99D));
	}

	@Test
	public void linkDoesntExist() throws Exception {
		assertThat(userDataService.tryLinkToPpaddict("fdsfsd", 12345)).isEmpty();
	}

	@Test
	public void linkIsExpired() throws Exception {
		String token = userDataService.getLinkString("auth-provider:uniqueid", "Authenticated Person");
		clock.advanceBy(61000);
		assertThat(userDataService.tryLinkToPpaddict(token, 12345)).isEmpty();
	}
}

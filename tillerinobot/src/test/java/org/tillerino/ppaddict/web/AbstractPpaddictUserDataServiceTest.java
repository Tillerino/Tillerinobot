package org.tillerino.ppaddict.web;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Test;
import org.tillerino.ppaddict.util.TestClock;
import org.tillerino.ppaddict.util.TestModule;

import tillerino.tillerinobot.AbstractDatabaseTest;

@TestModule(TestClock.Module.class)
public class AbstractPpaddictUserDataServiceTest extends AbstractDatabaseTest {
	@Inject
	TestClock clock;

	@Inject
	BarePpaddictUserDataService userDataService;

	@Test
	public void testServerUserData() throws Exception {
		ExamplePpaddictUserData userData = new ExamplePpaddictUserData();

		userData.setLinkedOsuId(12345);
		userData.setData("hi mom");

		userDataService.saveUserData("id:identifier", userData);

		assertThat(userDataService.loadUserData("id:identifier"))
				.hasValueSatisfying(loaded -> assertThat(loaded)
						.hasFieldOrPropertyWithValue("linkedOsuId", 12345)
						.hasFieldOrPropertyWithValue("data", "hi mom"));

		userData.setData("bye mom");
		userDataService.saveUserData("id:identifier", userData);
		assertThat(userDataService.loadUserData("id:identifier"))
				.hasValueSatisfying(loaded -> assertThat(loaded).hasFieldOrPropertyWithValue("data", "bye mom"));
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
			ExamplePpaddictUserData userData = new ExamplePpaddictUserData();
			userData.setData("existing settings");
			userDataService.saveUserData("auth-provider:uniqueid", userData);
		}
		{
			String token = userDataService.getLinkString("auth-provider:uniqueid", "Authenticated Person");
			assertThat(userDataService.tryLinkToPpaddict(token, 12345)).contains("Authenticated Person");
		}
		assertThat(userDataService.loadUserData("osu:12345"))
				.hasValueSatisfying(data -> assertThat(data).hasFieldOrPropertyWithValue("data", "existing settings"));
	}

	@Test
	public void userDataIsTaken() throws Exception {
		{
			ExamplePpaddictUserData userData = new ExamplePpaddictUserData();
			userData.setData("existing settings");
			userDataService.saveUserData("osu:12345", userData);
		}
		{
			String token = userDataService.getLinkString("auth-provider:uniqueid", "Authenticated Person");
			assertThat(userDataService.tryLinkToPpaddict(token, 12345)).contains("Authenticated Person");
		}
		assertThat(userDataService.loadUserData("auth-provider:uniqueid"))
				.hasValueSatisfying(data -> assertThat(data).hasFieldOrPropertyWithValue("data", "existing settings"));
	}

	@Test
	public void changesAreSavedWhenLinked() throws Exception {
		{
			String token = userDataService.getLinkString("auth-provider:uniqueid", "Authenticated Person");
			assertThat(userDataService.tryLinkToPpaddict(token, 12345)).contains("Authenticated Person");
		}
		userDataService.loadUserData("auth-provider:uniqueid").ifPresent(data -> {
			data.setData("a change");
			userDataService.saveUserData("auth-provider:uniqueid", data);
		});
		assertThat(userDataService.loadUserData("auth-provider:uniqueid"))
				.hasValueSatisfying(data -> assertThat(data).hasFieldOrPropertyWithValue("data", "a change"));
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

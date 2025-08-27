package tillerino.tillerinobot.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.tillerino.ppaddict.config.ConfigService;
import org.tillerino.ppaddict.util.MaintenanceException;
import org.tillerino.ppaddict.util.TestClock;

import dagger.Component;
import tillerino.tillerinobot.AbstractDatabaseTest;
import tillerino.tillerinobot.OsuApiV1;
import tillerino.tillerinobot.OsuApiV1Test;

public class PlayerTest extends AbstractDatabaseTest {
	@Component(modules = {Module.class, DockeredMysqlModule.class, OsuApiV1Test.Module.class})
	@Singleton
	interface Injector {
	  void inject(PlayerTest t);
	}
	@dagger.Module
	interface Module {
	}
	{
		DaggerPlayerTest_Injector.create().inject(this);
	}

	TestClock clock = new TestClock();

	@Inject OsuApiV1 downloader;

	protected ConfigService config = mock(ConfigService.class);

	@Test
	public void testUpdateLastSeen() throws Exception {
		clock.advanceBy(1); // = 1
		Player.updateLastSeen(2070907, db, 1);
		assertThat(Player.getPlayer(db, 2070907))
			.hasFieldOrPropertyWithValue("lastseen", 1L)
			.hasFieldOrPropertyWithValue("agetop50", 1L);

		clock.advanceBy(1); // = 2
		Player.updateLastSeen(2070907, db, 2);
		assertThat(Player.getPlayer(db, 2070907))
			.hasFieldOrPropertyWithValue("lastseen", 2L)
			.hasFieldOrPropertyWithValue("agetop50", 2L);

		Player updating = Player.getPlayer(db, 2070907);
		updating.updateTop50(db, 1, downloader, clock, config);
		verify(downloader).getUserTop(2070907, 0, 50);
		assertThat(updating).hasFieldOrPropertyWithValue("agetop50", 0L);

		// don't reduce
		Player.updateLastSeen(2070907, db, 1);
		assertThat(Player.getPlayer(db, 2070907))
			.hasFieldOrPropertyWithValue("lastseen", 2L)
			.hasFieldOrPropertyWithValue("agetop50", 0L);

		// crooked now
		clock.advanceBy(1); // =3
		Player.updateLastSeen(2070907, db, 3);
		assertThat(Player.getPlayer(db, 2070907))
			.hasFieldOrPropertyWithValue("lastseen", 3L)
			.hasFieldOrPropertyWithValue("agetop50", 1L);

		updating = Player.getPlayer(db, 2070907);
		updating.updateTop50(db, 0, downloader, clock, config);
		verify(downloader, times(2)).getUserTop(2070907, 0, 50);
		Mockito.verifyNoMoreInteractions(downloader);
		assertThat(updating).hasFieldOrPropertyWithValue("agetop50", 0L);
		assertThat(Player.getPlayer(db, 2070907))
			.hasFieldOrPropertyWithValue("lastseen", 3L)
			.hasFieldOrPropertyWithValue("agetop50", 0L);
	}

	@Test
	public void maintenance() throws Exception {
		when(config.scoresMaintenance()).thenReturn(true);
		clock.advanceBy(1000);
		Player player = Player.getPlayer(db, 1);
		assertThatThrownBy(() -> player.updateTop50(db, 1, downloader, clock, config))
			.isInstanceOf(MaintenanceException.class);
	}
}

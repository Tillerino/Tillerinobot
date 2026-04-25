package tillerino.tillerinobot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.tillerino.ppaddict.config.ConfigService;
import org.tillerino.ppaddict.util.MaintenanceException;
import tillerino.tillerinobot.data.ApiUser;
import tillerino.tillerinobot.data.Player;

public class PlayerServiceTest extends TestBase {

    protected final ConfigService config = mock(ConfigService.class);

    @Test
    public void testUpdateLastSeen() throws Exception {
        clock.advanceBy(1); // = 1
        playerService.registerActivity(2070907, 1);
        assertThat(playerService.getPlayer(db, 2070907))
                .hasFieldOrPropertyWithValue("lastseen", 1L)
                .hasFieldOrPropertyWithValue("agetop50", 1L);

        clock.advanceBy(1); // = 2
        playerService.registerActivity(2070907, 2);
        assertThat(playerService.getPlayer(db, 2070907))
                .hasFieldOrPropertyWithValue("lastseen", 2L)
                .hasFieldOrPropertyWithValue("agetop50", 2L);

        Player updating = playerService.getPlayer(db, 2070907);
        playerService.updateTop50(db, updating, 1, osuApi, clock, config);
        verify(osuApi).getUserTop(2070907, 0, 50);
        assertThat(updating).hasFieldOrPropertyWithValue("agetop50", 0L);

        // don't reduce
        playerService.registerActivity(2070907, 1);
        assertThat(playerService.getPlayer(db, 2070907))
                .hasFieldOrPropertyWithValue("lastseen", 2L)
                .hasFieldOrPropertyWithValue("agetop50", 0L);

        // crooked now
        clock.advanceBy(1); // =3
        playerService.registerActivity(2070907, 3);
        assertThat(playerService.getPlayer(db, 2070907))
                .hasFieldOrPropertyWithValue("lastseen", 3L)
                .hasFieldOrPropertyWithValue("agetop50", 1L);

        updating = playerService.getPlayer(db, 2070907);
        playerService.updateTop50(db, updating, 0, osuApi, clock, config);
        verify(osuApi, times(2)).getUserTop(2070907, 0, 50);
        Mockito.verifyNoMoreInteractions(osuApi);
        assertThat(updating).hasFieldOrPropertyWithValue("agetop50", 0L);
        assertThat(playerService.getPlayer(db, 2070907))
                .hasFieldOrPropertyWithValue("lastseen", 3L)
                .hasFieldOrPropertyWithValue("agetop50", 0L);

        ApiUser mockUser = mock(ApiUser.class);
        when(mockUser.getUserId()).thenReturn(2070907);
        assertThat(playerService.getLastActivity(mockUser)).isEqualTo(3L);
    }

    @Test
    public void maintenance() throws Exception {
        when(config.scoresMaintenance()).thenReturn(true);
        clock.advanceBy(1000);
        Player player = playerService.getPlayer(db, 1);
        assertThatThrownBy(() -> playerService.updateTop50(db, player, 1, osuApi, clock, config))
                .isInstanceOf(MaintenanceException.class);
    }
}

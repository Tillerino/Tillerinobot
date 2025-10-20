package org.tillerino.osupp.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tillerino.tillerinobot.AbstractDatabaseTest;
import tillerino.tillerinobot.DaggerAbstractDatabaseTest_Injector;
import tillerino.tillerinobot.OsuApi;
import tillerino.tillerinobot.OsuApiV1Test;
import tillerino.tillerinobot.data.ApiUser;

public class ApiUserTest extends AbstractDatabaseTest {
    {
        DaggerAbstractDatabaseTest_Injector.create().inject(this);
    }

    OsuApi downloader = OsuApiV1Test.Module.osuApiV1();

    @Test
    public void testDatabaseSchema() throws Exception {
        Assertions.assertThat(ApiUser.loadOrDownload(db, 2070907, 0, downloader))
                .isNotNull();

        ApiUser.loadOrDownload(db, 2070907, 0, downloader);
        Mockito.verify(downloader, Mockito.times(1)).getUser(2070907, 0);
    }
}

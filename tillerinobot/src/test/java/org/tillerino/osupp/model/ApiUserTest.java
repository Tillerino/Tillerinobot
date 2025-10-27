package org.tillerino.osupp.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tillerino.tillerinobot.*;
import tillerino.tillerinobot.data.ApiUser;

public class ApiUserTest extends TestBase {
    @Test
    public void testDatabaseSchema() throws Exception {
        Assertions.assertThat(ApiUser.loadOrDownload(db, 2070907, 0, osuApiV1)).isNotNull();

        ApiUser.loadOrDownload(db, 2070907, 0, osuApiV1);
        Mockito.verify(osuApiV1, Mockito.times(1)).getUser(2070907, 0);
    }
}

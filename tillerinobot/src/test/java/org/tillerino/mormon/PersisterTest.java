package org.tillerino.mormon;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.tillerino.mormon.Persister.Action;
import tillerino.tillerinobot.AbstractDatabaseTest;
import tillerino.tillerinobot.DaggerAbstractDatabaseTest_Injector;

public class PersisterTest extends AbstractDatabaseTest {
    {
        DaggerAbstractDatabaseTest_Injector.create().inject(this);
    }

    @Data
    @Table("simple_key")
    @KeyColumn("myKey")
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleKey {
        int myKey;

        String additionalProperty;

        static final String TABLE_DEF =
                "CREATE TABLE IF NOT EXISTS `simple_key` (`myKey` int, `additionalProperty` text)";
    }

    @Test
    public void testDelete() throws Exception {
        db.connection().createStatement().execute(SimpleKey.TABLE_DEF);
        db.truncate(SimpleKey.class); // clean up from previous tests

        assertEquals(0, (int) db.deleteFrom(SimpleKey.class).execute("where myKey = ", 2070907));

        db.persist(new SimpleKey(2070907, "more"), Action.INSERT);

        assertEquals(1, (int) db.deleteFrom(SimpleKey.class).execute("where myKey = ", 2070907));
    }

    @Test
    public void test() throws Exception {
        db.connection().createStatement().execute(SimpleKey.TABLE_DEF);
        db.truncate(SimpleKey.class); // clean up from previous tests

        SimpleKey player1 = new SimpleKey(1, "abc");

        SimpleKey player2 = new SimpleKey(2, "def");

        SimpleKey player3 = new SimpleKey(3, "ghi");

        try (Loader<SimpleKey> loader = db.loader(SimpleKey.class, " order by `myKey`")) {
            try (Persister<SimpleKey> persister = db.persister(SimpleKey.class, Action.INSERT)) {
                persister.persist(player1, 2);

                assertEquals(Collections.emptyList(), toList(loader.query()));

                persister.persist(player2, 2);

                assertEquals(Arrays.asList(player1, player2), toList(loader.query()));

                persister.persist(player3, 2);

                assertEquals(Arrays.asList(player1, player2), toList(loader.query()));
            }

            assertEquals(Arrays.asList(player1, player2, player3), toList(loader.query()));
        }
    }

    <T> List<T> toList(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();

        iterable.forEach(list::add);

        return list;
    }
}

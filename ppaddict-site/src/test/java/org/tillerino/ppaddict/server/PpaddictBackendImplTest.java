package org.tillerino.ppaddict.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.*;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.tillerino.ppaddict.server.PpaddictBackend.BeatmapData;
import org.tillerino.ppaddict.server.auth.Credentials;
import org.tillerino.ppaddict.util.ExecutorServiceRule;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.data.DiffEstimate;

public class PpaddictBackendImplTest extends AbstractPpaddictTest {
    @RegisterExtension
    public final ExecutorServiceRule<ExecutorService> exec = ExecutorServiceRule.singleThread("yo");

    @Test
    public void testCredentials() throws Exception {
        Credentials creds = new Credentials("id:identifier", "myName");

        String cookie = ppaddictBackend.createCookie(creds);

        Credentials fromDB = ppaddictBackend.resolveCookie(cookie);

        assertEquals(creds.displayName, fromDB.displayName);
        assertEquals(creds.expires, fromDB.expires);
        assertEquals(creds.identifier, fromDB.identifier);
    }

    @Test
    public void testGetBeatmaps() throws Exception {
        DiffEstimate estimate = new DiffEstimate();
        estimate.setBeatmapid(131891);
        estimate.setSuccess(true);
        estimate.setMd5("notset");
        diffEstimateRepo.insert(db.connection(), estimate);

        CountDownLatch blocker = new CountDownLatch(1);
        doAnswer(x -> {
                    blocker.await();
                    return x.callRealMethod();
                })
                .when(osuApi)
                .getBeatmap(Mockito.anyInt(), Mockito.anyLong());

        Future<?> submit = exec.submit(ppaddictBackend::reloadBeatmaps);
        assertThat(ppaddictBackend.getBeatmaps()).isEmpty();

        blocker.countDown();
        Awaitility.await().until(() -> !ppaddictBackend.getBeatmaps().isEmpty());
        Map<BeatmapWithMods, BeatmapData> first = ppaddictBackend.getBeatmaps();
        assertThat(first).hasEntrySatisfying(new BeatmapWithMods(131891, 0), data -> assertThat(data.beatmap())
                .hasFieldOrPropertyWithValue("artist", "The Quick Brown Fox")
                .hasFieldOrPropertyWithValue("title", "The Big Black")
                .hasFieldOrPropertyWithValue("version", "WHO'S AFRAID OF THE BIG BLACK"));
        submit.get();

        // load again
        ppaddictBackend.reloadBeatmaps();
        // map has been replaced, but beatmap is same to save memory
        Map<BeatmapWithMods, BeatmapData> second = ppaddictBackend.getBeatmaps();
        assertThat(second).isNotSameAs(first).hasSize(1).allSatisfy((k, v) -> assertThat(v)
                .isSameAs(first.get(k)));
    }

    @Test
    public void oldEstimateGetsRemoved() throws Exception {
        DiffEstimate estimate = new DiffEstimate();
        estimate.setBeatmapid(1234);
        estimate.setSuccess(true);
        estimate.setMd5("notset");
        diffEstimateRepo.insert(db.connection(), estimate);
        Assertions.assertThat(diffEstimateRepo.getAll(db.connection())).isNotEmpty();

        doReturn(null).when(osuApi).getBeatmap(1234, 0);
        ppaddictBackend.reloadBeatmaps();

        Assertions.assertThat(diffEstimateRepo.getAll(db.connection())).isEmpty();
    }

    /**
     * This tests the behaviour of the {@link ConcurrentHashMap} iterator under modification. What's important for us is
     * that we can modify the map while it is being iterated over and that the iterator does not keep values in-memory.
     * The goal is to modify the ppaddict beatmap list in-place keeping memory-overhead low.
     */
    @Test
    public void testConcurrentBeatmapReplace() {
        Map<String, String> concurrentHashMap = new ConcurrentHashMap<>();
        concurrentHashMap.put("A", "X");
        concurrentHashMap.put("B", "Y");
        concurrentHashMap.put("C", "Z");
        Iterator<Entry<String, String>> it = concurrentHashMap.entrySet().iterator();
        assertThat(it.next()).returns("X", Entry::getValue);
        concurrentHashMap.put("B", "J");
        assertThat(it.next()).returns("J", Entry::getValue);
        assertThat(it.next()).returns("Z", Entry::getValue);
    }
}

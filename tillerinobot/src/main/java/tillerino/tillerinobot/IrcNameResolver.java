package tillerino.tillerinobot;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Semaphore;
import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tillerino.mormon.DatabaseManager;
import org.tillerino.mormon.Persister.Action;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.chat.IRCName;
import tillerino.tillerinobot.data.PullThrough;
import tillerino.tillerinobot.data.UserNameMapping;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
@Singleton
public class IrcNameResolver {
    private final DatabaseManager dbm;

    private final PullThrough pullThrough;

    private final Cache<String, Integer> resolvedIRCNames =
            CacheBuilder.newBuilder().maximumSize(100000).build();

    private final LoadingCache<String, Semaphore> semaphores =
            CacheBuilder.newBuilder().maximumSize(10000).build(CacheLoader.from(() -> new Semaphore(1)));

    /*
     * cached
     */
    @SuppressFBWarnings(value = "TQ", justification = "producer")
    @CheckForNull
    public @UserId Integer resolveIRCName(@IRCName String ircName)
            throws SQLException, IOException, InterruptedException {
        Semaphore semaphore = semaphores.getUnchecked(ircName);
        semaphore.acquire();
        try {
            Integer resolved = resolvedIRCNames.getIfPresent(ircName);

            if (resolved != null) {
                return resolved;
            }

            resolved = getIDByUserName(ircName);
            if (resolved == null) {
                return null;
            }
            resolvedIRCNames.put(ircName, resolved);
            return resolved;
        } finally {
            semaphore.release();
        }
    }

    @CheckForNull
    public Integer getIDByUserName(@IRCName String userName) throws IOException, SQLException {
        UserNameMapping mapping = dbm.selectUnique(UserNameMapping.class)
                .execute("where userName = ", userName)
                .orElse(null);

        long maxAge = 90l * 24 * 60 * 60 * 1000;
        if (System.currentTimeMillis() < 1483747380000l /* sometime January 7th, 2017*/) {
            // decrease by 3 months over 1 week
            maxAge += Math.min(maxAge, 12 * (1483747380000l - System.currentTimeMillis()));
        }
        if (mapping == null
                || (mapping.getUserid() > 0 && mapping.getResolved() < System.currentTimeMillis() - maxAge)
                || (mapping.getUserid() < 0 && mapping.getResolved() < System.currentTimeMillis() - 24 * 60 * 60 * 1000)
                || (mapping.getUserid() < 0
                        && mapping.getResolved() < System.currentTimeMillis() - 60 * 60 * 1000
                        && mapping.getFirstresolveattempt() > System.currentTimeMillis() - 24 * 60 * 60 * 1000)) {
            if (mapping == null) {
                mapping = new UserNameMapping();
                mapping.setUserName(userName);
                mapping.setFirstresolveattempt(System.currentTimeMillis());
            }

            OsuApiUser user;
            try {
                user = pullThrough.downloadUser(userName);
            } catch (IOException e) {
                if (IRCBot.isTimeout(e) && mapping.getUserid() > 0) {
                    log.debug("timeout downloading user " + userName + "; return stale id.");
                    return mapping.getUserid();
                }
                throw e;
            }

            if (user != null) {
                mapping.setUserid(user.getUserId());
            } else {
                mapping.setUserid(-1);
            }

            mapping.setResolved(System.currentTimeMillis());

            dbm.persist(mapping, Action.REPLACE);
        }

        if (mapping.getUserid() == -1) {
            return null;
        }

        return mapping.getUserid();
    }

    /**
     * Tries to resolve a user manually by checking their user id.
     *
     * @param userId the user id to be checked. Information about this will be pulled from the osu API.
     * @return the resolved user or null if the user id does not exist in the API.
     */
    @CheckForNull
    public OsuApiUser resolveManually(@UserId int userId) throws SQLException, IOException {
        OsuApiUser user = pullThrough.getUser(userId, 1l);
        if (user == null) {
            return null;
        }
        String ircName = getIrcUserName(user);
        setMapping(ircName, userId);
        return user;
    }

    @SuppressFBWarnings(value = "TQ", justification = "Producer")
    public static @IRCName String getIrcUserName(OsuApiUser user) {
        return user.getUserName().replace(' ', '_');
    }

    /**
     * Explicitly maps an IRC name to an osu! user id.
     *
     * @param ircName the IRC nickname of the user
     * @param userId the osu! user id
     */
    public void setMapping(@IRCName String ircName, @UserId int userId) throws SQLException {
        UserNameMapping mapping = new UserNameMapping();
        mapping.setResolved(System.currentTimeMillis());
        mapping.setUserid(userId);
        mapping.setUserName(ircName);
        dbm.persist(mapping, Action.REPLACE);
        resolvedIRCNames.invalidate(ircName);
    }

    /**
     * Force a redownload and remapping of an IRC user based on her IRC name.
     *
     * @param ircName The IRC name of the user. This is passed directly to the osu! API. This will work most of the time
     *     even if spaces were replaced by underscores in the IRC name.
     * @return if found, the osuApiUser belonging to the IRC user. Null otherwise.
     */
    @CheckForNull
    public OsuApiUser redownloadUser(@IRCName String ircName) throws IOException, SQLException {
        OsuApiUser apiUser = pullThrough.downloadUser(ircName);
        if (apiUser == null) {
            setMapping(ircName, -1);
        } else {
            setMapping(ircName, apiUser.getUserId());
        }
        return apiUser;
    }
}

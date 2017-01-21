package tillerino.tillerinobot;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Semaphore;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.UserId;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.data.UserNameMapping;
import tillerino.tillerinobot.data.repos.UserNameMappingRepository;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
@Singleton
public class IrcNameResolver {
	private final UserNameMappingRepository repo;

	private final BotBackend backend;

	private final Cache<String, Integer> resolvedIRCNames = CacheBuilder
			.newBuilder().maximumSize(100000).build();

	private final LoadingCache<String, Semaphore> semaphores = CacheBuilder.newBuilder().maximumSize(10000)
			.build(CacheLoader.from(() -> new Semaphore(1)));

	/*
	 * cached
	 */
	@SuppressFBWarnings(value = "TQ", justification = "producer")
	@CheckForNull
	public @UserId Integer resolveIRCName(String ircName) throws SQLException,
			IOException, InterruptedException {
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
	public Integer getIDByUserName(String userName) throws IOException,
			SQLException {
		UserNameMapping mapping = repo.findOne(userName);

		long maxAge = 90l * 24 * 60 * 60 * 1000;
		if (System.currentTimeMillis() < 1483747380000l /* sometime January 7th, 2017*/) {
			// decrease by 3 months over 1 week
			maxAge += Math.min(maxAge, 12 * (1483747380000l - System.currentTimeMillis()));
		}
		if (mapping == null
				|| (mapping.getUserid() > 0 && mapping.getResolved() < System
						.currentTimeMillis() - maxAge)
				|| (mapping.getUserid() < 0 && mapping.getResolved() < System
						.currentTimeMillis() - 24 * 60 * 60 * 1000)
				|| (mapping.getUserid() < 0
						&& mapping.getResolved() < System.currentTimeMillis() - 60 * 60 * 1000 && mapping
						.getFirstresolveattempt() > System.currentTimeMillis()
						- 24 * 60 * 60 * 1000)) {
			if (mapping == null) {
				mapping = new UserNameMapping();
				mapping.setUserName(userName);
				mapping.setFirstresolveattempt(System.currentTimeMillis());
			}

			OsuApiUser user;
			try {
				user = backend.downloadUser(userName);
			} catch (IOException e) {
				if (IRCBot.isTimeout(e) && mapping.getUserid() > 0) {
					log.debug("timeout downloading user " + userName
							+ "; return stale id.");
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

			repo.save(mapping);
		}

		if (mapping.getUserid() == -1) {
			return null;
		}

		return mapping.getUserid();
	}

	/**
	 * Tries to resolve a user manually by checking their user id.
	 * 
	 * @param userid
	 *            the user id to be checked. Information about this will be
	 *            pulled from the osu api.
	 * @return the resolved user or null if the user id does not exist in the
	 *         API.
	 */
	@CheckForNull
	public OsuApiUser resolveManually(@UserId int userid) throws SQLException,
			IOException {
		OsuApiUser user = backend.getUser(userid, 1l);
		if (user == null) {
			return null;
		}
		UserNameMapping mapping = new UserNameMapping();
		mapping.setResolved(System.currentTimeMillis());
		mapping.setUserid(userid);
		String ircName = user.getUserName().replace(' ', '_');
		mapping.setUserName(ircName);
		repo.save(mapping);
		resolvedIRCNames.invalidate(ircName);
		return user;
	}
}

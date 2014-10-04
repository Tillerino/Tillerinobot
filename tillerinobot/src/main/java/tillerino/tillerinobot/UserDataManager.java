package tillerino.tillerinobot;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.UserId;

import tillerino.tillerinobot.lang.Default;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.lang.Tsundere;
import tillerino.tillerinobot.mbeans.AbstractMBeanRegistration;
import tillerino.tillerinobot.mbeans.CacheMXBean;
import tillerino.tillerinobot.mbeans.CacheMXBeanImpl;
import tillerino.tillerinobot.mbeans.UserDataManagerMXBean;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Manager for serializing and caching user data. Since user data can be
 * extremely volatile, we'll keep the data in cache and only serialize it when
 * the entry is being invalidated or the VM is being shut down. This might be a
 * bad idea.
 * 
 * @author Tillerino
 */
@Slf4j
@Singleton
public class UserDataManager extends AbstractMBeanRegistration implements UserDataManagerMXBean {
	/**
	 * Bot-specific user data. It is only saved when changed and responsible for
	 * determining if it has been changed. Manual getters and setters must be
	 * written with caution.
	 * 
	 * @author Tillerino
	 */
	public static class UserData {
		public enum LanguageIdentifier {
			Default(Default.class),
			Tsundere(Tsundere.class);
			
			Class<? extends Language> cls;

			private LanguageIdentifier(Class<? extends Language> cls) {
				this.cls = cls;
			}
		}
		
		@Data
		public static class BeatmapWithMods {
			public BeatmapWithMods(@BeatmapId int beatmap,
					@BitwiseMods long mods) {
				super();
				this.beatmap = beatmap;
				this.mods = mods;
			}

			@BeatmapId
			@Getter(onMethod = @__(@BeatmapId))
			@Setter(onParam = @__(@BeatmapId))
			int beatmap;

			@BitwiseMods
			@Getter(onMethod = @__(@BitwiseMods))
			@Setter(onParam = @__(@BitwiseMods))
			long mods;
		}

		transient boolean changed = false;

		public void setChanged(boolean changed) {
			this.changed = changed;
			
			if(!changed) {
				language.setChanged(changed);
			}
		}
		
		public boolean isChanged() {
			return changed || language.isChanged();
		}
		
		@Getter(onMethod = @__({ @Nonnull }))
		LanguageIdentifier languageIdentifier = LanguageIdentifier.Default;
		
		public void setLanguage(@Nonnull LanguageIdentifier languagePack) {
			if (this.languageIdentifier != languagePack) {
				changed = true;

				this.languageIdentifier = languagePack;

				this.language = null;
				this.serializedLanguage = null;
			}
		}
		
		transient Language language;
		
		public Language getLanguage() {
			if (language == null) {
				if (serializedLanguage != null) {
					language = gson.fromJson(serializedLanguage,
							languageIdentifier.cls);
				} else {
					try {
						language = languageIdentifier.cls.newInstance();
					} catch (InstantiationException | IllegalAccessException e) {
						throw new RuntimeException(languageIdentifier.cls
								+ " needs an accessible no-arg constructor", e);
					}
				}
			}
			return language;
		}

		@Getter(onMethod = @__({ @CheckForNull }))
		BeatmapWithMods lastSongInfo = null;
		
		public void setLastSongInfo(BeatmapWithMods lastSongInfo) {
			changed |= !Objects.equals(this.lastSongInfo, lastSongInfo);

			this.lastSongInfo = lastSongInfo;
		}

		/*
		 * we only use this field for serialization purposes. it should not be
		 * accessed from outside of UserDataManager. This field should be kept
		 * at the end because it may get large.
		 */
		JsonObject serializedLanguage;

		transient BotBackend backend;

		@UserId
		transient int userid;

		public int getHearts() throws SQLException, IOException {
			OsuApiUser user = backend.getUser(userid, 0);
			if (user == null)
				return 0;
			return backend.getDonator(user);
		}
	}
	
	BotBackend backend;
	
	@Inject
	public UserDataManager(BotBackend backend) {
		super();
		this.backend = backend;

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				cache.invalidateAll();
			}
		});
	}

	@Override
	public ObjectName preRegister(MBeanServer server, ObjectName objectName)
			throws Exception {
		server.registerMBean(cacheMXBean, null);
		return super.preRegister(server, objectName);
	}

	public UserData getData(int userid) throws SQLException {
		try {
			return cache.get(userid);
		} catch (ExecutionException e) {
			try {
				throw e.getCause();
			} catch (SQLException | RuntimeException f) {
				throw f;
			} catch (Throwable f) {
				throw new RuntimeException(e);
			}
		}
	}

	LoadingCache<Integer, UserData> cache = CacheBuilder.newBuilder()
			.expireAfterAccess(1, TimeUnit.HOURS).maximumSize(1000).recordStats()
			.removalListener(new RemovalListener<Integer, UserData>() {
				@Override
				@SuppressFBWarnings(value = "TQ")
				public void onRemoval(RemovalNotification<Integer, UserData> notification) {
					try {
						saveOptions(notification.getKey(), notification.getValue());
					} catch (SQLException e) {
						log.error("error saving user data", e);
					}
				}
			}).build(new CacheLoader<Integer, UserData>() {
				@Override
				public UserData load(Integer key) throws SQLException {
					return UserDataManager.this.load(key);
				}
			});
	
	CacheMXBean cacheMXBean = new CacheMXBeanImpl(cache, getClass(), "userDataCache");

	@Override
	public CacheMXBean fetchCache() {
		return cacheMXBean;
	}
	
	static Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting()
			.create();
	
	private UserData load(@UserId int key) throws SQLException {
		String rawOptions = backend.getOptions(key);
		
		UserData options;
		if(rawOptions == null || rawOptions.isEmpty()) {
			options = new UserData();
		} else {
			options = gson.fromJson(rawOptions, UserData.class);
		}

		options.backend = backend;
		options.userid = key;

		return options;
	}

	void saveOptions(@UserId int userid, UserData options) throws SQLException {
		if(!options.isChanged()) {
			return;
		}
		
		preSerialization(options);
		String serialized = gson.toJson(options);
		postSerialization(options);
		
		backend.saveOptions(userid, serialized);
		options.setChanged(false);
	}

	private void preSerialization(UserData options) {
		options.serializedLanguage = (JsonObject) gson
				.toJsonTree(options.language);
	}
	
	private void postSerialization(UserData options) {
		options.serializedLanguage = null;
	}
}

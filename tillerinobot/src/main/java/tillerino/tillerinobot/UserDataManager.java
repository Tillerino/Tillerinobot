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
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang3.StringUtils;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.UserId;

import tillerino.tillerinobot.data.BotUserData;
import tillerino.tillerinobot.data.repos.BotUserDataRepository;
import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager;
import tillerino.tillerinobot.lang.*;
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
public class UserDataManager extends AbstractMBeanRegistration implements UserDataManagerMXBean, TidyObject {
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
			English(Default.class),
			Tsundere(TsundereEnglish.class),
			TsundereGerman(TsundereGerman.class),
			Italiano(Italiano.class),
			Français(Francais.class),
			Polski(Polski.class),
			Nederlands(Nederlands.class),
			עברית(Hebrew.class),
			Farsi(Farsi.class),
			Português_BR(Portuguese.class),
			Deutsch(Deutsch.class),
			Čeština(Czech.class),
			Magyar(Hungarian.class),
			한국어(Korean.class),
			Dansk(Dansk.class),
			Türkçe(Turkish.class),
			日本語(Japanese.class),
			Español(Spanish.class),
			Ελληνικά(Greek.class),
			Русский(Russian.class),
			Lietuvių(Lithuanian.class),
			Português_PT(PortuguesePortugal.class),
			Svenska(Svenska.class),
			Romana(Romana.class),
			繁體中文(ChineseTraditional.class),
			български(Bulgarian.class),
			Norsk(Norwegian.class),
			Indonesian(Indonesian.class),
			简体中文(ChineseSimple.class),
			Català(Catalan.class),
			Slovenščina(Slovenian.class),
			; // please end identifier entries with a comma and leave this semicolon here
			
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
				getLanguage().setChanged(changed);
			}
		}
		
		public boolean isChanged() {
			return changed || getLanguage().isChanged();
		}
		
		@Getter
		boolean allowedToDebug = false;
		
		public void setAllowedToDebug(boolean allowedToDebug) {
			changed |= allowedToDebug != this.allowedToDebug;
			
			this.allowedToDebug = allowedToDebug;
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
		
		@CheckForNull
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

		@Getter(onMethod = @__({ @CheckForNull }))
		String defaultRecommendationOptions = null;

		public void setDefaultRecommendationOptions(String defaultRecommendationOptions) {
			changed |= !Objects.equals(this.defaultRecommendationOptions, defaultRecommendationOptions);

			this.defaultRecommendationOptions = defaultRecommendationOptions;
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

		@Getter
		boolean showWelcomeMessage = true;

		public void setShowWelcomeMessage(boolean welcomeMessage) {
			changed |= welcomeMessage != this.showWelcomeMessage;
			this.showWelcomeMessage = welcomeMessage;
		}

		@Getter
		boolean osuTrackWelcomeEnabled = false;

		public void setOsuTrackWelcomeEnabled(boolean doOsuTrackUpdateOnWelcome) {
			changed |= doOsuTrackUpdateOnWelcome != this.osuTrackWelcomeEnabled;
			this.osuTrackWelcomeEnabled = doOsuTrackUpdateOnWelcome;
		}
	}
	
	final BotBackend backend;
	
	final EntityManagerFactory emf;
	
	final ThreadLocalAutoCommittingEntityManager em;
	
	final BotUserDataRepository repository;
	
	final ShutdownHook hook = new ShutdownHook(this);
	
	@Inject
	public UserDataManager(BotBackend backend, EntityManagerFactory emf, ThreadLocalAutoCommittingEntityManager em,
			BotUserDataRepository repository) {
		super();
		this.backend = backend;
		this.emf = emf;
		this.em = em;
		this.repository = repository;

		hook.add();
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
		BotUserData data = repository.findByUserId(key);
		
		UserData options;
		if(data == null || StringUtils.isEmpty(data.getUserdata())) {
			options = new UserData();
		} else {
			options = gson.fromJson(data.getUserdata(), UserData.class);
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
		
		BotUserData data = new BotUserData();
		data.setUserId(userid);
		data.setUserdata(serialized);
		repository.save(data);
		options.setChanged(false);
	}

	private void preSerialization(UserData options) {
		options.serializedLanguage = (JsonObject) gson
				.toJsonTree(options.getLanguage());
	}
	
	private void postSerialization(UserData options) {
		options.serializedLanguage = null;
	}

	@Override
	public void tidyUp(boolean fromShutdownHook) {
		log.info("tidyUp({})", fromShutdownHook);
		
		boolean createEm = false;
		if (!em.isThreadLocalEntityManagerPresent() || !em.isOpen()) {
			createEm = true;
			em.setThreadLocalEntityManager(emf.createEntityManager());
		}
		try {
			cache.invalidateAll();
		} finally {
			if (createEm) {
				em.close();
			}
		}
		
		hook.remove(fromShutdownHook);
	}
}

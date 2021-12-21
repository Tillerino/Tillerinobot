package tillerino.tillerinobot;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableFunction;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.UserId;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import tillerino.tillerinobot.data.BotUserData;
import tillerino.tillerinobot.data.repos.BotUserDataRepository;
import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.lang.LanguageIdentifier;
import tillerino.tillerinobot.util.IsMutable;

/**
 * Manager for serializing and caching user data. Since user data can be
 * extremely volatile, we'll keep the data in cache and only serialize it when
 * the entry is being invalidated or the VM is being shut down. This might be a
 * bad idea.
 * 
 * @author Tillerino
 */
@Singleton
public class UserDataManager {
	/**
	 * Bot-specific user data. It is only saved when changed and responsible for
	 * determining if it has been changed. Manual getters and setters must be
	 * written with caution.
	 * 
	 * @author Tillerino
	 */
	public static class UserData implements Serializable, Closeable {
		private static final long serialVersionUID = 1L;

		@Value
		@Builder(toBuilder = true)
		@AllArgsConstructor(access = AccessLevel.PUBLIC)
		public static class BeatmapWithMods implements Serializable {
			private static final long serialVersionUID = 1L;

			@BeatmapId
			private int beatmap;

			@BitwiseMods
			private long mods;
		}

		transient boolean changed = false;

		public void setChanged(boolean changed) {
			this.changed = changed;
			
			if(!changed && (language instanceof IsMutable)) {
				((IsMutable) language).clearModified();
			}
		}
		
		public boolean isChanged() {
			return changed || (language instanceof IsMutable) && ((IsMutable) language).isModified();
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
		
		public <T, E extends Exception> T usingLanguage(FailableFunction<Language, T, E> task) throws E {
			if (language == null) {
				if (serializedLanguage != null) {
					language = gson.fromJson(serializedLanguage,
							languageIdentifier.cls);
				} else {
					try {
						language = languageIdentifier.cls.getDeclaredConstructor().newInstance();
					} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
						throw new RuntimeException(languageIdentifier.cls + " needs an accessible no-arg constructor", e);
					}
				}
			}
			return task.apply(language);
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
		@SuppressWarnings("squid:S1948")
		@SuppressFBWarnings("SE_BAD_FIELD")
		JsonObject serializedLanguage;

		transient UserDataManager manager;

		@UserId
		transient int userid;

		public int getHearts() throws SQLException, IOException {
			return manager.backend.getDonator(userid);
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

		@Override
		public void close() {
			manager.saveOptions(userid, this);
		}
	}
	
	final BotBackend backend;
	
	final EntityManagerFactory emf;
	
	final ThreadLocalAutoCommittingEntityManager em;
	
	final BotUserDataRepository repository;

	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Injection")
	@Inject
	public UserDataManager(BotBackend backend, EntityManagerFactory emf, ThreadLocalAutoCommittingEntityManager em,
			BotUserDataRepository repository) {
		super();
		this.backend = backend;
		this.emf = emf;
		this.em = em;
		this.repository = repository;
	}

	static Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting()
			.create();

	public UserData getData(@UserId int userid) {
		BotUserData data = repository.findByUserId(userid);

		UserData options;
		if(data == null || StringUtils.isEmpty(data.getUserdata())) {
			options = new UserData();
		} else {
			options = gson.fromJson(data.getUserdata(), UserData.class);
		}

		options.manager = this;
		options.userid = userid;

		return options;
	}

	void saveOptions(@UserId int userid, UserData options) {
		if (!options.isChanged()) {
			return;
		}

		options.serializedLanguage = (JsonObject) options.usingLanguage(lang -> gson.toJsonTree(lang));
		String serialized = gson.toJson(options);

		BotUserData data = new BotUserData();
		data.setUserId(userid);
		data.setUserdata(serialized);
		repository.save(data);
		options.setChanged(false);
	}
}

package tillerino.tillerinobot;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableFunction;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.UserId;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
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
	public static class UserData implements Closeable {
		public record BeatmapWithMods(@BeatmapId int beatmap, @BitwiseMods long mods) { }

		private transient boolean changed = false;

		public void setChanged(boolean changed) {
			this.changed = changed;
			
			if(!changed && language instanceof IsMutable mutable) {
				mutable.clearModified();
			}
		}
		
		public boolean isChanged() {
			return changed || language instanceof IsMutable mutable && mutable.isModified();
		}
		
		@Getter
		private boolean allowedToDebug = false;
		
		public void setAllowedToDebug(boolean allowedToDebug) {
			changed |= allowedToDebug != this.allowedToDebug;
			
			this.allowedToDebug = allowedToDebug;
		}
		
		@Getter(onMethod = @__({ @Nonnull }))
		private LanguageIdentifier languageIdentifier = LanguageIdentifier.Default;
		
		public void setLanguage(@Nonnull LanguageIdentifier languagePack) {
			if (this.languageIdentifier != languagePack) {
				changed = true;

				this.languageIdentifier = languagePack;

				this.language = null;
				this.serializedLanguage = null;
			}
		}
		
		@CheckForNull
		private transient Language language;
		
		public <T, E extends Exception> T usingLanguage(FailableFunction<Language, T, E> task) throws E {
			if (language == null) {
				if (serializedLanguage != null && !serializedLanguage.isNull()) {
					try {
						language = JACKSON.treeToValue(serializedLanguage, languageIdentifier.cls);
					} catch (JsonProcessingException e) {
						throw new RuntimeException("Language data cannot be read", e);
					}
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
		private BeatmapWithMods lastSongInfo = null;
		
		public void setLastSongInfo(BeatmapWithMods lastSongInfo) {
			changed |= !Objects.equals(this.lastSongInfo, lastSongInfo);

			this.lastSongInfo = lastSongInfo;
		}

		@Getter(onMethod = @__({ @CheckForNull }))
		private String defaultRecommendationOptions = null;

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
		private JsonNode serializedLanguage;

		private transient UserDataManager manager;

		@UserId
		private transient int userid;

		public int getHearts() throws SQLException, IOException {
			return manager.backend.getDonator(userid);
		}

		@Getter
		private boolean showWelcomeMessage = true;

		public void setShowWelcomeMessage(boolean welcomeMessage) {
			changed |= welcomeMessage != this.showWelcomeMessage;
			this.showWelcomeMessage = welcomeMessage;
		}

		@Getter
		private boolean osuTrackWelcomeEnabled = false;

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
	
	final ThreadLocalAutoCommittingEntityManager em;
	
	final BotUserDataRepository repository;

	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Injection")
	@Inject
	public UserDataManager(BotBackend backend, ThreadLocalAutoCommittingEntityManager em, BotUserDataRepository repository) {
		this.backend = backend;
		this.em = em;
		this.repository = repository;
	}

	/**
	 * This is a very specially JSON de-/serializer which we use for the weird way we set up serialization.
	 * We always serialize fields, never via getters and setters.
	 */
	private static final ObjectMapper JACKSON = new ObjectMapper()
			.setSerializationInclusion(Include.ALWAYS)
			.enable(SerializationFeature.INDENT_OUTPUT)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
			.setVisibility(PropertyAccessor.GETTER, Visibility.NONE)
			.setVisibility(PropertyAccessor.SETTER, Visibility.NONE)
			.registerModule(new ParameterNamesModule());

	public UserData getData(@UserId int userid) {
		BotUserData data = repository.findByUserId(userid);

		UserData options;
		if(data == null || StringUtils.isEmpty(data.getUserdata())) {
			options = new UserData();
		} else {
			try {
				options = JACKSON.readValue(data.getUserdata(), UserData.class);
			} catch (JsonProcessingException e) {
				throw new RuntimeException("Can't read user data", e);
			}
		}

		options.manager = this;
		options.userid = userid;

		return options;
	}

	void saveOptions(@UserId int userid, UserData options) {
		if (!options.isChanged()) {
			return;
		}

		if (options.usingLanguage(lang -> lang instanceof IsMutable mutable && mutable.isModified())) {
			options.serializedLanguage = options.usingLanguage(JACKSON::valueToTree);
		}
		String serialized;
		try {
			serialized = JACKSON.writeValueAsString(options);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Cannot serialize options", e);
		}

		BotUserData data = new BotUserData();
		data.setUserId(userid);
		data.setUserdata(serialized);
		repository.save(data);
		options.setChanged(false);
	}
}

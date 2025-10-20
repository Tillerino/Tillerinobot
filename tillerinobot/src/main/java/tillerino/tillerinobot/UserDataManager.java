package tillerino.tillerinobot;

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
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Objects;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableFunction;
import org.tillerino.mormon.DatabaseManager;
import org.tillerino.mormon.Persister.Action;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.util.PhaseTimer;
import tillerino.tillerinobot.data.BotUserData;
import tillerino.tillerinobot.diff.DiffEstimateProvider;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.lang.LanguageIdentifier;
import tillerino.tillerinobot.util.IsMutable;

@Singleton
@SuppressFBWarnings(value = "SA_LOCAL_SELF_COMPARISON", justification = "Looks like a bug")
@Slf4j
public class UserDataManager {
    /**
     * Bot-specific user data. It is only saved when changed and responsible for determining if it has been changed.
     * Manual getters and setters must be written with caution.
     *
     * @author Tillerino
     */
    @SuppressFBWarnings(value = "SA_FIELD_SELF_COMPARISON", justification = "Looks like a bug")
    public static class UserData implements Closeable {
        @With
        public record BeatmapWithMods(
                @BeatmapId int beatmap, @BitwiseMods long mods) {
            public BeatmapWithMods nomod() {
                return new BeatmapWithMods(beatmap, 0);
            }

            public BeatmapWithMods diffMods() {
                return new BeatmapWithMods(beatmap, DiffEstimateProvider.getDiffMods(mods));
            }
        }

        private transient boolean changed = false;

        public void setChanged(boolean changed) {
            this.changed = changed;

            if (!changed && language instanceof IsMutable mutable) {
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

        @Getter(onMethod = @__({@Nonnull}))
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
                    try (var _ = PhaseTimer.timeTask("deserializeLanguage")) {
                        language = JACKSON.treeToValue(serializedLanguage, languageIdentifier.cls);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Language data cannot be read", e);
                    }
                } else {
                    try (var _ = PhaseTimer.timeTask("instantiateLanguage")) {
                        language =
                                languageIdentifier.cls.getDeclaredConstructor().newInstance();
                    } catch (InstantiationException
                            | IllegalAccessException
                            | InvocationTargetException
                            | NoSuchMethodException e) {
                        throw new RuntimeException(
                                languageIdentifier.cls + " needs an accessible no-arg constructor", e);
                    }
                }
            }
            return task.apply(language);
        }

        @Getter(onMethod = @__({@CheckForNull}))
        private BeatmapWithMods lastSongInfo = null;

        public void setLastSongInfo(BeatmapWithMods lastSongInfo) {
            changed |= !Objects.equals(this.lastSongInfo, lastSongInfo);

            this.lastSongInfo = lastSongInfo;
        }

        @Getter(onMethod = @__({@CheckForNull}))
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

        @Getter
        private transient @UserId int userid;

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

        @Getter
        private boolean showMapMetaDataOnRecommendation = true;

        public void setShowMapMetaDataOnRecommendation(boolean showMapMetaDataOnRecommendation) {
            changed |= showMapMetaDataOnRecommendation != this.showMapMetaDataOnRecommendation;
            this.showMapMetaDataOnRecommendation = showMapMetaDataOnRecommendation;
        }

        @Getter
        private boolean v2 = false;

        public void setV2(boolean v2) {
            changed |= v2 != this.v2;
            this.v2 = v2;
        }

        @Override
        public void close() {
            try {
                manager.saveUserData(this);
            } catch (SQLException e) {
                log.error("Error saving user data", e);
            }
        }
    }

    private final BotBackend backend;

    private final DatabaseManager dbm;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Injection")
    @Inject
    public UserDataManager(BotBackend backend, DatabaseManager dbm) {
        this.backend = backend;
        this.dbm = dbm;
    }

    /**
     * This is a very specially JSON de-/serializer which we use for the weird way we set up serialization. We always
     * serialize fields, never via getters and setters.
     */
    private static final ObjectMapper JACKSON = new ObjectMapper()
            .setSerializationInclusion(Include.ALWAYS)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
            .setVisibility(PropertyAccessor.GETTER, Visibility.NONE)
            .setVisibility(PropertyAccessor.SETTER, Visibility.NONE)
            .registerModule(new ParameterNamesModule());

    public UserData loadUserData(@UserId int userid) throws SQLException {
        try (var _ = PhaseTimer.timeTask("loadUserData")) {
            BotUserData data = dbm.selectUnique(BotUserData.class)
                    .execute("where userId = ", userid)
                    .orElse(null);

            UserData options;
            if (data == null || StringUtils.isEmpty(data.getUserdata())) {
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
    }

    public void saveUserData(UserData options) throws SQLException {
        if (!options.isChanged()) {
            return;
        }

        try (var _ = PhaseTimer.timeTask("saveUserData")) {
            String serialized;
            try (var _ = PhaseTimer.timeTask("serializeUserData")) {
                if (options.usingLanguage(lang -> lang instanceof IsMutable mutable && mutable.isModified())) {
                    options.serializedLanguage = options.usingLanguage(JACKSON::valueToTree);
                }
                try {
                    serialized = JACKSON.writeValueAsString(options);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Cannot serialize options", e);
                }
            }

            BotUserData data = new BotUserData();
            data.setUserId(options.userid);
            data.setUserdata(serialized);
            dbm.persist(data, Action.REPLACE);
            options.setChanged(false);
        }
    }
}

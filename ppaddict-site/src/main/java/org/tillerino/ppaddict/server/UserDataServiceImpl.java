package org.tillerino.ppaddict.server;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.ppaddict.rest.AuthenticationService;
import org.tillerino.ppaddict.server.auth.AuthLeaveService;
import org.tillerino.ppaddict.server.auth.AuthLogoutService;
import org.tillerino.ppaddict.server.auth.AuthenticatorService;
import org.tillerino.ppaddict.server.auth.AuthenticatorServices;
import org.tillerino.ppaddict.server.auth.Credentials;
import org.tillerino.ppaddict.server.auth.CredentialsWithOsu;
import org.tillerino.ppaddict.shared.BeatmapRangeRequest;
import org.tillerino.ppaddict.shared.ClientUserData;
import org.tillerino.ppaddict.shared.PpaddictException;
import org.tillerino.ppaddict.shared.Settings;
import tillerino.tillerinobot.data.ApiUser;
import tillerino.tillerinobot.data.PullThrough;
import tillerino.tillerinobot.lang.Default;
import tillerino.tillerinobot.recommendations.RecommendationsManager;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UserDataServiceImpl {
    static final Logger log = LoggerFactory.getLogger(UserDataServiceImpl.class);

    private final PpaddictBackend backend;

    @Setter
    private AuthLogoutService logoutService;

    private final AuthLeaveService leaveService;

    private final @AuthenticatorServices List<AuthenticatorService> authServices;

    private final RecommendationsManager recommendationsManager;

    private final AuthenticationService apiAuthenticationService;

    private final @Named("ppaddict.apiauth.key") String apiAuthKey;

    private final PpaddictUserDataService ppaddictUserDataService;

    private final PullThrough pullThrough;

    public static final String CREDENTIALS_SESSION_KEY = "ppaddict.auth.credentials";
    public static final String CREDENTIALS_COOKIE_KEY = "ppaddict.auth.cookie";

    public static class UserData {
        @Nonnull
        final ClientUserData userData;

        @CheckForNull
        PersistentUserData persistentUserData;

        @Nonnull
        public ClientUserData getClientUserData() {
            return userData;
        }

        public UserData(@Nonnull ClientUserData userData) {
            super();
            this.userData = userData;
        }

        public UserData(@Nonnull ClientUserData userData, @CheckForNull PersistentUserData persistentUserData) {
            super();
            this.userData = userData;
            this.persistentUserData = persistentUserData;
        }
    }

    /**
     * creates a {@link ClientUserData} object and since {@link PersistentUserData} needs to be loaded anyway, we'll
     * return it as well.
     */
    @Nonnull
    public UserData createUserData(HttpServletRequest req, Credentials credentials) throws PpaddictException {
        ClientUserData data = new ClientUserData();

        String referer = req.getHeader("referer");

        if (credentials != null) {
            data.id = credentials.identifier;
            data.nickname = credentials.displayName;
            data.logoutURL = logoutService.getLogoutURL(referer);
            PersistentUserData persistent = getServerUserData(credentials);

            if (persistent.getLinkedOsuId() == null && credentials instanceof CredentialsWithOsu osuCred) {
                // we've logged in with osu! OAuth and can immediately link this account :)
                try {
                    String token = ppaddictUserDataService.getLinkString(osuCred.identifier, osuCred.displayName);
                    ppaddictUserDataService.tryLinkToPpaddict(token, osuCred.getOsuUserId());
                } catch (SQLException e) {
                    throw ExceptionsUtil.getLoggedWrappedException(log, e);
                }

                // reload data right away in case that we already had settings stored in the osu:*** settings.
                persistent = getServerUserData(credentials);
                if (persistent.getLinkedOsuId() == null) {
                    throw new PpaddictException("This is hecked. Please get an adult.");
                }
            }

            Integer linkedId = persistent.getLinkedOsuId();
            data.settings = persistent.getSettings();

            if (linkedId != null) {
                try {
                    ApiUser user = pullThrough.getUser(linkedId, 0);
                    data.nickname = user != null ? user.getUserName() : "(user not found)";
                } catch (SQLException | IOException e) {
                    throw ExceptionsUtil.getLoggedWrappedException(log, e);
                }
                data.isOsuName = true;
            }

            return new UserData(data, persistent);
        } else {
            data.loginElements = new ArrayList<>();
            for (AuthenticatorService authService : authServices) {
                String loginUrl = leaveService.getURL(authService.getIdentifier(), referer);

                data.loginElements.add("<a href=\"" + StringEscapeUtils.escapeHtml4(loginUrl) + "\">"
                        + StringEscapeUtils.escapeHtml4(authService.getDisplayName()) + "</a>");
            }
            return new UserData(data);
        }
    }

    /**
     * Retreives a user's persistend data or creates a new object
     *
     * @return not null, always a copy
     */
    @Nonnull
    public PersistentUserData getServerUserData(@Nonnull Credentials credentials) throws PpaddictException {
        Optional<PersistentUserData> data;
        try {
            data = ppaddictUserDataService.loadUserData(credentials.identifier);
        } catch (Exception e) {
            throw ExceptionsUtil.getLoggedWrappedException(log, e);
        }
        return data.map(/* creates a copy */ PersistentUserData::new).orElseGet(PersistentUserData::new);
    }

    public void saveSettings(Settings s, Credentials credentials) throws PpaddictException {
        PersistentUserData data = getServerUserData(credentials);
        if (s.getRecommendationsParameters() != null) {
            try {
                recommendationsManager.parseSamplerSettings(
                        pullThrough.getUser(data.getLinkedOsuIdOrThrow(), 0),
                        s.getRecommendationsParameters(),
                        new Default());
            } catch (Exception e) {
                throw ExceptionsUtil.getLoggedWrappedException(log, e);
            }
        }
        data.setSettings(s);
        saveUserData(credentials, data);
    }

    public void saveUserData(@Nonnull Credentials credentials, @Nonnull PersistentUserData data)
            throws PpaddictException {
        PersistentUserData saving = new PersistentUserData(data);
        BeatmapRangeRequest lastRequest = saving.getLastRequest();
        if (lastRequest != null) {
            lastRequest.loadedUserRequest = false;
        }
        try {
            ppaddictUserDataService.saveUserData(credentials.identifier, saving);
        } catch (Exception e) {
            throw ExceptionsUtil.getLoggedWrappedException(log, e);
        }
    }

    public void saveComment(
            @CheckForNull HttpServletRequest request,
            int beatmapid,
            @CheckForNull String mods,
            @CheckForNull String comment)
            throws PpaddictException {
        log.info("saveComment called: beatmapid={}, mods={}, comment={}", beatmapid, mods, comment);
        if (comment == null) {
            return;
        }
        long modsAsLong;
        if (mods != null && !mods.isEmpty()) {
            if (mods.equals("?")) {
                modsAsLong = -1L;
            } else {
                modsAsLong = Mods.fromShortNamesContinuous(mods);
            }
        } else {
            modsAsLong = 0;
        }

        comment = comment.trim();

        Credentials credentials = getCredentialsOrThrow(request);

        PersistentUserData userData = getServerUserData(credentials);

        if (userData.getBeatmapComments() == null) {
            userData.setBeatmapComments(new TreeSet<>());
        }

        comment = comment.substring(0, Math.min(ClientUserData.BEATMAP_COMMENT_LENGTH, comment.length()))
                .trim();

        userData.putBeatMapComment(beatmapid, modsAsLong, comment);

        saveUserData(credentials, userData);
    }

    public void rememberCredentials(HttpServletRequest req, HttpServletResponse resp, Credentials credentials)
            throws PpaddictException {
        credentials.expires = System.currentTimeMillis() + 4 * 7L * 24 * 60 * 60 * 1000;

        req.getSession().setAttribute(UserDataServiceImpl.CREDENTIALS_SESSION_KEY, credentials);

        Cookie cookie;
        try {
            cookie = new Cookie(CREDENTIALS_COOKIE_KEY, backend.createCookie(credentials));
        } catch (SQLException e) {
            throw ExceptionsUtil.getLoggedWrappedException(log, e);
        }

        cookie.setPath("/");

        resp.addCookie(cookie);
    }

    @CheckForNull
    public Credentials getCredentials(HttpServletRequest req) throws PpaddictException {
        Credentials credentials = (Credentials) req.getSession().getAttribute(CREDENTIALS_SESSION_KEY);

        if (credentials != null) {
            return credentials;
        }

        Cookie cookie = findMyCookie(req.getCookies());

        if (cookie == null) {
            return null;
        }

        try {
            credentials = backend.resolveCookie(cookie.getValue());
        } catch (SQLException e) {
            throw ExceptionsUtil.getLoggedWrappedException(log, e);
        }

        return credentials;
    }

    @CheckForNull
    Cookie findMyCookie(Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }
        Cookie c = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(CREDENTIALS_COOKIE_KEY)) {
                c = cookie;
            }
        }
        return c;
    }

    public void logout(HttpServletRequest req, HttpServletResponse resp) {
        req.getSession().removeAttribute(CREDENTIALS_SESSION_KEY);

        Cookie cookie = findMyCookie(req.getCookies());
        if (cookie != null) {
            cookie.setMaxAge(0);
            resp.addCookie(cookie);
        }
    }

    public Credentials getCredentialsOrThrow(HttpServletRequest threadLocalRequest) throws PpaddictException {
        Credentials credentials = getCredentials(threadLocalRequest);
        if (credentials == null) {
            throw new PpaddictException.NotLoggedIn();
        }
        return credentials;
    }

    public String createApiKey(Credentials credentialsOrThrow) throws PpaddictException {
        int osuUserId = getServerUserData(credentialsOrThrow).getLinkedOsuIdOrThrow();
        return apiAuthenticationService.createKey(apiAuthKey, osuUserId);
    }
}

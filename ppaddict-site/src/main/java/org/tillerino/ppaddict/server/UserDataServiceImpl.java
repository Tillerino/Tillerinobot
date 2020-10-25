package org.tillerino.ppaddict.server;

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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.client.AbstractBeatmapTable;
import org.tillerino.ppaddict.client.services.UserDataService;
import org.tillerino.ppaddict.rest.AuthenticationService;
import org.tillerino.ppaddict.server.auth.AuthLeaveService;
import org.tillerino.ppaddict.server.auth.AuthLogoutService;
import org.tillerino.ppaddict.server.auth.AuthenticatorService;
import org.tillerino.ppaddict.server.auth.AuthenticatorServices;
import org.tillerino.ppaddict.server.auth.Credentials;
import org.tillerino.ppaddict.server.auth.CredentialsWithOsu;
import org.tillerino.ppaddict.shared.BeatmapBundle;
import org.tillerino.ppaddict.shared.BeatmapRangeRequest;
import org.tillerino.ppaddict.shared.ClientUserData;
import org.tillerino.ppaddict.shared.InitialData;
import org.tillerino.ppaddict.shared.PpaddictException;
import org.tillerino.ppaddict.shared.PpaddictException.NotLoggedIn;
import org.tillerino.ppaddict.shared.Settings;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.lang.Default;
import tillerino.tillerinobot.recommendations.RecommendationsManager;

@Singleton
public class UserDataServiceImpl extends RemoteServiceServlet implements UserDataService {
  static Logger log = LoggerFactory.getLogger(UserDataServiceImpl.class);

  @Inject
  PpaddictBackend backend;

  @Inject
  BotBackend botBackend;

  @Inject
  AuthLogoutService logoutService;

  @Inject
  BeatmapTableServiceImpl beatmapTableService;

  @Inject
  AuthLeaveService leaveService;

  @Inject
  @AuthenticatorServices
  List<AuthenticatorService> authServices;

  @Inject
  RecommendationsManager recommendationsManager;

  @Inject
  AuthenticationService apiAuthenticationService;

  @Inject
  @Named("ppaddict.apiauth.key")
  String apiAuthKey;

  @Inject
  PpaddictUserDataService ppaddictUserDataService;

  public static final String CREDENTIALS_SESSION_KEY = "ppaddict.auth.credentials";
  public static final String CREDENTIALS_COOKIE_KEY = "ppaddict.auth.cookie";

  private static final long serialVersionUID = 1L;

  @Override
  public ClientUserData getStatus() throws PpaddictException {
    HttpServletRequest req = getThreadLocalRequest();

    Credentials credentials = getCredentials(getThreadLocalRequest());

    UserData data = createUserData(req, credentials);

    return data.userData;
  }

  @Override
  public InitialData getInitialData(BeatmapRangeRequest request) throws PpaddictException {
    Credentials credentials = getCredentials(getThreadLocalRequest());

    UserData userDataWithPersistentUserData = createUserData(getThreadLocalRequest(), credentials);

    ClientUserData userData = userDataWithPersistentUserData.userData;

    if (request == null) {
      request = userDataWithPersistentUserData.persistentUserData != null
          ? userDataWithPersistentUserData.persistentUserData.getLastRequest() : null;
    }
    if (request == null) {
      request = new BeatmapRangeRequest();
    }
    if (request.start > 0) {
      request.start = 0;
      request.length = AbstractBeatmapTable.PAGE_SIZE;
    }

    BeatmapBundle beatmapBundle = beatmapTableService.executeGetRange(request, credentials,
        userDataWithPersistentUserData.persistentUserData);

    return new InitialData(userData, beatmapBundle, request);
  }

  public static class UserData {
    @Nonnull
    ClientUserData userData;
    @CheckForNull
    PersistentUserData persistentUserData;

    @Nonnull
    public PersistentUserData getServerUserDataOrThrow() throws NotLoggedIn {
      if (persistentUserData == null) {
        throw new NotLoggedIn();
      }
      return persistentUserData;
    }

    public UserData(@Nonnull ClientUserData userData) {
      super();
      this.userData = userData;
    }

    public UserData(@Nonnull ClientUserData userData,
        @CheckForNull PersistentUserData persistentUserData) {
      super();
      this.userData = userData;
      this.persistentUserData = persistentUserData;
    }
  }

  /**
   * creates a {@link ClientUserData} object and since {@link PersistentUserData} needs to be loaded
   * anyway, we'll return it as well.
   * 
   * @param req
   * @param credentials
   * @return
   * @throws PpaddictException
   */
  @Nonnull
  public UserData createUserData(HttpServletRequest req, Credentials credentials)
      throws PpaddictException {
    ClientUserData data = new ClientUserData();

    String referer = req.getHeader("referer");

    if (credentials != null) {
      data.id = credentials.identifier;
      data.nickname = credentials.displayName;
      data.logoutURL = logoutService.getLogoutURL(referer);
      PersistentUserData persistent = getServerUserData(credentials);

      if(persistent.getLinkedOsuId() == null && credentials instanceof CredentialsWithOsu) {
        // we've logged in with osu! OAuth and can immediately link this account :)
        CredentialsWithOsu osuCred = (CredentialsWithOsu) credentials;
        String token = ppaddictUserDataService.getLinkString(osuCred.identifier, osuCred.displayName);
        ppaddictUserDataService.tryLinkToPpaddict(token, osuCred.osuUserId);

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
          OsuApiUser user = botBackend.getUser(linkedId, 0);
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

        data.loginElements
            .add(new SafeHtmlBuilder().appendHtmlConstant("<a href=\"" + loginUrl + "\">")
                .appendEscaped(authService.getDisplayName()).appendHtmlConstant("</a>").toSafeHtml().asString());

      }
      return new UserData(data);
    }
  }

  /**
   * Retreives a user's persistend data or creates a new object
   * 
   * @param credentials
   * @return not null, always a copy
   * @throws PpaddictException
   */
  @Nonnull
  public PersistentUserData getServerUserData(@Nonnull Credentials credentials)
      throws PpaddictException {
    Optional<PersistentUserData> data;
    try {
      data = ppaddictUserDataService.loadUserData(credentials.identifier);
    } catch (Exception e) {
      throw ExceptionsUtil.getLoggedWrappedException(log, e);
    }
    return data.map(/* creates a copy */ PersistentUserData::new)
        .orElseGet(PersistentUserData::new);
  }

  @Override
  public void saveSettings(@Nonnull Settings s) throws PpaddictException {
    Credentials credentials = getCredentialsOrThrow();
    PersistentUserData data = getServerUserData(credentials);
    if (s.getRecommendationsParameters() != null) {
      try {
        recommendationsManager.parseSamplerSettings(
            botBackend.getUser(data.getLinkedOsuIdOrThrow(), 0), s.getRecommendationsParameters(),
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

  @Override
  public void saveComment(int beatmapid, @CheckForNull String mods, @CheckForNull String comment)
      throws PpaddictException {
    if (comment == null) {
      return;
    }
    long modsAsLong;
    if (mods != null) {
      if (mods.equals("?")) {
        modsAsLong = -1l;
      } else {
        modsAsLong = Mods.fromShortNamesContinuous(mods);
      }
    } else {
      modsAsLong = 0;
    }

    comment = comment.trim();

    Credentials credentials = getCredentialsOrThrow();

    PersistentUserData userData = getServerUserData(credentials);

    if (userData.getBeatmapComments() == null) {
      userData.setBeatmapComments(new TreeSet<String>());
    }

    comment = comment
        .substring(0, Math.min(ClientUserData.BEATMAP_COMMENT_LENGTH, comment.length())).trim();

    userData.putBeatMapComment(beatmapid, modsAsLong, comment);

    saveUserData(credentials, userData);
  }

  public void rememberCredentials(HttpServletRequest req, HttpServletResponse resp,
      Credentials credentials) throws PpaddictException {
    credentials.expires = System.currentTimeMillis() + 4 * 7l * 24 * 60 * 60 * 1000;

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
    for (int i = 0; i < cookies.length; i++) {
      if (cookies[i].getName().equals(CREDENTIALS_COOKIE_KEY)) {
        c = cookies[i];
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

  @Override
  public String getLinkString() throws PpaddictException {
    Credentials credentials = getCredentialsOrThrow();
    try {
      return ppaddictUserDataService.getLinkString(credentials.identifier, credentials.displayName);
    } catch (Exception e) {
      throw ExceptionsUtil.getLoggedWrappedException(log, e);
    }
  }

  private @Nonnull Credentials getCredentialsOrThrow() throws PpaddictException {
    return getCredentialsOrThrow(getThreadLocalRequest());
  }

  public Credentials getCredentialsOrThrow(HttpServletRequest threadLocalRequest)
      throws PpaddictException {
    Credentials credentials = getCredentials(threadLocalRequest);
    if (credentials == null) {
      throw new PpaddictException.NotLoggedIn();
    }
    return credentials;
  }

  @SuppressFBWarnings(value = "NP", justification = "checked")
  public @UserId int getUserIdOrThrow(HttpServletRequest threadLocalRequest)
      throws PpaddictException {
    PersistentUserData persistentUserData = getLinkedDataOrThrow(threadLocalRequest);
    return persistentUserData.getLinkedOsuId();
  }

  public @Nonnull PersistentUserData getLinkedDataOrThrow(HttpServletRequest threadLocalRequest)
      throws PpaddictException {
    Credentials credentials = getCredentialsOrThrow(threadLocalRequest);
    PersistentUserData persistentUserData = getServerUserData(credentials);
    if (persistentUserData.getLinkedOsuId() == null) {
      throw new PpaddictException.NotLinked();
    }
    return persistentUserData;
  }

  @Override
  public String createApiKey() throws PpaddictException {
    int osuUserId = getServerUserData(getCredentialsOrThrow()).getLinkedOsuIdOrThrow();
    return apiAuthenticationService.createKey(apiAuthKey, osuUserId);
  }
}

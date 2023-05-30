package org.tillerino.ppaddict.server;

import java.sql.SQLException;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.tillerino.mormon.DatabaseManager;
import org.tillerino.mormon.Persister.Action;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.web.data.PpaddictLinkKey;
import org.tillerino.ppaddict.web.data.PpaddictUser;
import org.tillerino.ppaddict.web.types.PpaddictId;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import tillerino.tillerinobot.handlers.LinkPpaddictHandler;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PpaddictUserDataService {
  private final DatabaseManager dbm;

  private final Clock clock;

  private final ObjectMapper objectMapper = new ObjectMapper();
  {
    objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
        .withFieldVisibility(Visibility.ANY).withGetterVisibility(Visibility.NONE)
        .withSetterVisibility(Visibility.NONE).withCreatorVisibility(Visibility.NONE));
  }

  public Optional<PersistentUserData> loadUserData(@PpaddictId String identifier) {
    Optional<PpaddictUser> userMaybe;
    try {
      userMaybe = dbm.loadUnique(PpaddictUser.class, identifier);
    } catch (SQLException e) {
      throw new RuntimeException("Error loading user", e);
    }
    return userMaybe.flatMap(user -> {
      if (user.getForward() != null) {
        return loadUserData(user.getForward());
      }
      return Optional.ofNullable(user.getData()).map(t -> {
        try {
          return objectMapper.readValue(t, PersistentUserData.class);
        } catch (JsonProcessingException e) {
          throw new RuntimeException("Error deserializing JSON", e);
        }
      });
    });
  }

  public void saveUserData(@PpaddictId String identifier, PersistentUserData userData) throws SQLException {
    PpaddictUser row = dbm.loadUnique(PpaddictUser.class, identifier).orElse(new PpaddictUser(identifier, null, null));
    if (row.getForward() != null) {
      saveUserData(row.getForward(), userData);
    } else {
      String serialized;
      try {
        serialized = objectMapper.writeValueAsString(userData);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Error serializing JSON", e);
      }
      row.setData(serialized);
      dbm.persist(row, Action.REPLACE);
    }
  }

  public String getLinkString(@PpaddictId String id, String displayName) throws SQLException {
    PpaddictLinkKey key = new PpaddictLinkKey(id, displayName, LinkPpaddictHandler.newKey(),
        clock.currentTimeMillis() + 60 * 1000L);

    dbm.persist(key, Action.INSERT);

    return key.getLinkKey();
  }

  /**
   * links the given user to a ppaddict account using a token string.
   * 
   * @param token a token that was given to the user by the ppaddict website.
   * @param osuUserId
   * @return the name of the ppaddict account that current user was linked to, or empty if the token
   *         was not valid
   * @throws SQLException
   */
  public Optional<String> tryLinkToPpaddict(String token, @UserId int osuUserId)
      throws SQLException {
    Optional<PpaddictLinkKey> validLink = dbm.loadUnique(PpaddictLinkKey.class, token)
        .filter(l -> l.getExpires() > clock.currentTimeMillis())
        .filter(link -> !link.getIdentifier().startsWith("osu:")
        // don't chain links
        );
    if (!validLink.isPresent()) {
      return Optional.empty();
    }
    PpaddictLinkKey link = validLink.get();
    PpaddictUser authenticatedUser = dbm.loadUnique(PpaddictUser.class, link.getIdentifier())
        .orElseGet(() -> new PpaddictUser(link.getIdentifier(), null, null));
    if (authenticatedUser.getForward() != null) {
      // don't change existing forwards
      return Optional.empty();
    }

    String osuIdentifier = createPpaddictIdentifierForOsuId(osuUserId);

    {
      /*
       * copy old data to new place (if there were any and we're not overwriting anything) and set
       * osu id. note that the user data in the old place remains unchanged as a backup.
       */
      PersistentUserData osuData = loadUserData(osuIdentifier)
          .orElseGet(() -> loadUserData(link.getIdentifier()).orElseGet(PersistentUserData::new));
      osuData.setLinkedOsuId(osuUserId);
      saveUserData(osuIdentifier, osuData);
    }

    authenticatedUser.setForward(osuIdentifier);
    dbm.persist(authenticatedUser, Action.REPLACE);

    dbm.delete(link);
    return Optional.of(link.getDisplayName());
  }

  @SuppressFBWarnings(value = "TQ", justification = "source")
  public static @PpaddictId String createPpaddictIdentifierForOsuId(@UserId int osuUserId) {
    return "osu:" + osuUserId;
  }
}

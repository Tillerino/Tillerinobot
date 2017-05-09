package org.tillerino.ppaddict.shared;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.tillerino.ppaddict.shared.types.PpaddictId;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * An instance of this class is always available, even if the user is not logged in. It is used to
 * carry any information about the user to the frontend. It should be seen as a combination of
 * 
 * @author Tillerino
 */
public class ClientUserData implements IsSerializable {
  /**
   * null if not logged in. will change from openid to osu! name
   */
  @CheckForNull
  public String nickname;

  public boolean isOsuName = false;

  @CheckForNull
  public @PpaddictId String id;

  /**
   * null if not logged in.
   */
  @CheckForNull
  public String logoutURL;

  /**
   * null if logged in.
   */
  @CheckForNull
  public List<SafeHtml> loginElements;

  /**
   * null if not logged in.
   */
  @Nonnull
  public Settings settings = new Settings();

  public static final int BEATMAP_COMMENT_LENGTH = 64;

  public boolean isLoggedIn() {
    return nickname != null;
  }
}

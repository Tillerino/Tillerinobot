package org.tillerino.ppaddict.server.auth;

import java.io.Serializable;

import org.tillerino.ppaddict.shared.types.PpaddictId;

public class Credentials implements Serializable {
  private static final long serialVersionUID = 1L;

  @PpaddictId
  public String identifier;
  public String displayName;

  public long expires = System.currentTimeMillis() + 28l * 24 * 60 * 60 * 1000;

  public Credentials(@PpaddictId String identifier, String displayName) {
    super();
    this.identifier = identifier;
    this.displayName = displayName;
  }

  public Credentials(Credentials o) {
    this.identifier = o.identifier;
    this.displayName = o.displayName;
    this.expires = o.expires;
  }

  protected Credentials() {

  }
}

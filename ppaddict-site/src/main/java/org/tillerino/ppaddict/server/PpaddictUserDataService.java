package org.tillerino.ppaddict.server;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.web.AbstractPpaddictUserDataService;
import org.tillerino.ppaddict.web.data.repos.PpaddictLinkKeyRepository;
import org.tillerino.ppaddict.web.data.repos.PpaddictUserRepository;

@Singleton
public class PpaddictUserDataService extends AbstractPpaddictUserDataService<PersistentUserData> {
  @Inject
  public PpaddictUserDataService(PpaddictUserRepository users, PpaddictLinkKeyRepository linkKeys,
      Clock clock) {
    super(users, linkKeys, clock);
  }
}

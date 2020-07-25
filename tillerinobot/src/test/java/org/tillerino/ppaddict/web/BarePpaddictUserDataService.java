package org.tillerino.ppaddict.web;

import javax.inject.Inject;

import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.web.data.repos.PpaddictLinkKeyRepository;
import org.tillerino.ppaddict.web.data.repos.PpaddictUserRepository;

public class BarePpaddictUserDataService extends AbstractPpaddictUserDataService<ExamplePpaddictUserData> {
	@Inject
	public BarePpaddictUserDataService(PpaddictUserRepository users, PpaddictLinkKeyRepository linkKeys, Clock clock) {
		super(users, linkKeys, clock);
	}
}

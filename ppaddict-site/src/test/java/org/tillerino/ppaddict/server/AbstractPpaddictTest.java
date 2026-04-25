package org.tillerino.ppaddict.server;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.tillerino.ppaddict.PpaddictModule;
import tillerino.tillerinobot.TestBase;

public class AbstractPpaddictTest extends TestBase {
    @Inject
    PpaddictUserDataService userDataService;

    @Inject
    PpaddictCredentials.Repo credentialsRepo;

    @Inject
    PpaddictBackendImpl ppaddictBackend;

    @dagger.Component(modules = {TestBase.Module.class, PpaddictModule.class})
    @Singleton
    interface Injector {
        void inject(AbstractPpaddictTest t);
    }

    {
        DaggerAbstractPpaddictTest_Injector.create().inject(this);
    }
}

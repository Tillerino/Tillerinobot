package org.tillerino.ppaddict;

import dagger.Component;
import dagger.Module;
import io.undertow.Undertow;
import java.sql.SQLException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.tillerino.mormon.DatabaseManager;
import org.tillerino.ppaddict.auth.FakeAuthenticatorService;
import org.tillerino.ppaddict.auth.FakeAuthenticatorWebsite;
import org.tillerino.ppaddict.server.PpaddictBackend;
import org.tillerino.ppaddict.server.PpaddictUserDataService;
import org.tillerino.ppaddict.server.auth.AuthArriveService;
import org.tillerino.ppaddict.server.auth.implementations.OsuOauth;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.util.TestClock;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.LocalConsoleTillerinobot;
import tillerino.tillerinobot.MysqlContainer;

/** Starts ppaddict locally on port 8080 with a fake backend. */
public class LocalPpaddict {
    @Inject
    PpaddictContextConfigurator configurator;

    public static void main(String[] args) throws Exception {
        LocalPpaddict localPpaddict = new LocalPpaddict();
        DaggerLocalPpaddict_Injector.create().inject(localPpaddict);

        MysqlContainer.MysqlDatabaseLifecycle.createSchema();
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(PpaddictModule.createFilterPathHandler(deploymentInfo -> {
                    localPpaddict.configurator.configureUndertow(deploymentInfo);
                    PpaddictContextConfigurator.addServlet(deploymentInfo, "/showErrorPage", new ProducesError());
                    PpaddictContextConfigurator.addServlet(
                            deploymentInfo, FakeAuthenticatorWebsite.PATH, new FakeAuthenticatorWebsite());
                }))
                .build();
        server.start();
    }

    @Component(modules = Module.class)
    @Singleton
    interface Injector {
        void inject(LocalPpaddict t);
    }

    @dagger.Module(
            includes = {
                LocalConsoleTillerinobot.Module.class,
                TestClock.Module.class,
                FakeAuthenticatorService.Module.class
            })
    interface Module {
        @dagger.Provides
        static @Named("ppaddict.auth.returnURL") String returnUrl() {
            return "http://localhost:8080" + AuthArriveService.PATH;
        }

        @dagger.Provides
        static @Named("ppaddict.apiauth.key") String authKey() {
            return "ppaddict-app-key";
        }

        @dagger.Binds
        PpaddictBackend ppaddictBackend(TestBackend testBackend);

        @dagger.Provides
        @Singleton
        static PpaddictUserDataService getPpaddictUserDataService(
                DatabaseManager linkKeys, Clock clock, BotBackend botBackend) {
            final String osuOAuthPrefix = OsuOauth.OSU_AUTH_SERVICE_IDENTIFIER + ":";

            return new PpaddictUserDataService(linkKeys, clock) {
                @Override
                public String getLinkString(String id, String displayName) throws SQLException {
                    if (id.startsWith(osuOAuthPrefix)) {
                        int osuId = Integer.parseInt(id.substring(osuOAuthPrefix.length()));
                        ((tillerino.tillerinobot.TestBackend) botBackend)
                                .hintUser(displayName, false, 100000, 1000, osuId);
                    }
                    return super.getLinkString(id, displayName);
                }
            };
        }
    }
}

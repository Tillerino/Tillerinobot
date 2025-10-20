package org.tillerino.ppaddict;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.daggeradapter.DaggerAdapter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import io.undertow.Undertow;
import java.sql.SQLException;
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
    public static void main(String[] args) throws Exception {
        MysqlContainer.MysqlDatabaseLifecycle.createSchema();
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(PpaddictModule.createGuiceFilterPathHandler(
                        new GuiceServletContextListener() {
                            @Override
                            protected Injector getInjector() {
                                return Guice.createInjector(new ServletModule() {
                                    @Override
                                    protected void configureServlets() {
                                        serve(FakeAuthenticatorWebsite.PATH).with(FakeAuthenticatorWebsite.class);
                                        serve("/showErrorPage").with(ProducesError.class);
                                        install(new PpaddictModule());

                                        install(DaggerAdapter.from(Module.class));
                                    }
                                });
                            }
                        }.getClass()))
                .build();
        server.start();
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

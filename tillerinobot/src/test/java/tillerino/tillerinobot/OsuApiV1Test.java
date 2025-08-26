package tillerino.tillerinobot;

import java.io.IOException;
import java.net.URI;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.tillerino.MockServerRule;

import dagger.Component;
import dagger.Provides;
import lombok.SneakyThrows;
import tillerino.tillerinobot.data.ApiBeatmap;
import tillerino.tillerinobot.data.ApiUser;

public class OsuApiV1Test {
  public static final String OSUAPI_V1_MOCK_KEY = "1234567890123456789012345678901234567890";

  @Component(modules = Module.class)
  @Singleton
  interface Injector {
    void inject(OsuApiV1Test t);
  }
  @dagger.Module
  public interface Module {
    @Provides
    @SneakyThrows
    static OsuApiV1 osuApiV1() {
      return Mockito.spy(new OsuApiV1(URI.create(MockServerRule.getExternalMockServerAddress() + "/api/").toURL(), OSUAPI_V1_MOCK_KEY, RateLimiter.unlimited()));
    }
  }
  {
    DaggerOsuApiV1Test_Injector.create().inject(this);
  }

  @Inject
  OsuApiV1 osuApiV1;

  @RegisterExtension
  public final MockServerRule mockServer = new MockServerRule();

  @Test
  void testGetUserFromName() throws Exception {
    Assertions.assertThat(osuApiV1.getUser("Tillerino", 0)).returns(2070907, ApiUser::getUserId);
  }

  @Test
  void testGetUserFromId() throws Exception {
    Assertions.assertThat(osuApiV1.getUser(2070907, 0)).returns("Tillerino", ApiUser::getUserName);
  }

  @Test
  void testGetUserTop() throws Exception {
    Assertions.assertThat(osuApiV1.getUserTop(2070907, 0, 50)).hasSize(50);
  }

  @Test
  void testGetUserRecent() throws Exception {
    Assertions.assertThat(osuApiV1.getUserRecent(2070907, 0)).isNotNull();
  }

  @Test
  void testGetBeatmap() throws Exception {
    Assertions.assertThat(osuApiV1.getBeatmap(131891, 0)).returns("The Quick Brown Fox", ApiBeatmap::getArtist);
  }
}
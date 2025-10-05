package tillerino.tillerinobot.diff;

import dagger.Component;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import tillerino.tillerinobot.OsuApiV2;
import tillerino.tillerinobot.OsuApiV2Test;
import tillerino.tillerinobot.data.ApiBeatmap;
import tillerino.tillerinobot.data.ApiScore;

class OsuScoreTest {
  @Component(modules = OsuApiV2Test.Module.class)
  @Singleton
  interface Injector {
    void inject(OsuScoreTest t);
  }

  {
    DaggerOsuScoreTest_Injector.create().inject(this);
  }

  @Inject
  OsuApiV2 api;

  @Test
  void testBigBlackNomodV2() throws Exception {
    ApiScore score = api.getBeatmapTop(131891, 0).get(43);
    OsuScore osuScore = new OsuScore(score, false);
    Assertions.assertThat((double) osuScore.getPP(getBigBlackNomod())).isEqualTo(score.getPp(), Offset.offset(1E-2));
  }

  @Test
  void testBigBlackHiddenV1() throws Exception {
    ApiScore score = api.getBeatmapTop(131891, 0).get(4);
    OsuScore osuScore = new OsuScore(score, true);
    Assertions.assertThat((double) osuScore.getPP(getBigBlackNomod())).isEqualTo(score.getPp(), Offset.offset(1E-2));
  }

  @Test
  void testBigBlackHdhrV1() throws Exception {
    ApiScore score = api.getBeatmapTop(131891, 0).get(0);
    OsuScore osuScore = new OsuScore(score, true);
    Assertions.assertThat((double) osuScore.getPP(getBigBlackHardrock())).isEqualTo(score.getPp(), Offset.offset(1E-2));
  }

  private BeatmapImpl getBigBlackNomod() throws IOException {
    ApiBeatmap apiBeatmap = api.getBeatmap(131891, 0L);
    //     "body" : "{\"attributes\":{\"star_rating\":6.860909938812256,
    //     \"max_combo\":1337,
    //     \"aim_difficulty\":3.56781005859375,
    //     \"aim_difficult_slider_count\":200.3780059814453,
    //     \"speed_difficulty\":2.901710033416748,
    //     \"speed_note_count\":363.6050109863281,
    //     \"slider_factor\":0.9920060038566589,
    //     \"aim_difficult_strain_count\":118.95600128173828,
    //     \"speed_difficult_strain_count\":142.7740020751953}}",
    return new BeatmapImpl(
        0L,
        0,
        3.56781005859375f,
        2.901710033416748f,
        (float) apiBeatmap.getOverallDifficulty(),
        (float) apiBeatmap.getApproachRate(),
        apiBeatmap.getMaxCombo(),
        0.9920060038566589f,
        (float) 0,
        363.6050109863281f,
        410,
        2,
        334,
        118.95600128173828f,
        142.7740020751953f
    );
  }

  private BeatmapImpl getBigBlackHardrock() throws IOException {
    ApiBeatmap apiBeatmap = api.getBeatmap(131891, 16L);
    //     "body" : "{\"attributes\":{
    //     \"star_rating\":7.370090007781982,
    //     \"max_combo\":1337,
    //     \"aim_difficulty\":3.8672399520874023,
    //     \"aim_difficult_slider_count\":206.76300048828125,
    //     \"speed_difficulty\":3.0591800212860107,
    //     \"speed_note_count\":282.6159973144531,
    //     \"slider_factor\":0.9905149936676025,
    //     \"aim_difficult_strain_count\":123.26200103759766,
    //     \"speed_difficult_strain_count\":116.10900115966797}}",
    return new BeatmapImpl(
        16L,
        0,
        3.8672399520874023f,
        3.0591800212860107f,
        (float) apiBeatmap.getOverallDifficulty(),
        (float) apiBeatmap.getApproachRate(),
        apiBeatmap.getMaxCombo(),
        0.9905149936676025f,
        (float) 0,
        282.6159973144531f,
        410,
        2,
        334,
        123.26200103759766f,
        116.10900115966797f
    );
  }
}

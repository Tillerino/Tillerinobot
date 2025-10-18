package tillerino.tillerinobot.diff;

import dagger.Component;
import java.io.IOException;
import java.util.NoSuchElementException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import tillerino.tillerinobot.OsuApiV2;
import tillerino.tillerinobot.OsuApiV2Test;
import tillerino.tillerinobot.data.ApiBeatmap;
import tillerino.tillerinobot.data.ApiScore;

@ExtendWith(SoftAssertionsExtension.class)
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
  void testBigBlack(SoftAssertions softly) throws IOException {
    for (ApiScore score : api.getBeatmapTop(131891, 0)) {
      if (score.getPp() == null) {
        continue;
      }

      boolean v2 = Mods.V2.is(score.getMods());
      OsuPerformanceAttributes attrs = new OsuPerformanceCalculator().CreatePerformanceAttributes(score, getBeatmap(score), !v2);
      softly.assertThat(attrs.total())
          .as("%s", Mods.toShortNamesContinuous(Mods.getMods(score.getMods())))
          // for v2 we are not very precise yet because of slider ends
          .isEqualTo(score.getPp(), Offset.offset(v2 ? .5 : 0.0007));
    }
  }

  @Test
  void testSongsCompilationIV(SoftAssertions softly) throws IOException {
    for (ApiScore score : api.getBeatmapTop(5047712, 0)) {
      if (score.getPp() == null) {
        continue;
      }

      boolean v2 = Mods.V2.is(score.getMods());
      OsuPerformanceAttributes attrs = new OsuPerformanceCalculator().CreatePerformanceAttributes(score, getBeatmap(score), !v2);
      softly.assertThat(attrs.total())
          .as("Mods: %s", Mods.toShortNamesContinuous(Mods.getMods(score.getMods())))
          // for v2 we are not very precise yet because of slider ends
          .isEqualTo(score.getPp(), Offset.offset(v2 ? .5 : 0.0007));
      System.out.println(attrs.total());
    }
  }

  private BeatmapImpl getBeatmap(ApiScore score) throws IOException {
    int id = score.getBeatmapId();
    long mods = Beatmap.getDiffMods(score.getMods());

    return switch (id) {
      case 131891 -> switch ((int) mods) {
        case 0 -> getBigBlackNomod();
        case 16 -> getBigBlackHardrock();
        default -> throw new NoSuchElementException("" + mods);
      };
      case 5047712 -> switch ((int) mods) {
        case 0 -> getSongCompilationIVNomod();
        case 16 -> getSongCompilationIVHardrock();
        case 64 -> getSongCompilationIVDt();
        default -> throw new NoSuchElementException("" + mods);
      };
      default -> throw new NoSuchElementException("" + id);
    };
  }

  private BeatmapImpl getBigBlackNomod() throws IOException {
    long mods = 0L;
    ApiBeatmap apiBeatmap = api.getBeatmap(131891, mods);
    return new BeatmapImpl(
        mods,
        (float) OsuApiBeatmap.calcOd(apiBeatmap.getOverallDifficulty(), mods),
        (float) OsuApiBeatmap.calcAR(apiBeatmap.getApproachRate(), mods),
        410,
        334,
        2,
        0,
        apiBeatmap.getMaxCombo(),
        3.56781005859375f,
        200.3780059814453f,
        2.901710033416748f,
        363.6050109863281f,
        0.9920060038566589f,
        118.95600128173828f,
        142.7740020751953f,
        (float) 0
    );
  }

  private BeatmapImpl getBigBlackHardrock() throws IOException {
    long mods = 16L;
    ApiBeatmap apiBeatmap = api.getBeatmap(131891, mods);
    return new BeatmapImpl(
        mods,
        (float) OsuApiBeatmap.calcOd(apiBeatmap.getOverallDifficulty(), mods),
        (float) OsuApiBeatmap.calcAR(apiBeatmap.getApproachRate(), mods),
        410,
        334,
        2,
        0,
        apiBeatmap.getMaxCombo(),
        3.8672399520874023f,
        206.76300048828125f,
        3.0591800212860107f,
        282.6159973144531f,
        0.9905149936676025f,
        123.26200103759766f,
        116.10900115966797f,
        (float) 0
    );
  }

  private BeatmapImpl getSongCompilationIVNomod() throws IOException {
    long mods = 0L;
    ApiBeatmap apiBeatmap = api.getBeatmap(5047712, mods);
    return new BeatmapImpl(
        mods,
        (float) OsuApiBeatmap.calcOd(apiBeatmap.getOverallDifficulty(), mods),
        (float) OsuApiBeatmap.calcAR(apiBeatmap.getApproachRate(), mods),
        1121,
        860,
        4,
        0,
        apiBeatmap.getMaxCombo(),
        3.977260112762451f,
        323.0539855957031f,
        2.68107008934021f,
        998.8770141601562f,
        0.9961370229721069f,
        185.45599365234375f,
        245.968994140625f,
        (float) 0
    );
  }

  private BeatmapImpl getSongCompilationIVHardrock() throws IOException {
    long mods = 16L;
    ApiBeatmap apiBeatmap = api.getBeatmap(5047712, mods);
    return new BeatmapImpl(
        mods,
        (float) OsuApiBeatmap.calcOd(apiBeatmap.getOverallDifficulty(), mods),
        (float) OsuApiBeatmap.calcAR(apiBeatmap.getApproachRate(), mods),
        1121,
        860,
        4,
        0,
        apiBeatmap.getMaxCombo(),
        4.264699935913086f,
        341.4339904785156f,
        2.6813700199127197f,
        1008.1400146484375f,
        0.993461012840271f,
        193.65199279785156f,
        248.65699768066406f,
        (float) 0
    );
  }

  private BeatmapImpl getSongCompilationIVDt() throws IOException {
    long mods = 64L;
    ApiBeatmap apiBeatmap = api.getBeatmap(5047712, mods);
    return new BeatmapImpl(
        mods,
        (float) OsuApiBeatmap.calcOd(apiBeatmap.getOverallDifficulty(), mods),
        (float) OsuApiBeatmap.calcAR(apiBeatmap.getApproachRate(), mods),
        1121,
        860,
        4,
        0,
        apiBeatmap.getMaxCombo(),
        6.0218000411987305f,
        313.4620056152344f,
        3.7941699028015137f,
        1054.469970703125f,
        0.9939389824867249f,
        167.5780029296875f,
        314.1919860839844f,
        (float) 0
    );}
}

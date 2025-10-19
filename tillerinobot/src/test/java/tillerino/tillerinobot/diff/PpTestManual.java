package tillerino.tillerinobot.diff;

import com.github.omkelderman.sandoku.DiffResult;
import com.github.omkelderman.sandoku.ProcessorApi;
import com.github.omkelderman.sandoku.ScoreInfo;
import dagger.Component;
import jakarta.ws.rs.BadRequestException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiScore;
import org.tillerino.ppaddict.mockmodules.BeatmapDownloaderMockModule;
import tillerino.tillerinobot.OsuApiV2;
import tillerino.tillerinobot.OsuApiV2Test;
import tillerino.tillerinobot.data.ApiScore;
import tillerino.tillerinobot.data.DiffEstimate.DiffEstimateToBeatmapImplMapper;
import tillerino.tillerinobot.diff.sandoku.SanDoku;
import tillerino.tillerinobot.rest.AbstractBeatmapResource.BeatmapDownloader;

@ExtendWith(SoftAssertionsExtension.class)
public class PpTestManual {
  @Component(modules = {OsuApiV2Test.Module.class, BeatmapDownloaderMockModule.class})
  @Singleton
  interface Injector {
    void inject(PpTestManual t);
  }

  {
    DaggerPpTestManual_Injector.create().inject(this);
  }

  @Inject OsuApiV2 api;
  ProcessorApi sanDoku = SanDoku.defaultClient(URI.create("http://localhost:8080"));

  @Inject BeatmapDownloader beatmapDownloader;

  @Test
  void testBigBlack(SoftAssertions softly) throws Exception {
    // https://osu.ppy.sh/beatmapsets/41823#osu/131891
    testBeatmapTop(softly, 131891, 0, 3, 0.5);
  }

  @Test
  void testSongCompilationIV(SoftAssertions softly) throws Exception {
    // https://osu.ppy.sh/beatmapsets/2347113#osu/5047712
    testBeatmapTop(softly, 5047712, 0, 2.5, 2.5);
  }

  @Test
  void testFreedomDive(SoftAssertions softly) throws Exception {
    // https://osu.ppy.sh/beatmapsets/39804/#osu/129891
    testBeatmapTop(softly, 129891, 0, 0.002, 0.6);
  }

  @Test
  void testFlashlight(SoftAssertions softly) throws Exception {
    // https://osu.ppy.sh/beatmapsets/274035#osu/663567
    testBeatmapTop(softly, 663567, 0, 0.7, 0.6);
  }

  @Test
  void testBestFriends(SoftAssertions softly) throws Exception {
    // https://osu.ppy.sh/beatmapsets/130104#osu/328472
    testBeatmapTop(softly, 328472, 0, 1.2, 1.5);
  }

  private void testBeatmapTop(SoftAssertions softly, int beatmapid, int userFilter,
      double stableTolerance,
      double lazerTolerance) throws Exception {
    byte[] beatmapBytes = beatmapDownloader.getActualBeatmap(beatmapid).getBytes(StandardCharsets.UTF_8);

    for (ApiScore score : api.getBeatmapTop(beatmapid, 0)) {
      if (userFilter != 0 && score.getUserId() != userFilter) {
        continue;
      }

      if (score.getPp() == null) {
        System.out.println(); // empty line so we can copy-pasta this next to a score dump
        continue;
      }

      long mods = Mods.fixNC(score.getMods());
      String modsShort = "%s (%s)".formatted(
          Mods.toShortNamesContinuous(Mods.getMods(mods & ~Mods.getMask(Mods.Lazer))),
          Mods.Lazer.is(mods) ? "Lazer" : "Stable");
      DiffResult diff = sanDoku.processorCalcDiff(0, (int) Beatmap.getDiffMods(mods), false, beatmapBytes);
      BeatmapImpl beatmap = DiffEstimateToBeatmapImplMapper.INSTANCE.toBeatmap(diff);

      AtomicReference<Double> sanDokuPp = new AtomicReference<>();
      Thread thread = new Thread(() -> {
        sanDokuPp.set(fetchSanDokuPp(score, (int) mods, diff.getBeatmapMd5(), beatmapBytes));
      });
      thread.start();

      double ourPp = new OsuPerformanceCalculator().CreatePerformanceAttributes(score, beatmap).total();
      thread.join();

      softly.assertThat(ourPp)
          .as("%s", modsShort)
          // for lazer we are not very precise yet because of slider ends
          .isEqualTo(score.getPp(), Offset.offset(Mods.Lazer.is(mods) ? lazerTolerance : stableTolerance));

      if (!Mods.Lazer.is(mods)) {
        // san-doku doesn't support lazer pp calc yet, but for the rest, we should match exactly.
        softly.assertThat(ourPp).isCloseTo(sanDokuPp.get(), Offset.offset(0.01));
      }
      System.out.printf("%s\t%s\t%s%n", modsShort, ourPp, sanDokuPp.get());
    }
  }

  private double fetchSanDokuPp(ApiScore score, int mods, String beatmapMd5, byte[] beatmapBytes) {
    mods = mods & ~(int) Mods.getMask(Mods.Perfect); // Perfect trips up SanDoku
    try {
      // cache with exact mods for immediate pp query
      sanDoku.processorCalcDiff(0, mods, true, beatmapBytes);
      return sanDoku.processorCalcPp(beatmapMd5, 0, mods, Mapper.INSTANCE.toSanDoku(score)).getPp();
    } catch (BadRequestException e) {
      System.out.println(SanDoku.unwrapError(e));
      throw e;
    }
  }

  @org.mapstruct.Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
  interface Mapper {
    Mapper INSTANCE = Mappers.getMapper(Mapper.class);

    @Mapping(target = "totalScore", expression = "java(0L)")
    ScoreInfo toSanDoku(OsuApiScore score);
  }
}

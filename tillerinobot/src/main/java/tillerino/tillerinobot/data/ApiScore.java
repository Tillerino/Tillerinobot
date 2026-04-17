package tillerino.tillerinobot.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import javax.annotation.CheckForNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.tillerino.mormon.KeyColumn;
import org.tillerino.mormon.Table;
import org.tillerino.osuApiModel.OsuApiScore;
import org.tillerino.osuApiModel.deserializer.DateToLong;
import org.tillerino.osuApiModel.types.*;
import tillerino.tillerinobot.data.ApiBeatmap.Mapper;

@Data
@EqualsAndHashCode
@Table("apiscores")
@ToString
@Slf4j
@KeyColumn({"userId", "beatmapId"})
public class ApiScore {
    public long downloaded = System.currentTimeMillis();

    @BeatmapId
    private int beatmapId;

    private long score;

    private int maxCombo;

    private int count300;
    private int count100;
    private int count50;

    private int countMiss;

    private int countKatu;

    private int countGeki;

    /** 1 = maximum combo of map reached; 0 otherwise */
    private int perfect;
    /** bitwise flag representation of mods used. see reference */
    @BitwiseMods
    private long mods;

    @UserId
    private int userId;

    @JsonDeserialize(using = DateToLong.class)
    @MillisSinceEpoch
    private long date;

    private String rank;

    @CheckForNull
    private Double pp = null;

    @GameMode
    private int mode;

    public double getAccuracy() {
        return OsuApiScore.getAccuracy(count300, count100, count50, countMiss);
    }

    @org.mapstruct.Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
    public interface Mapper {
        Mapper INSTANCE = Mappers.getMapper(Mapper.class);

        @Mapping(target = "downloaded", source = "downloaded")
        ApiScore fromApi(OsuApiScore api, long downloaded);

        @Mapping(target = "modsList", ignore = true)
        OsuApiScore toApi(ApiScore api);
    }
}

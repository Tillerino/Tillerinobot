package tillerino.tillerinobot.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.sql.Connection;
import java.sql.SQLException;
import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.tillerino.jagger.annotations.JdbcConfig;
import org.tillerino.jagger.annotations.JdbcInsert;
import org.tillerino.jagger.annotations.JsonConfig;
import org.tillerino.osuApiModel.OsuApiScore;
import org.tillerino.osuApiModel.deserializer.DateToLong;
import org.tillerino.osuApiModel.types.*;

@Data
@EqualsAndHashCode
@Table(name = "apiscores")
@ToString
public class ApiScore implements java.io.Serializable {
    public long downloaded = System.currentTimeMillis();

    @BeatmapId
    @Id
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
    @Id
    private int userId;

    @JsonDeserialize(using = DateToLong.class)
    @MillisSinceEpoch
    private long date;

    private String rank;

    @CheckForNull
    private Double pp = null;

    @GameMode
    private int mode;

    @Transient
    public double getAccuracy() {
        return OsuApiScore.getAccuracy(count300, count100, count50, countMiss);
    }

    @JdbcConfig(quoteChar = "`")
    @JsonConfig(onGeneratedClass = Singleton.class, onGeneratedConstructors = Inject.class)
    public interface Repo {
        @JdbcInsert("replace into apiscores (`s.#columns`) values (:s.#values)")
        void replaceAll(Connection c, Iterable<ApiScore> s) throws SQLException;

        @JdbcInsert
        void insert(Connection c, ApiScore s) throws SQLException;
    }

    @org.mapstruct.Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
    public interface Mapper {
        Mapper INSTANCE = Mappers.getMapper(Mapper.class);

        @Mapping(target = "downloaded", source = "downloaded")
        ApiScore fromApi(OsuApiScore api, long downloaded);
    }
}

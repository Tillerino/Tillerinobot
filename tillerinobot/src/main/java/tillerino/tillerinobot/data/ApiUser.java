package tillerino.tillerinobot.data;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
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
import org.tillerino.jagger.annotations.JdbcSelect;
import org.tillerino.jagger.annotations.JsonConfig;
import org.tillerino.osuApiModel.GameModes;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.GameMode;
import org.tillerino.osuApiModel.types.OsuName;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.util.PhaseTimer;
import tillerino.tillerinobot.OsuApi;

@Table(name = "apiusers")
@Data
@EqualsAndHashCode
@ToString
public class ApiUser {
    long downloaded = System.currentTimeMillis();

    @Id
    @UserId
    private int userId;

    @OsuName
    private String userName;
    /** Total amount for all ranked and approved beatmaps played */
    private int count300;
    /** Total amount for all ranked and approved beatmaps played */
    private int count100;
    /** Total amount for all ranked and approved beatmaps played */
    private int count50;
    /** Only counts ranked and approved beatmaps */
    private int playCount;
    /** Counts the best individual score on each ranked and approved beatmaps */
    private long rankedScore;

    /** Counts every score on ranked and approved beatmaps */
    private long totalScore;

    private int rank;

    private double level;

    private double pp;

    private double accuracy;

    /** Counts for SS/S/A ranks on maps */
    private int countSS;

    /** Counts for SS/S/A ranks on maps */
    private int countS;

    /** Counts for SS/S/A ranks on maps */
    private int countA;

    private String country;

    @Id
    @GameMode
    private int mode;

    /** @param maxAge if > 0, maximum age in milliseconds */
    public static ApiUser loadOrDownload(Repo repo, Connection c, @UserId int userid, long maxAge, OsuApi downloader)
            throws SQLException, IOException {
        ApiUser user;
        try (var _ = PhaseTimer.timeTask("loadUser")) {
            user = repo.findByUserId(c, userid).orElse(null);
        }

        if (user == null || (maxAge > 0 && user.downloaded < System.currentTimeMillis() - maxAge)) {
            try (var _ = PhaseTimer.timeTask("downloadUser")) {
                System.out.println("downloading user " + userid);
                user = downloader.getUser(userid, GameModes.OSU);
                System.out.println(".downloaded user " + userid);
            }

            if (user == null) return null;

            try (var _ = PhaseTimer.timeTask("persistUser")) {
                repo.replace(c, user);
            }
        }

        return user;
    }

    @org.mapstruct.Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
    public interface Mapper {
        ApiUser.Mapper INSTANCE = Mappers.getMapper(ApiUser.Mapper.class);

        @Mapping(target = "downloaded", source = "downloaded")
        ApiUser fromApi(OsuApiUser api, long downloaded);

        OsuApiUser toApi(ApiUser api);
    }

    @JdbcConfig(quoteChar = "`")
    @JsonConfig(onGeneratedClass = Singleton.class, onGeneratedConstructors = Inject.class)
    public interface Repo {
        @JdbcSelect(where = "`userId` = :userId")
        Optional<ApiUser> findByUserId(Connection c, @UserId int userId) throws SQLException;

        @JdbcInsert("REPLACE INTO `apiusers` (`u.#columns`) VALUES (:u.#values)")
        void replace(Connection c, ApiUser u) throws SQLException;
    }
}

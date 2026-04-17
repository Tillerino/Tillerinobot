package tillerino.tillerinobot.data;

import java.io.IOException;
import java.sql.SQLException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.tillerino.mormon.Database;
import org.tillerino.mormon.KeyColumn;
import org.tillerino.mormon.Persister;
import org.tillerino.mormon.Persister.Action;
import org.tillerino.mormon.Table;
import org.tillerino.osuApiModel.GameModes;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.GameMode;
import org.tillerino.osuApiModel.types.OsuName;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.util.PhaseTimer;
import tillerino.tillerinobot.OsuApi;

@Table("apiusers")
@Data
@EqualsAndHashCode
@KeyColumn("userId")
@ToString
public class ApiUser {
    long downloaded = System.currentTimeMillis();

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

    @GameMode
    private int mode;

    /** @param maxAge if > 0, maximum age in milliseconds */
    public static ApiUser loadOrDownload(Database database, @UserId int userid, long maxAge, OsuApi downloader)
            throws SQLException, IOException {
        ApiUser user;
        try (var _ = PhaseTimer.timeTask("loadUser")) {
            user = database.selectUnique(ApiUser.class)
                    .execute("where userId = ", userid)
                    .orElse(null);
        }

        if (user == null || (maxAge > 0 && user.downloaded < System.currentTimeMillis() - maxAge)) {
            try (var _ = PhaseTimer.timeTask("downloadUser")) {
                System.out.println("downloading user " + userid);
                user = downloader.getUser(userid, GameModes.OSU);
                System.out.println(".downloaded user " + userid);
            }

            if (user == null) return null;

            try (var _ = PhaseTimer.timeTask("persistUser");
                    Persister<ApiUser> persister = database.persister(ApiUser.class, Action.REPLACE)) {
                persister.persist(user);
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
}

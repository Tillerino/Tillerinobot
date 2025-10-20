package tillerino.tillerinobot.osutrack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import org.tillerino.osuApiModel.types.GameMode;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UpdateResult {
    private String username;

    @GameMode
    private int mode;

    @JsonProperty("playcount")
    private int playCount;

    @JsonProperty("pp_rank")
    private int ppRank;

    @JsonProperty("pp_raw")
    private float ppRaw;

    private float accuracy;

    @JsonProperty("total_score")
    private long totalScore;

    @JsonProperty("ranked_score")
    private long rankedScore;

    private int count300;

    private int count100;

    private int count50;

    private float level;

    @JsonProperty("count_rank_a")
    private int countRankA;

    @JsonProperty("count_rank_s")
    private int countRankS;

    @JsonProperty("count_rank_ss")
    private int countRankSS;

    @JsonProperty("levelup")
    private boolean levelUp;

    private boolean first;

    private boolean exists;

    @JsonProperty("newhs")
    private List<Highscore> newHighscores;
}

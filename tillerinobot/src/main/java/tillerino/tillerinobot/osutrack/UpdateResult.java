package tillerino.tillerinobot.osutrack;

import java.util.List;

import org.tillerino.osuApiModel.types.GameMode;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class UpdateResult {
    private String username;

    @GameMode
    private int mode;

    @SerializedName("playcount")
    private int playCount;

    @SerializedName("pp_rank")
    private int ppRank;

    @SerializedName("pp_raw")
    private float ppRaw;

    private float accuracy;

    @SerializedName("total_score")
    private long totalScore;

    @SerializedName("ranked_score")
    private long rankedScore;

    private int count300;

    private int count100;

    private int count50;

    private float level;

    @SerializedName("count_rank_a")
    private int countRankA;

    @SerializedName("count_rank_s")
    private int countRankS;

    @SerializedName("count_rank_ss")
    private int countRankSS;

    @SerializedName("levelup")
    private boolean levelUp;

    private boolean first;

    private boolean exists;

    @SerializedName("newhs")
    private List<Highscore> newHighscores;
}

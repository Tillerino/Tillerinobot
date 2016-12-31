package tillerino.tillerinobot.osutrack;

import lombok.Data;
import org.tillerino.osuApiModel.OsuApiScore;

@Data
public class Highscore extends OsuApiScore {
    private int ranking;
}

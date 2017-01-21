package tillerino.tillerinobot.osutrack;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.tillerino.osuApiModel.OsuApiScore;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Highscore extends OsuApiScore {
    private int ranking;
}

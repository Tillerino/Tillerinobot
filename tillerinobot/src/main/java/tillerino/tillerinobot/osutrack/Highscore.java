package tillerino.tillerinobot.osutrack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.tillerino.osuApiModel.OsuApiScore;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Highscore extends OsuApiScore {
    private int ranking;
}

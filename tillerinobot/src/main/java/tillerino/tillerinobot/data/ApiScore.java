package tillerino.tillerinobot.data;

import org.tillerino.mormon.KeyColumn;
import org.tillerino.mormon.Table;
import org.tillerino.osuApiModel.OsuApiScore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
@EqualsAndHashCode(callSuper=true)
@Table("apiscores")
@ToString(callSuper=true)
@Slf4j
@KeyColumn({"userId", "beatmapId"})
public class ApiScore extends OsuApiScore {
	public long downloaded = System.currentTimeMillis();
}

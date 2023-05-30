package tillerino.tillerinobot.data;

import javax.annotation.Nonnull;

import org.tillerino.mormon.KeyColumn;
import org.tillerino.mormon.Table;

import lombok.Data;

@Table("userdata")
@KeyColumn("userId")
@Data
public class BotUserData {
	@Nonnull
	private Integer userId = 0;

	/*
	 * At the time of this writing, the maximum length of data was 1440.
	 */
	private String userdata;
}

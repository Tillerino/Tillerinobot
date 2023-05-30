package tillerino.tillerinobot.data;

import org.tillerino.mormon.KeyColumn;
import org.tillerino.mormon.Table;

import lombok.Data;

/**
 * Configuration that can be changed while the bot runs.
 * This is stored as simple key-value in the database.
 */
@Data
@Table("botconfig")
@KeyColumn("path")
public class BotConfig {
	private String path;

	private String value;
}

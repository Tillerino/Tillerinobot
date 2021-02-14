package tillerino.tillerinobot.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * Configuration that can be changed while the bot runs.
 * This is stored as simple key-value in the database.
 */
@Data
@Table(name = "botconfig")
@Entity(name = "botconfig")
public class BotConfig {
	@Id
	private String path;

	private String value;
}

package tillerino.tillerinobot.data;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Table(name = "userdata")
@Entity(name = "userdata")
public class BotUserData {
	@Id
	@Column(name = "userid", nullable = false)
	@Nonnull
	private Integer userId = 0;

	/*
	 * At the time of this writing, the maximum length of data was 1440.
	 */
	@Column(length = 10000)
	private String userdata;
}

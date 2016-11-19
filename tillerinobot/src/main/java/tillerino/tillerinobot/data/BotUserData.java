package tillerino.tillerinobot.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity(name = "userdata")
public class BotUserData {
	@Id
	@Column(name = "userid")
	private int userId;
	
	/*
	 * At the time of this writing, the maximum length of data was 1440.
	 */
	@Column(length = 10000)
	private String userdata;
}

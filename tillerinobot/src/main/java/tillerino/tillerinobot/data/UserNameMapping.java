package tillerino.tillerinobot.data;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Entity(name = "usernames")
@Data
public class UserNameMapping {
	@Id
	private String userName;

	private int userid;

	private long resolved;

	private long firstresolveattempt;
}

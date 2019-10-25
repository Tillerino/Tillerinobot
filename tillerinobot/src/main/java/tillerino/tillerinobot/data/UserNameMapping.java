package tillerino.tillerinobot.data;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.chat.IRCName;

import lombok.Data;

@Entity(name = "usernames")
@Data
public class UserNameMapping {
	@Id
	@IRCName
	private String userName;

	@UserId
	private int userid;

	private long resolved;

	private long firstresolveattempt;
}

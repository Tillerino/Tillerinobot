package tillerino.tillerinobot.data;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.tillerino.osuApiModel.types.UserId;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import tillerino.tillerinobot.BotBackend.IRCName;

@Entity(name = "usernames")
@Data
public class UserNameMapping {
	@Id
	@IRCName
	@Getter(onMethod = @__(@IRCName))
	@Setter(onParam = @__(@IRCName))
	private String userName;

	@UserId
	@Getter(onMethod = @__(@UserId))
	@Setter(onParam = @__(@UserId))
	private int userid;

	private long resolved;

	private long firstresolveattempt;
}

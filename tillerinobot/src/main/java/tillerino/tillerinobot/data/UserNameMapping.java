package tillerino.tillerinobot.data;

import lombok.Data;
import org.tillerino.mormon.KeyColumn;
import org.tillerino.mormon.Table;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.chat.IRCName;

@Table("usernames")
@KeyColumn("userName")
@Data
public class UserNameMapping {
    @IRCName
    private String userName;

    @UserId
    private int userid;

    private long resolved;

    private long firstresolveattempt;
}

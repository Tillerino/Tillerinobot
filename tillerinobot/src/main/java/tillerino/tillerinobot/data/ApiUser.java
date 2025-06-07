package tillerino.tillerinobot.data;

import java.io.IOException;
import java.sql.SQLException;

import org.tillerino.mormon.Database;
import org.tillerino.mormon.KeyColumn;
import org.tillerino.mormon.Persister;
import org.tillerino.mormon.Table;
import org.tillerino.mormon.Persister.Action;
import tillerino.tillerinobot.OsuApi;
import org.tillerino.osuApiModel.GameModes;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.UserId;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.tillerino.ppaddict.util.PhaseTimer;

@Table("apiusers")
@Data
@EqualsAndHashCode(callSuper=true)
@KeyColumn("userId")
@ToString(callSuper=true)
public class ApiUser extends OsuApiUser {
	long downloaded = System.currentTimeMillis();
	
	/**
	 * 
	 * @param database 
	 * @param userid
	 * @param maxAge if > 0, maximum age in milliseconds
	 * @return
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static ApiUser loadOrDownload(Database database, @UserId int userid, long maxAge, OsuApi downloader) throws SQLException, IOException {
		ApiUser user;
		try (var _ = PhaseTimer.timeTask("loadUser")) {
			user = database.selectUnique(ApiUser.class)."where userId = \{userid}".orElse(null);
		}

		if(user == null || (maxAge > 0 && user.downloaded < System.currentTimeMillis() - maxAge)) {
			try (var _ = PhaseTimer.timeTask("downloadUser")) {
				System.out.println("downloading user " + userid);
				user = downloader.getUser(userid, GameModes.OSU);
				System.out.println(".downloaded user " + userid);
			}

			if(user == null)
				return null;
			
			try(var _ = PhaseTimer.timeTask("persistUser");
					Persister<ApiUser> persister = database.persister(ApiUser.class, Action.REPLACE)) {
				persister.persist(user);
			}
		}
		
		return user;
	}
}

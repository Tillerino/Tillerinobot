package tillerino.tillerinobot.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.UserId;

@Entity(name = "givenrecommendations")
@Data
public class GivenRecommendation {
	public GivenRecommendation(@UserId int userid, @BeatmapId int beatmapid,
			long date, @BitwiseMods long mods) {
		super();
		this.userid = userid;
		this.beatmapid = beatmapid;
		this.date = date;
		this.mods = mods;
	}
	
	public GivenRecommendation() {
		
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@UserId
	@Getter(onMethod = @__(@UserId))
	@Setter(onParam = @__(@UserId))
	private int userid;
	@BeatmapId
	@Getter(onMethod = @__(@BeatmapId))
	@Setter(onParam = @__(@BeatmapId))
	private int beatmapid;
	private long date;
	@BitwiseMods
	@Getter(onMethod = @__(@BitwiseMods))
	@Setter(onParam = @__(@BitwiseMods))
	public long mods;

	/**
	 * If true, this won't be taken into consideration when generating
	 * recommendations.
	 */
	private boolean forgotten = false;
	/**
	 * If true, this won't be displayed in the recommendations list in ppaddict
	 * anymore.
	 */
	private boolean hidden = false;
}
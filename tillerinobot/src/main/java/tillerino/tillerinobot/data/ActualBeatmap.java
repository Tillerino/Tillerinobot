package tillerino.tillerinobot.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "actualbeatmaps")
@SuppressFBWarnings(value = "EI")
public class ActualBeatmap {
	@Id
	private int beatmapid;
	@Column(length = 128 * 1024 * 1024) // arbitrary cap
	private byte[] content;
	private long downloaded;
	private String hash;
}
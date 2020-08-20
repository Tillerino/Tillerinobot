package tillerino.tillerinobot.data;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "actualbeatmaps")
@Entity(name = "actualbeatmaps")
@SuppressFBWarnings(value = "EI")
public class ActualBeatmap {
	@Id
	@Nonnull
	@Column(nullable = false)
	private Integer beatmapid = 0;

	@Column(length = 128 * 1024 * 1024) // arbitrary cap
	private byte[] content;
	private long downloaded;
	private String hash;
}
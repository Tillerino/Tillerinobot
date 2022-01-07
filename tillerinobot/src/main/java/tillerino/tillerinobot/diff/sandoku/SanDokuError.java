package tillerino.tillerinobot.diff.sandoku;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = { "EI_EXPOSE_REP", "EI_EXPOSE_REP2" },
		justification = "Yes, but also this is wayyy too annoying to do correctly. It's a DTO, relax.")
@JsonIgnoreProperties(ignoreUnknown = true)
public record SanDokuError(
		String title,
		Map<String,List<String>> errors
		) {
}

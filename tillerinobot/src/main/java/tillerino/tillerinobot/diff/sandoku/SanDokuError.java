package tillerino.tillerinobot.diff.sandoku;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SanDokuError(
		String title,
		Map<String,List<String>> errors
		) {
}

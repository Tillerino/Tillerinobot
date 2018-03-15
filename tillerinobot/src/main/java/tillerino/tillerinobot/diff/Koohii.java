package tillerino.tillerinobot.diff;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import org.tillerino.osuApiModel.Mods;

import com.github.francesco149.koohii.Koohii.DiffCalc;
import com.github.francesco149.koohii.Koohii.Map;
import com.github.francesco149.koohii.Koohii.Parser;

import lombok.Value;
import tillerino.tillerinobot.UserException;

public class Koohii implements DifficultyCalculator {
	@Value
	private static class KoohiiDifficulty implements DifficultyProperties {
		int allObjectsCount;

		int circleCount;

		double speed;

		double aim;

		int maxCombo;
	}

	@Override
	public DifficultyProperties calculate(InputStream is, Collection<Mods> mods)
			throws UserException, IOException {
		Parser parser = new Parser();
		Map map = parser.map(new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)));
		DiffCalc diff = new DiffCalc().calc(map, (int) Mods.getMask(mods));
		return new KoohiiDifficulty(map.objects.size(), map.ncircles, diff.speed, diff.aim, map.max_combo());
	}
}

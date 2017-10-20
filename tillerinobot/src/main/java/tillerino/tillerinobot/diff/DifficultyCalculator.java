package tillerino.tillerinobot.diff;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import org.tillerino.osuApiModel.Mods;

import tillerino.tillerinobot.UserException;

public interface DifficultyCalculator {
	DifficultyProperties calculate(InputStream byis, Collection<Mods> mods)
			throws UserException, UnsupportedEncodingException, IOException;

}
package tillerino.tillerinobot.diff;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import lombok.SneakyThrows;
import lombok.Value;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.tillerino.osuApiModel.Mods;

import tillerino.tillerinobot.UserException;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;


public class Oppai {
	@Value
	public class OppaiResults {
		@SerializedName("oppai_version")
		String oppaiVersion;
		String artist;
		String title;
		String version;
		String creator;
		@SerializedName("mods_str")
		String mods;
		double od;
		double ar;
		double cs;
		int combo;
		@SerializedName("max_combo")
		int maxCombo;
		@SerializedName("num_circles")
		int circleCount;
		@SerializedName("num_sliders")
		int sliderCount;
		@SerializedName("num_spinners")
		int spinnerCount;
		int misses;
		@SerializedName("score_version")
		int scoreVersion;
		double stars;
		@SerializedName("speed_stars")
		double speed;
		@SerializedName("aim_stars")
		double aim;
		double pp;
		
		public int getAllObjectsCount() {
			return getCircleCount() + getSliderCount() + getSpinnerCount();
		}
	}

	@SneakyThrows(UnsupportedEncodingException.class)
	public OppaiResults runOppai(byte[] beatmap, Collection<Mods> mods) throws UserException {
		ByteArrayInputStream byis = new ByteArrayInputStream(beatmap);
		DefaultExecutor executor = new DefaultExecutor();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		executor.setStreamHandler(new PumpStreamHandler(baos, baos, byis));
		try {
			CommandLine command = new CommandLine("oppai").addArgument("-").addArgument("-ojson");
			if (!mods.isEmpty()) {
				command = command.addArgument("+" + Mods.toShortNamesContinuous(mods));
			}
			executor.execute(command);
		} catch (IOException e) {
			if (beatmap.length == 1024 * 1024) {
				throw new UserException("Beatmap file truncated, see https://github.com/ppy/osu-api/issues/131");
			}
			throw new RuntimeException("error running oppai: " + baos.toString("UTF-8"), e);
		}
		try {
			return new Gson().fromJson(baos.toString("UTF-8"), OppaiResults.class);
		} catch (Exception e) {
			throw new RuntimeException("Error running Oppai: " + baos.toString("UTF-8"), e);
		}
	}
}

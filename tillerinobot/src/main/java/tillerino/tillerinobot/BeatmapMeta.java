package tillerino.tillerinobot;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.types.BitwiseMods;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.diff.PercentageEstimates;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static java.lang.String.format;

@Data
@AllArgsConstructor
public class BeatmapMeta {
	OsuApiBeatmap beatmap;

	Integer personalPP;
	
	PercentageEstimates estimates;

	static DecimalFormat format = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));

	static DecimalFormat noDecimalsFormat = new DecimalFormat("#", new DecimalFormatSymbols(Locale.US));

	public String formInfoMessage(boolean formLink, String addition, int hearts, Double acc, Integer combo, Integer misses) throws UserException {
		return formInfoMessage(formLink, addition, hearts, PpMessageBuilder.getFor(acc, combo, misses));
	}

	public String formInfoMessage(boolean formLink, String addition, int hearts, int x100, int x50, int combo, int misses) throws UserException {
		return formInfoMessage(formLink, addition, hearts, PpMessageBuilder.getFor(x100, x50, combo, misses));
	}

	public String formInfoMessage(boolean formLink, String addition, int hearts, PpMessageBuilder ppMessageBuilder) throws UserException {
		if (beatmap.getMaxCombo() <= 0) {
			// This is kind of an awkward place to warn about this, but we don't want to be throwing UserExceptions from the backend.
			throw new UserException(
					format("Encountered a [https://osu.ppy.sh/b/%s broken beatmap], see [https://github.com/ppy/osu-api/issues/130 this issue]",
							beatmap.getBeatmapId()));
		}

		String beatmapName = String.format("%s - %s [%s]", getBeatmap().getArtist(), getBeatmap().getTitle(),
				getBeatmap().getVersion());
		if(formLink) {
			beatmapName = String.format("[http://osu.ppy.sh/b/%d %s]", getBeatmap().getBeatmapId(), beatmapName);
		}

		beatmapName += formModsSuffix();

		String estimateMessage = "";
		Integer future = getPersonalPP();
		if (future != null && future >= getPpForAcc(.9)
				&& future < getPpForAcc(1) * 1.05) {
			future = (int) Math.floor(Math.min(future, getPpForAcc(1)));
			estimateMessage += "future you: " + future + "pp | ";
		}

        estimateMessage += ppMessageBuilder.buildMessage(getEstimates());

        if (getEstimates().isShaky()) {
            estimateMessage += " (rough estimates)";
        }

        estimateMessage += " | " + secondsToMinuteColonSecond(getBeatmap().getTotalLength(getMods()));

		Double starDiff = null;
		if (getMods() == 0) {
			starDiff = beatmap.getStarDifficulty();
		} else {
			starDiff = estimates.getStarDiff();
		}
		if (starDiff != null) {
			estimateMessage += " ★ " + format.format(starDiff);
		}

		estimateMessage += " ♫ " + format.format(getBeatmap().getBpm(getMods()));
		estimateMessage += " AR" + format.format(getBeatmap().getApproachRate(getMods()));
		estimateMessage += " OD" + format.format(getBeatmap().getOverallDifficulty(getMods()));
		
		if (estimates.isOppaiOnly()) {
			estimateMessage += " (";
			if (!estimates.isRanked()) {
				estimateMessage += "unranked; ";
			}
			estimateMessage += "all [https://github.com/Francesco149/oppai oppai])";
		}

		String heartString = hearts > 0 ? " " + StringUtils.repeat('♥', hearts) : "";

		return beatmapName + "   " + estimateMessage + (addition != null ? "   " + addition : "") + heartString;
	}

	public static String secondsToMinuteColonSecond(int length) {
		return length / 60 + ":" + StringUtils.leftPad(String.valueOf(length % 60), 2, '0');
	}
	
	/**
	 * Mods presented by this meta
	 * @return 0 if estimates are old style
	 */
	@BitwiseMods
	public long getMods() {
		return estimates.getMods();
	}
	
	public BeatmapWithMods getBeatmapWithMods() {
		return new BeatmapWithMods(beatmap.getBeatmapId(), getMods());
	}

	double getPpForAcc(double acc) {
		return getEstimates().getPP(acc);
	}

	public interface PpMessageBuilder {
	    String buildMessage(PercentageEstimates estimates);

        static PpMessageBuilder getFor(Double acc, Integer combo, Integer misses) {
            if(acc == null) {
                return new DefaultPpMessageBuilder();
            }
            if(combo == null || misses == null) {
                return new AccPpMessageBuilder(acc);
            }
            return new AccComboMissesPpMessageBuilder(acc, combo, misses);
        }

        static PpMessageBuilder getFor(int x100, int x50, int combo, int misses) {
            return new HitPointsComboMissesPpMessageBuilder(x100, x50, combo, misses);
        }
    }

    public static class DefaultPpMessageBuilder implements PpMessageBuilder {
        @Override
        public String buildMessage(PercentageEstimates estimates) {
            return "95%: " + noDecimalsFormat.format(estimates.getPP(.95)) + "pp" +
                    " | 98%: " + noDecimalsFormat.format(estimates.getPP(.98)) + "pp" +
                    " | 99%: " + noDecimalsFormat.format(estimates.getPP(.99)) + "pp" +
                    " | 100%: " + noDecimalsFormat.format(estimates.getPP(1)) + "pp";
        }
    }

    public static class AccPpMessageBuilder implements PpMessageBuilder {
	    private final double acc;

        public AccPpMessageBuilder(double acc) {
            this.acc = acc;
        }

        @Override
        public String buildMessage(PercentageEstimates estimates) {
            return format.format(acc * 100) + "%: " +
                    noDecimalsFormat.format(estimates.getPP(acc)) + "pp";
        }
    }

    public static class AccComboMissesPpMessageBuilder implements PpMessageBuilder {
	    private final double acc;
        private final int combo;
        private final int misses;

        public AccComboMissesPpMessageBuilder(double acc, int combo, int misses) {
            this.acc = acc;
            this.combo = combo;
            this.misses = misses;
        }

        @Override
        public String buildMessage(PercentageEstimates estimates) {
            return format.format(acc * 100) + "% " + combo + "x " + misses + "miss: " +
                    noDecimalsFormat.format(estimates.getPP(acc, combo, misses)) + "pp";
        }
    }

    public static class HitPointsComboMissesPpMessageBuilder implements PpMessageBuilder {
	    private final int x100;
        private final int x50;
        private final int combo;
        private final int misses;

        public HitPointsComboMissesPpMessageBuilder(int x100, int x50, int combo, int misses) {
            this.x100 = x100;
            this.x50 = x50;
            this.combo = combo;
            this.misses = misses;
        }

        @Override
        public String buildMessage(PercentageEstimates estimates) {
            return x100 + "x100 " + x50 + "x50 " + combo + "x " + misses + "miss: " +
                    noDecimalsFormat.format(estimates.getPP(x100, x50, combo, misses)) + "pp";
        }
    }

	String formModsSuffix() {
		PercentageEstimates percentageEstimates = getEstimates();
		if (percentageEstimates.getMods() == 0) {
			return "";
		}
		StringBuilder modsString = new StringBuilder();
		for (Mods mod : Mods.getMods(percentageEstimates.getMods())) {
			if (mod.isEffective()) {
				modsString.append(mod.getShortName());
			}
		}
		return " " + modsString;
	}
}

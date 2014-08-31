package tillerino.tillerinobot;

import java.text.DecimalFormat;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.apache.commons.lang3.StringUtils;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;

@Data
@AllArgsConstructor
public class BeatmapMeta {
	public interface Estimates {
		
	}
	
	public interface OldEstimates extends Estimates {
		Integer getMaxPP();
		
		double getCommunityPP();
		
		boolean isTrustCommunity();

		boolean isTrustMax();
	}
	
	public interface PercentageEstimates extends Estimates {
		double getPPForAcc(double acc);
		
		long getMods();
	}
	
	OsuApiBeatmap beatmap;

	Integer personalPP;
	
	Estimates estimates;

	static DecimalFormat format = new DecimalFormat("#.##");

	static DecimalFormat noDecimalsFormat = new DecimalFormat("#");

	public String formInfoMessage(boolean formLink, String addition, int hearts) {
		String beatmapName = getBeatmap().getArtist() + " - " + getBeatmap().getTitle()
				+ " [" + getBeatmap().getVersion() + "]";
		if(formLink) {
			beatmapName = "[http://osu.ppy.sh/b/" + getBeatmap().getId() + " " + beatmapName + "]";
		}
		
		if(getEstimates() instanceof PercentageEstimates) {
			PercentageEstimates percentageEstimates = (PercentageEstimates) getEstimates();
			if(percentageEstimates.getMods() != 0) {
				String mods = "";
				for(Mods mod : Mods.getMods(percentageEstimates.getMods())) {
					if(mod.isEffective()) {
						mods += mod.getShortName();
					}
				}
				beatmapName += " " + mods;
			}
		}

		String estimateMessage = "";
		if(getPersonalPP() != null) {
			estimateMessage += "future you: " + getPersonalPP() + "pp | ";
		}
		
		Estimates estimates = getEstimates();
		
		if (estimates instanceof OldEstimates) {
			OldEstimates oldEstimates = (OldEstimates) estimates;
			
			double community = oldEstimates.getCommunityPP();
			community = Math.round(community * 2) / 2;
			String ppestimate = community % 1 == 0 ? "" + (int) community : "" + format.format(community);

			String cQ = oldEstimates.isTrustCommunity() ? "" : "??";
			String bQ = oldEstimates.isTrustMax() ? "" : "??";
			
			estimateMessage += "community: " + ppestimate + cQ + "pp";
			if(oldEstimates.getMaxPP() != null)
				estimateMessage += " | best: " + oldEstimates.getMaxPP() + bQ + "pp";
		} else if (estimates instanceof PercentageEstimates) {
			PercentageEstimates percentageEstimates = (PercentageEstimates) estimates;

			estimateMessage += "95%: " + noDecimalsFormat.format(percentageEstimates.getPPForAcc(.95)) + "pp";
			estimateMessage += " | 98%: " + noDecimalsFormat.format(percentageEstimates.getPPForAcc(.98)) + "pp";
			estimateMessage += " | 99%: " + noDecimalsFormat.format(percentageEstimates.getPPForAcc(.99)) + "pp";
			estimateMessage += " | 100%: " + noDecimalsFormat.format(percentageEstimates.getPPForAcc(1)) + "pp";
		}
		
		estimateMessage += " | " + secondsToMinuteColonSecond(getBeatmap().getTotalLength());
		estimateMessage += " ★ " + format.format(getBeatmap().getStarDifficulty());
		estimateMessage += " ♫ " + format.format(getBeatmap().getBpm());
		estimateMessage += " AR" + format.format(getBeatmap().getApproachRate());

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
	public long getMods() {
		if (estimates instanceof PercentageEstimates) {
			PercentageEstimates percentageEstimates = (PercentageEstimates) estimates;
			
			return percentageEstimates.getMods();
		}
		
		return 0;
	}
}

package tillerino.tillerinobot;

import java.text.DecimalFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.types.BitwiseMods;

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
		
		@BitwiseMods
		long getMods();
		
		boolean isShaky();
	}
	
	OsuApiBeatmap beatmap;

	Integer personalPP;
	
	Estimates estimates;

	static DecimalFormat format = new DecimalFormat("#.##");

	static DecimalFormat noDecimalsFormat = new DecimalFormat("#");

	public String formInfoMessage(boolean formLink, String addition, int hearts, Double acc) {
		
		String beatmapName = getBeatmap().getArtist() + " - " + getBeatmap().getTitle()
				+ " [" + getBeatmap().getVersion() + "]";
		if(formLink) {
			beatmapName = "[http://osu.ppy.sh/b/" + getBeatmap().getBeatmapId() + " " + beatmapName + "]";
		}
		
		long mods = 0;
		
		if(getEstimates() instanceof PercentageEstimates) {
			PercentageEstimates percentageEstimates = (PercentageEstimates) getEstimates();
			mods = percentageEstimates.getMods();
			if(percentageEstimates.getMods() != 0) {
				StringBuilder modsString = new StringBuilder();
				for(Mods mod : Mods.getMods(percentageEstimates.getMods())) {
					if(mod.isEffective()) {
						modsString.append(mod.getShortName());
					}
				}
				beatmapName += " " + modsString;
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
			community = Math.round(community * 2) / 2d;
			String ppestimate = community % 1 == 0 ? "" + (int) community : "" + format.format(community);

			String cQ = oldEstimates.isTrustCommunity() ? "" : "??";
			String bQ = oldEstimates.isTrustMax() ? "" : "??";
			
			estimateMessage += "community: " + ppestimate + cQ + "pp";
			if(oldEstimates.getMaxPP() != null)
				estimateMessage += " | best: " + oldEstimates.getMaxPP() + bQ + "pp";
		} else if (estimates instanceof PercentageEstimates) {
			PercentageEstimates percentageEstimates = (PercentageEstimates) estimates;

			if(acc != null) {
				estimateMessage += format.format(acc * 100) + "%: "
						+ noDecimalsFormat.format(percentageEstimates.getPPForAcc(acc)) + "pp";
			} else {
				estimateMessage += "95%: " + noDecimalsFormat.format(percentageEstimates.getPPForAcc(.95)) + "pp";
				estimateMessage += " | 98%: " + noDecimalsFormat.format(percentageEstimates.getPPForAcc(.98)) + "pp";
				estimateMessage += " | 99%: " + noDecimalsFormat.format(percentageEstimates.getPPForAcc(.99)) + "pp";
				estimateMessage += " | 100%: " + noDecimalsFormat.format(percentageEstimates.getPPForAcc(1)) + "pp";
			}
			if(percentageEstimates.isShaky()) {
				estimateMessage += " (rough estimates)";
			}
		}
		
		estimateMessage += " | " + secondsToMinuteColonSecond(getBeatmap().getTotalLength(mods));
		if(mods == 0)
			estimateMessage += " ★ " + format.format(getBeatmap().getStarDifficulty());
		estimateMessage += " ♫ " + format.format(getBeatmap().getBpm(mods));
		estimateMessage += " AR" + format.format(getBeatmap().getApproachRate(mods));

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
		if (estimates instanceof PercentageEstimates) {
			PercentageEstimates percentageEstimates = (PercentageEstimates) estimates;
			
			return percentageEstimates.getMods();
		}
		
		return 0;
	}
}

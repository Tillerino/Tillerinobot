package tillerino.tillerinobot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tillerino.osuApiModel.Mods;

import tillerino.tillerinobot.RecommendationsManager.Model;

public enum RecommendationType{
	
	//Types
	NOMOD, 
	ANY, 
	DT, 
	NC,
	HR, 
	HD,
	RELAX,
	BETA,
	GAMMA, 
	Mod;
	
	
	public static String parseStr(RecommendationType[] rt) {
		List<String> str = new ArrayList<String>();
		for(int i = 0; i < rt.length; i++)
		{
			if(rt.length == 0)
			{
				continue;
			}
			if(rt[i].equals(NOMOD)) {
				str.add("Nomod");
				continue;
			}
			if(rt[i].equals(ANY)) {
				str.add("Any");
				continue;
			}
			if(rt[i].equals(DT)) {
				str.add("DT");
				continue;
			}
			if(rt[i].equals(HR)) {
				str.add("HR");
				continue;
			}
			if(rt[i].equals(HD)) {
				str.add("HD");
				continue;
			}
			if(rt[i].equals(RELAX)) {
				str.add("Relax");
				continue;
			}
			if(rt[i].equals(BETA)) {
				str.add("Beta");
				continue;
			}
			if(rt[i].equals(GAMMA)) {
				str.add("Gamma");
				continue;
			}
		}
		Collections.sort(str);
		StringBuilder sb = new StringBuilder();
		int i = 1;
		for (String s : str)
		{
		    sb.append(s);
		    if(i != str.size())
		    {
		        sb.append(", ");
		    }
		    else
		    {
		    	sb.append(".");
		    }
		    i++;
		}
		return sb.toString();
	}


	public static Mods typeMod(RecommendationType rt) {
		switch (rt)
		{
		case DT:
			return Mods.DoubleTime;
		case NC:
			return Mods.Nightcore;
		case HR:
			return Mods.HardRock;
		case HD:
			return Mods.Hidden;
		default:
			return null;
		}
	}
	
	public static Model typeModel(RecommendationType rt) {
		switch (rt)
		{
		case RELAX:
			return Model.ALPHA;
		case BETA:
			return Model.BETA;
		case GAMMA:
			return Model.GAMMA;
		default:
			return Model.GAMMA;
		}
	}
}


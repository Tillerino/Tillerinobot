package tillerino.tillerinobot.handlers;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;
import static org.apache.commons.lang3.StringUtils.join;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;

import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationType;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserDataManager.UserData.LanguageIdentifier;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Language;

public class OptionsHandler implements CommandHandler {
	@Override
	public boolean handle(String command, IRCBotUser ircUser,
			OsuApiUser apiUser, UserData userData) throws UserException,
			IOException, SQLException {
		boolean set = false;
		
		if (command.toLowerCase().startsWith("set")) {
			set = true;
			command = command.substring("set".length()).trim();
		} else if (command.toLowerCase().startsWith("show")
				|| command.toLowerCase().startsWith("view")) {
			command = command.substring("show".length()).trim();
		} else {
			return false;
		}
		
		if (set && !command.contains(" ")) {
			throw new UserException(userData.getLanguage().setFormat());
		}

		String option = set ? command.substring(0, command.indexOf(' '))
				.toLowerCase() : command.toLowerCase();
		String value = set ? command.substring(option.length() + 1) : null;

		if (option.equals("lang") || getLevenshteinDistance(option, "language") <= 1) {
			if (set) {
				LanguageIdentifier ident;
				try {
					ident = find(LanguageIdentifier.values(), value);
				} catch (IllegalArgumentException e) {
					LanguageIdentifier[] values = LanguageIdentifier.values();
					Arrays.sort(values, new Comparator<LanguageIdentifier>() {
						@Override
						public int compare(LanguageIdentifier o1, LanguageIdentifier o2) {
							return o1.toString().compareTo(o2.toString());
						}
					});
					throw new UserException(userData.getLanguage().invalidChoice(value,
							join(values, ", ")));
				}

				userData.setLanguage(ident);

				userData.getLanguage().optionalCommentOnLanguage(ircUser,
						apiUser);
			} else {
				ircUser.message("Language: " + userData.getLanguageIdentifier().toString());
			}
		} else if (getLevenshteinDistance(option, "welcome") <= 1 && userData.getHearts() > 0) {
			if (set) {
				userData.setShowWelcomeMessage(parseBoolean(value, userData.getLanguage()));
			} else {
				ircUser.message("Welcome Message: " + (userData.isShowWelcomeMessage() ? "ON" : "OFF"));
			}
		} else if (getLevenshteinDistance(option, "recommendmods") <= 1) {
			if (set) {
				userData.setRecommendMods(parseType(value, userData.getLanguage()));
			} else {
				ircUser.message("Recommend modded maps: " + RecommendationType.parseStr(userData.getRecommendModdedMaps()));
			}
	    } else {
			throw new UserException(userData.getLanguage().invalidChoice(option,
					"Language" + (userData.getHearts() > 0 ? ", Welcome" : "")));
		}

		return true;
	}

	private static boolean parseBoolean(final @Nonnull String original, Language lang) throws UserException {
		String s = original.toLowerCase();
		if(s.equals("on") || s.equals("true") || s.equals("yes") || s.equals("1")) {
			return true;
		}
		if(s.equals("off") || s.equals("false") || s.equals("no") || s.equals("0")) {
			return false;
		}
		throw new UserException(lang.invalidChoice(original, "on|true|yes|1|off|false|no|0"));
	}
	
	private static RecommendationType[] parseType(final @Nonnull String o, Language lang) throws UserException 
	{
	    List<RecommendationType> rtList = new ArrayList<RecommendationType>();
		String[] remaining = o.split(" ");
		 for(int i = 0; i < o.length() && i < remaining.length; i++)
		 {
			String param = remaining[i];
			String lowerCase = param.toLowerCase();
			if(lowerCase.length() == 0)
			{
				continue;
			}
			if(!rtList.contains(RecommendationType.ANY))
			{   
				
			  if (!rtList.contains(RecommendationType.NOMOD) &&
			     !(rtList.contains(RecommendationType.DT) ||
				   rtList.contains(RecommendationType.HD) || 
				   rtList.contains(RecommendationType.HR)))
			  {
				  
				if (getLevenshteinDistance(lowerCase, "any") <= 2) {
				    rtList.add(RecommendationType.ANY);
				    continue;
				
				}
				if(getLevenshteinDistance(lowerCase, "nomod") <= 2) {
				   rtList.add(RecommendationType.NOMOD);
				   continue;
				}
				
			  }
			  
			  if (!(rtList.contains(RecommendationType.RELAX) ||
				    rtList.contains(RecommendationType.BETA)  ||
				    rtList.contains(RecommendationType.GAMMA)))
			  {
				  if(!(rtList.contains(RecommendationType.DT) ||
					   rtList.contains(RecommendationType.HD) || 
				       rtList.contains(RecommendationType.HR)))
				  {
						if (getLevenshteinDistance(lowerCase, "relax") <= 2) {
							rtList.add(RecommendationType.RELAX);
							continue;
						}
						if (getLevenshteinDistance(lowerCase, "beta") <= 1) {
							rtList.add(RecommendationType.BETA);
							continue;
						}
				  }
					if (getLevenshteinDistance(lowerCase, "gamma") <= 2) {
						rtList.add(RecommendationType.GAMMA);
						continue;
					}
			  }
			  
			  
				 if (!rtList.contains(RecommendationType.NOMOD) &&
				    !(rtList.contains(RecommendationType.BETA)  ||
				      rtList.contains(RecommendationType.RELAX)))
				 {
					 
					  if (!rtList.contains(RecommendationType.DT) && ((lowerCase.equals("dt") || lowerCase.equals("nc")))) 
					  {
							rtList.add(RecommendationType.DT);
							continue;
					  }
					  
					  if (!rtList.contains(RecommendationType.HR) && lowerCase.equals("hr")) 
					  {
							rtList.add(RecommendationType.HR);
							continue;
					  }
					  
					  if (!rtList.contains(RecommendationType.HD) && lowerCase.equals("hd")) 
					  {
							rtList.add(RecommendationType.HD);
							continue;
					  }
					  
			    }
		  }
		}
		if(rtList.size() < 1)
		{
		throw new UserException(lang.invalidChoice(o, "Nomod|Any & DT/NC/HR/HD & Relax|Gamma|Beta (You can choose more than 1)"));
		}
		else
			return rtList.toArray(new RecommendationType[rtList.size()]);
	}
	
	public static @Nonnull <E extends Enum<E>> E find(@Nonnull E[] haystack, @Nonnull String needle) {
		needle = needle.toLowerCase();
		
		E found = null;
		
		for (int i = 0; i < haystack.length; i++) {
			if(getLevenshteinDistance(haystack[i].toString().toLowerCase(), needle) <= 1) {
				if(found != null) {
					throw new IllegalArgumentException();
				}
				found = haystack[i];
			}
		}
		
		if(found == null) {
			throw new IllegalArgumentException();
		}
		
		return found;
	}
}

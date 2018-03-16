package tillerino.tillerinobot;

import javax.annotation.CheckForNull;

import org.pircbotx.PircBotX;

public interface BotRunner extends TidyObject, Runnable {
	@CheckForNull
	PircBotX getBot();
}
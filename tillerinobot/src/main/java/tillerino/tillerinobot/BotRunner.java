package tillerino.tillerinobot;

import javax.annotation.CheckForNull;

import org.pircbotx.PircBotX;

public interface BotRunner extends TidyObject {
	@CheckForNull
	PircBotX getBot();

	void run();
}
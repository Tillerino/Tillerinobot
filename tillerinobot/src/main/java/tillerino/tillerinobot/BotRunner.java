package tillerino.tillerinobot;

import javax.annotation.CheckForNull;

import org.pircbotx.PircBotX;

public interface BotRunner {
	@CheckForNull
	PircBotX getBot();

	void run();
}
package tillerino.tillerinobot;


public interface IRCBotUser {
	String getNick();
	void message(String msg);
}
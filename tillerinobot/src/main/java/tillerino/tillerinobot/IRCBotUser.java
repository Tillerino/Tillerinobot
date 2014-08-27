package tillerino.tillerinobot;


public interface IRCBotUser {
	String getNick();
	/**
	 * 
	 * @param msg
	 * @return true if the message was sent
	 */
	boolean message(String msg);
	
	/**
	 * 
	 * @param msg
	 * @return true if the action was sent
	 */
	boolean action(String msg);
}
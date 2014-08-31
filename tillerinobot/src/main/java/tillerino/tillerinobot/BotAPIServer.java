package tillerino.tillerinobot;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.google.gson.Gson;

/**
 * 
 * 
 * @author Tillerino
 */
public class BotAPIServer extends AbstractHandler {
	IRCBot bot;
	
	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if(bot == null)
			return;
		
		if(!baseRequest.getRemoteAddr().equals("127.0.0.1") && !baseRequest.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
			return;
		}
		
		System.out.println(target);
		
		if(target.startsWith("/botinfo")) {
			botinfo(baseRequest, response);
		}
	}

	Gson gson = new Gson();
	
	public static class BotInfo {
		boolean isConnected;
		long runningSince;
		long lastPingDeath;
		long lastInteraction;
	}
	
	BotInfo botInfo = new BotInfo();
	
	private void botinfo(Request request, HttpServletResponse response) throws IOException {
		botInfo.isConnected = bot.bot.isConnected();
		gson.toJson(botInfo, response.getWriter());
		request.setHandled(true);
	}
}

package org.tillerino.ppaddict.live;

import java.io.IOException;
import java.util.function.Supplier;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rabbitmq.client.Channel;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HealthServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@SuppressFBWarnings("SE_BAD_FIELD")
	private final Supplier<Channel> channel;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final String path = req.getRequestURI();
		if (path.equalsIgnoreCase("/live")) {
			resp.setStatus(200);
		} else if (path.equalsIgnoreCase("/ready")) {
			Channel c = this.channel.get();
			resp.setStatus(c != null && c.isOpen() ? 200 : 404);
		} else {
			resp.setStatus(404);
		}
	}
}

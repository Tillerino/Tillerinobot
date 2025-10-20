package org.tillerino.ppaddict.server.auth;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.tillerino.ppaddict.server.UserDataServiceImpl;

@Singleton
public class AuthLogoutService extends HttpServlet {
    @Inject
    UserDataServiceImpl userDataService;

    private static final long serialVersionUID = 1L;

    public static final String PATH = "/authlogout";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        userDataService.logout(req, resp);

        resp.sendRedirect(req.getParameter("returnTo"));
    }

    public String getLogoutURL(String returnToUrl) {
        return PATH + "?returnTo=" + URLEncoder.encode(returnToUrl, StandardCharsets.UTF_8);
    }
}

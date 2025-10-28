package org.tillerino.ppaddict.server.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.tillerino.ppaddict.server.UserDataServiceImpl;

@Singleton
public class AuthLogoutService extends HttpServlet {
    private final UserDataServiceImpl userDataService;

    private static final long serialVersionUID = 1L;

    public static final String PATH = "/authlogout";

    @Inject
    public AuthLogoutService(UserDataServiceImpl userDataService) {
        this.userDataService = userDataService;
        userDataService.setLogoutService(this);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        userDataService.logout(req, resp);

        resp.sendRedirect(req.getParameter("returnTo"));
    }

    public String getLogoutURL(String returnToUrl) {
        return PATH + "?returnTo=" + URLEncoder.encode(returnToUrl, StandardCharsets.UTF_8);
    }
}

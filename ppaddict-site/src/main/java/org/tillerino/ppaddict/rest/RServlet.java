package org.tillerino.ppaddict.rest;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RServlet extends HttpServlet {
    @Inject
    public RServlet() {}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String html = RootServlet.buildHtml(
                "ppaddict+!r",
                "/htmx/recommendations/initial",
                "Loading recommendations...",
                RootServlet.R_LINK_HELP_RECOMMENDATIONS_PAGE);
        resp.getOutputStream().write(html.getBytes(StandardCharsets.UTF_8));
    }
}

package org.tillerino.ppaddict.rest;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RootServlet extends HttpServlet {
    private static final String SHARED_HEAD = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            TITLE
            <link rel="stylesheet" href="styles.css">
            <link rel="preload" href="/Cabin-SemiBold.ttf" as="font" type="font/ttf" crossorigin>
            <link rel="preload" href="/htmx/user" as="fetch" crossorigin>
            INITIAL_PRELOAD
            <script src="htmx-2.0.10.min.js"></script>
            <script src="code.js"></script>
        </head>
        <body onclick="outsideClick(event)" onload="showWelcomeIfNeeded()">
            <div class="header-bar">
                <h1><a href="v2.html">ppaddict</a> + <a href="r.html"R_LINK_HELP>!r</a></h1>
                <!-- noHeaders so that the preloaded requests can be matched. use a nested replace to avoid inheritance of hx-request. -->
                <div id="user-area">
                    <a href="v1.html" style="margin-right: 10px;">switch to old interface</a>
                    <a href="#" class="help-trigger" style="margin-right: 10px;" onclick="showHelp()"HELP_EXTERNAL_LINKS>Help</a>
                    <span hx-get="/htmx/user" hx-trigger="load" hx-request='{"noHeaders": true}' hx-swap="outerHTML">Loading...</span>
                </div>
            </div>
            <div class="table-container">
                TABLE_CONTENT
            </div>
            <dialog id="welcome-dialog" closedby="any">
              <h1>Welcome</h1>
              <p>Welcome to ppaddict. Here's the latest:</p>
              <p> We rebuilt the ppaddict user interface. Everything should be in its usual place.
                The old interface stays available; you can switch anytime via the link in the top right.
                If you run into any problems, please report them on
                <a href="https://github.com/Tillerino/ppaddict/issues" target="_blank">GitHub</a>.</p>
              <p>Click help to see what everything on the screen does or means. This changes when you
                navigate around or log in, so do it often!</p>
              <p>(Click anywhere outside this box to dismiss)</p>
            </dialog>
        </body>
        </html>
        """;

    static final HelpEntry HELP_EXTERNAL_LINKS = new HelpEntry(
            "<a href=\"https://github.com/Tillerino/Tillerinobot/wiki\" target=\"_blank\">Tillerinobot wiki</a>"
                    + "<br><a href=\"https://github.com/Tillerino/ppaddict\" target=\"_blank\">ppaddict source</a>",
            "omni");

    static final HelpEntry R_LINK_HELP_MAIN_PAGE = new HelpEntry("Click r! to access recommendations.", "below-right");
    static final HelpEntry R_LINK_HELP_RECOMMENDATIONS_PAGE = new HelpEntry(
            "recommendations",
            "You will always find 10 personal recommendations below."
                    + " To get a new recommendation, hover over your least favorite one and click \"hide\"."
                    + " You can customize your recommendations in the settings.",
            "right-below");

    static String buildHtml(String title, String endpoint, String loadingText, HelpEntry rLinkHelp) {
        return SHARED_HEAD
                .replace("TITLE", "<title>" + title + "</title>")
                .replace("INITIAL_PRELOAD", "<link rel=\"preload\" href=\"" + endpoint + "\" as=\"fetch\" crossorigin>")
                .replace(
                        "TABLE_CONTENT",
                        "<span hx-get=\"" + endpoint
                                + "\" hx-trigger=\"load\" hx-request='{\"noHeaders\": true}' hx-swap=\"outerHTML\">"
                                + loadingText + "</span>")
                .replace("HELP_EXTERNAL_LINKS", HELP_EXTERNAL_LINKS.toString())
                .replace("R_LINK_HELP", rLinkHelp.toString());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String queryString = req.getQueryString();
        String html = buildHtml(
                "ppaddict v2",
                "/htmx/beatmaps/initial" + (queryString != null ? "?" + queryString : ""),
                "Loading beatmaps...",
                R_LINK_HELP_MAIN_PAGE);
        resp.getOutputStream().write(html.getBytes(StandardCharsets.UTF_8));
    }
}

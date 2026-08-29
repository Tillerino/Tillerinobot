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
        <body>
            <div class="header-bar">
                <h1><a href="v2.html">ppaddict</a> + <a href="r.html">!r</a></h1>
                <!-- noHeaders so that the preloaded requests can be matched. use a nested replace to avoid inheritance of hx-request. -->
                <div id="user-area">
                    <span hx-get="/htmx/user" hx-trigger="load" hx-request='{"noHeaders": true}' hx-swap="outerHTML">Loading...</span>
                </div>
            </div>
            <div class="table-container">
                TABLE_CONTENT
            </div>
        </body>
        </html>
        """;

    static String buildHtml(String title, String endpoint, String loadingText) {
        return SHARED_HEAD
                .replace("TITLE", "<title>" + title + "</title>")
                .replace("INITIAL_PRELOAD", "<link rel=\"preload\" href=\"" + endpoint + "\" as=\"fetch\" crossorigin>")
                .replace(
                        "TABLE_CONTENT",
                        "<span hx-get=\"" + endpoint
                                + "\" hx-trigger=\"load\" hx-request='{\"noHeaders\": true}' hx-swap=\"outerHTML\">"
                                + loadingText + "</span>");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String queryString = req.getQueryString();
        String html = buildHtml(
                "ppaddict v2",
                "/htmx/beatmaps/initial" + (queryString != null ? "?" + queryString : ""),
                "Loading beatmaps...");
        resp.getOutputStream().write(html.getBytes(StandardCharsets.UTF_8));
    }
}

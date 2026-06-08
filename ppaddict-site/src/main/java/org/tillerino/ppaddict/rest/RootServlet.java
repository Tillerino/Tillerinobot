package org.tillerino.ppaddict.rest;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RootServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getOutputStream().write("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>ppaddict v2</title>
                <link rel="stylesheet" href="styles.css">
                <link rel="preload" href="/Cabin-SemiBold.ttf" as="font" type="font/ttf" crossorigin>
                <link rel="preload" href="/htmx/user" as="fetch" crossorigin>
                <link rel="preload" href="/htmx/beatmaps/initial" as="fetch" crossorigin>
                <script src="htmx-2.0.10.min.js"></script>
                <script src="code.js"></script>
            </head>
            <body>
                <div class="header-bar">
                    <h1>ppaddict+!r</h1>
                    <!-- noHeaders so that the preloaded requests can be matched. use a nested replace to avoid inheritance of hx-request. -->
                    <div id="user-area">
                        <span hx-get="/htmx/user" hx-trigger="load" hx-request='{"noHeaders": true}' hx-swap="outerHTML">Loading...</span>
                    </div>
                </div>
                <div class="table-container">
                    <span  hx-get="/htmx/beatmaps/initial" hx-trigger="load" hx-request='{"noHeaders": true}' hx-swap="outerHTML">Loading beatmaps...</span>
                </div>
            </body>
            </html>
            """.getBytes(StandardCharsets.UTF_8));
    }
}

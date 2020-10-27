package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.util.List;
import java.util.Optional;

public class CookieHandler implements HttpHandler {
    private static final String SESSION_COOKIE_NAME = "sessionId";

    int counter = 0;
    CookieHelper cookieHelper = new CookieHelper();

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        counter++;
        String response = "Page was visited: " + counter + " times!";

        Optional<HttpCookie> cookie = getSessionIdCookie(httpExchange);

        boolean isNewSession;
        if (cookie.isPresent()) {  // Cookie already exists
            isNewSession = false;
        } else { // Create a new cookie
            isNewSession = true;
            String sessionId = String.valueOf(counter); // This isn't a good way to create sessionId. Find a better one!
            cookie = Optional.of(new HttpCookie(SESSION_COOKIE_NAME, sessionId));
            httpExchange.getResponseHeaders().add("Set-Cookie", cookie.get().toString());
        }

        response += "\n isNewSession: " + isNewSession;
        response += "\n session id: " + cookie.get().getValue();

        sendResponse(httpExchange, response);
    }

    Optional<HttpCookie> getSessionIdCookie(HttpExchange httpExchange){
        String cookieStr = httpExchange.getRequestHeaders().getFirst("Cookie");
        List<HttpCookie> cookies = cookieHelper.parseCookies(cookieStr);
        return cookieHelper.findCookieByName(SESSION_COOKIE_NAME, cookies);
    }

    public void sendResponse(HttpExchange httpExchange, String response) throws IOException {
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }


}

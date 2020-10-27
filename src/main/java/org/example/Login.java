package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.*;
import java.net.HttpCookie;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Login implements HttpHandler {
    private static final String SESSION_COOKIE_NAME = "sessionId";
    private static int counter = 0;
    private final CookieHelper cookieHelper;
    private final CookieHandler cookieHandler;
    private final Database database;

    public Login(Database database) {
        this.database = database;
        this.cookieHandler = new CookieHandler();
        this.cookieHelper = new CookieHelper();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        if (method.equals("POST")) {
            postForm(httpExchange);
        } else if (method.equals("GET")) {
            handleGet(httpExchange);
        }
    }

    private void handleGet(HttpExchange httpExchange) throws IOException {
        Optional<HttpCookie> optionalCookie = cookieHandler.getSessionIdCookie(httpExchange);
        if (optionalCookie.isPresent()) {
            String value = optionalCookie.get().getValue().replace("\"", "");
            int sessionId = Integer.parseInt(value);
            User user = database.getUserBySessionId(sessionId);
            moveToLoggedPage(httpExchange, user);
        } else {
            String response;
            JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/loginPage.twig");
            JtwigModel model = JtwigModel.newModel();
            response = template.render(model);
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private boolean checkUser(Map<String, String> inputs, Database database) {
        String providedName = inputs.get("username");
        String providedPassword = inputs.get("password");
        User user = database.getUserByProvidedName(providedName);
        System.out.println(providedName);
        return (user != null) && user.getPassword().equals(providedPassword);
    }

    private void postForm(HttpExchange httpExchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String formData = br.readLine();
        Map<String, String> inputs = parseFormData(formData);
        if (!checkUser(inputs, database)) {
            String response = "try again";
            cookieHandler.sendResponse(httpExchange, response);
            return;
        }
        String sessionId = String.valueOf(counter);
        cookieHelper.createCookie(httpExchange, SESSION_COOKIE_NAME, sessionId);
        User user = database.getUserByProvidedName(inputs.get("username"));
        database.getSessionUserMap().put(counter, user);
        moveToLoggedPage(httpExchange, user);
        counter++;
    }

    private void moveToLoggedPage(HttpExchange httpExchange, User user) throws IOException {
        String response;
        JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/loggedPage.twig");
        JtwigModel model = JtwigModel.newModel();
        model.with("username", user.getName());
        response = template.render(model);
        cookieHandler.sendResponse(httpExchange, response);
    }

    private static Map<String, String> parseFormData(String formData) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            String key = URLDecoder.decode(keyValue[0], "UTF-8");
            String value = URLDecoder.decode(keyValue[1], "UTF-8");
            map.put(key, value);
        }
        return map;
    }
}

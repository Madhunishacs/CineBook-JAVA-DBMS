package com.cinebook;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class Json {
    public static final Gson GSON = new Gson();
    private Json() {}
    public static void send(HttpServletResponse resp, int status, Object value) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(GSON.toJson(value));
    }
    public static void error(HttpServletResponse resp, int status, String message) throws IOException {
        send(resp, status, java.util.Map.of("error", message));
    }
}

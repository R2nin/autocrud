package br.edu.autocrud.http;

import br.edu.autocrud.core.*;
import com.sun.net.httpserver.*;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

public class CrudHttpHandler implements HttpHandler {

    private final CrudRepository<?> repo;

    public CrudHttpHandler(CrudRepository<?> repo) {
        this.repo = repo;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {

        Headers h = ex.getResponseHeaders();
        h.add("Access-Control-Allow-Origin",  "*");
        h.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        h.add("Access-Control-Allow-Headers", "Content-Type");
        h.add("Content-Type", "application/json; charset=UTF-8");

        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            send(ex, 204, "");
            return;
        }

        String method = ex.getRequestMethod().toUpperCase();
        String path   = ex.getRequestURI().getPath();
        Long   id     = extractId(path);

        try {
            switch (method) {
                case "GET"    -> handleGet(ex, id);
                case "POST"   -> handlePost(ex);
                case "PUT"    -> handlePut(ex, id);
                case "DELETE" -> handleDelete(ex, id);
                default       -> send(ex, 405, error("Método não permitido"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            send(ex, 500, error(e.getMessage()));
        }
    }

    private void handleGet(HttpExchange ex, Long id) throws IOException, SQLException {
        if (id != null) {
            Optional<Map<String, Object>> row = repo.findById(id);
            if (row.isPresent()) send(ex, 200, JsonUtil.toJson(row.get()));
            else                  send(ex, 404, error("Não encontrado"));
        } else {
            send(ex, 200, JsonUtil.toJson(repo.findAll()));
        }
    }

    private void handlePost(HttpExchange ex) throws IOException, SQLException {
        Map<String, Object> data = readBody(ex);
        long newId = repo.insert(data);
        send(ex, 201, JsonUtil.toJson(Map.of("id", newId)));
    }

    private void handlePut(HttpExchange ex, Long id) throws IOException, SQLException {
        if (id == null) { send(ex, 400, error("ID obrigatório")); return; }
        Map<String, Object> data = readBody(ex);
        boolean ok = repo.update(id, data);
        send(ex, ok ? 200 : 404, ok ? JsonUtil.toJson(Map.of("id", id)) : error("Não encontrado"));
    }

    private void handleDelete(HttpExchange ex, Long id) throws IOException, SQLException {
        if (id == null) { send(ex, 400, error("ID obrigatório")); return; }
        boolean ok = repo.delete(id);
        send(ex, ok ? 204 : 404, ok ? "" : error("Não encontrado"));
    }

    private Map<String, Object> readBody(HttpExchange ex) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return JsonUtil.fromJson(body.isBlank() ? "{}" : body);
    }

    private void send(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, bytes.length == 0 ? -1 : bytes.length);
        if (bytes.length > 0) {
            try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
        } else {
            ex.getResponseBody().close();
        }
    }

    private String error(String msg) {
        return "{\"error\":\"" + (msg != null ? msg.replace("\"","'") : "erro") + "\"}";
    }

    private Long extractId(String path) {
        String[] parts = path.split("/");
        try { return Long.parseLong(parts[parts.length - 1]); }
        catch (NumberFormatException e) { return null; }
    }
}

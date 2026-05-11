package br.edu.autocrud.http;

import br.edu.autocrud.core.*;
import com.sun.net.httpserver.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

public class AutoCrudServer {

    private final int            port;
    private final List<EntityMetadata> entities;
    private final Database       db;
    private HttpServer           server;

    public AutoCrudServer(int port, List<EntityMetadata> entities, Database db) {
        this.port     = port;
        this.entities = entities;
        this.db       = db;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new UiHandler(entities));

        server.createContext("/api/meta", exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin",  "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.getResponseBody().close();
                return;
            }
            var list = entities.stream().map(m -> {
                var cols = m.getColumnsSorted().stream().map(c -> {
                    var col = new java.util.LinkedHashMap<String, Object>();
                    col.put("javaName",    c.javaName());
                    col.put("columnName",  c.columnName());
                    col.put("label",       c.label());
                    col.put("sqlType",     c.sqlType());
                    col.put("required",    c.required());
                    col.put("minLength",   c.minLength());
                    col.put("maxLength",   c.maxLength());
                    col.put("min",         c.min());
                    col.put("max",         c.max());
                    col.put("pattern",     c.pattern());
                    col.put("errorMsg",    c.errorMsg());
                    col.put("placeholder", c.placeholder());
                    col.put("mask",        c.mask());
                    col.put("order",       c.order());
                    return col;
                }).toList();
                var ent = new java.util.LinkedHashMap<String, Object>();
                ent.put("label",   m.getLabel());
                ent.put("apiPath", m.getApiPath());
                ent.put("columns", cols);
                return ent;
            }).toList();

            byte[] body = br.edu.autocrud.core.JsonUtil.toJson(list)
                    .getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.getResponseBody().close();
        });

        server.createContext("/api/_tools/new-entity", exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin",  "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.getResponseBody().close();
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"M\\u00e9todo n\\u00e3o permitido\"}");
                return;
            }
            try {
                String raw = new String(exchange.getRequestBody().readAllBytes(),
                        java.nio.charset.StandardCharsets.UTF_8);
                java.util.Map<String, Object> body =
                        br.edu.autocrud.core.JsonUtil.fromJson(raw.isBlank() ? "{}" : raw);

                String className = String.valueOf(body.getOrDefault("className", "")).trim();
                String label     = String.valueOf(body.getOrDefault("label", "")).trim();

                if (className.isEmpty() || !className.matches("[A-Z][a-zA-Z0-9]*")) {
                    sendJson(exchange, 400,
                        "{\"error\":\"Nome da classe inv\\u00e1lido. Use PascalCase, ex: MinhaEntidade\"}");
                    return;
                }

                @SuppressWarnings("unchecked")
                java.util.List<java.util.Map<String, Object>> fields =
                        (java.util.List<java.util.Map<String, Object>>) body.get("fields");

                if (fields == null || fields.isEmpty()) {
                    sendJson(exchange, 400, "{\"error\":\"Adicione ao menos um campo\"}");
                    return;
                }

                String source = br.edu.autocrud.core.EntityFileWriter.generateSource(
                        className, label.isEmpty() ? className : label, fields);
                br.edu.autocrud.core.EntityFileWriter.write(className, source);

                String filePath = "src/entities/" + className + ".java";
                sendJson(exchange, 200, "{\"ok\":true,\"file\":\"" + filePath + "\"}");

            } catch (Exception e) {
                e.printStackTrace();
                String msg = e.getMessage() != null ? e.getMessage().replace("\"", "'") : "Erro interno";
                sendJson(exchange, 500, "{\"error\":\"" + msg + "\"}");
            }
        });

        for (EntityMetadata meta : entities) {
            CrudRepository<?> repo = new CrudRepository<>(meta, db);
            String path = meta.getApiPath();
            server.createContext(path, new CrudHttpHandler(repo));
            System.out.println("[AutoCrud] Endpoint: " + path);
        }

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("[AutoCrud] Servidor rodando em http://localhost:" + port);
    }

    public void stop() {
        if (server != null) server.stop(1);
        db.shutdown();
    }

    private static void sendJson(com.sun.net.httpserver.HttpExchange ex, int code, String body)
            throws IOException {
        byte[] bytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, bytes.length);
        try (var os = ex.getResponseBody()) { os.write(bytes); }
    }
}

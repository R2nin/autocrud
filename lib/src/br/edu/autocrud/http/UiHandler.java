package br.edu.autocrud.http;

import br.edu.autocrud.core.EntityMetadata;
import br.edu.autocrud.ui.UiGenerator;
import com.sun.net.httpserver.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class UiHandler implements HttpHandler {

    private final String html;

    public UiHandler(List<EntityMetadata> entities) {
        this.html = UiGenerator.generate(entities);
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }
}

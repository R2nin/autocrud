package br.edu.autocrud.core;

import com.fasterxml.jackson.databind.*;

import java.io.IOException;
import java.util.Map;

public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    private JsonUtil() {}

    public static String toJson(Object obj) {
        try { return MAPPER.writeValueAsString(obj); }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> fromJson(String json) {
        try { return MAPPER.readValue(json, Map.class); }
        catch (IOException e) { throw new RuntimeException("JSON inválido: " + e.getMessage(), e); }
    }
}

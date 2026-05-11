package br.edu.autocrud.core;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public final class EntityFileWriter {

    public static String generateSource(String className, String label,
                                        List<Map<String, Object>> fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("package entities;\n\n");

        Set<String> extra = new LinkedHashSet<>();
        for (Map<String, Object> f : fields) {
            switch (str(f, "type")) {
                case "BigDecimal"    -> extra.add("java.math.BigDecimal");
                case "LocalDate"     -> extra.add("java.time.LocalDate");
                case "LocalDateTime" -> extra.add("java.time.LocalDateTime");
            }
        }

        sb.append("import br.edu.autocrud.annotations.Column;\n");
        sb.append("import br.edu.autocrud.annotations.Entity;\n");
        for (String imp : extra) sb.append("import ").append(imp).append(";\n");

        sb.append("\n@Entity(label = \"").append(esc(label)).append("\")\n");
        sb.append("public class ").append(className).append(" {\n");

        for (int i = 0; i < fields.size(); i++) {
            Map<String, Object> f = fields.get(i);
            String name        = str(f, "name").trim();
            String type        = str(f, "type");
            String fieldLabel  = str(f, "label");
            boolean required   = bool(f, "required");
            int minLength      = intVal(f, "minLength");
            int maxLength      = intVal(f, "maxLength");
            String min         = str(f, "min");
            String max         = str(f, "max");
            String placeholder = str(f, "placeholder");
            String mask        = str(f, "mask");
            String pattern     = str(f, "pattern");
            String errorMsg    = str(f, "errorMsg");
            String sqlType     = str(f, "sqlType");
            if (type.isEmpty()) type = "String";

            sb.append("\n    @Column(label = \"").append(esc(fieldLabel)).append("\"");
            sb.append(", order = ").append(i + 1);
            if (!sqlType.isEmpty())     sb.append(", sqlType = \"").append(esc(sqlType)).append("\"");
            if (required)               sb.append(", required = true");
            if (minLength > 0)          sb.append(", minLength = ").append(minLength);
            if (maxLength > 0)          sb.append(", maxLength = ").append(maxLength);
            if (!min.isEmpty())         sb.append(", min = \"").append(esc(min)).append("\"");
            if (!max.isEmpty())         sb.append(", max = \"").append(esc(max)).append("\"");
            if (!placeholder.isEmpty()) sb.append(", placeholder = \"").append(esc(placeholder)).append("\"");
            if (!mask.isEmpty())        sb.append(", mask = \"").append(esc(mask)).append("\"");
            if (!pattern.isEmpty())     sb.append(", pattern = \"").append(esc(pattern)).append("\"");
            if (!errorMsg.isEmpty())    sb.append(", errorMsg = \"").append(esc(errorMsg)).append("\"");
            sb.append(")\n");
            sb.append("    private ").append(type).append(" ").append(name).append(";\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    public static void write(String className, String source) throws IOException {
        File dir = resolveEntityDir();
        Files.writeString(new File(dir, className + ".java").toPath(),
                source, StandardCharsets.UTF_8);
    }

    public static File resolveEntityDir() {
        for (String path : new String[]{"src/entities", "../src/entities"}) {
            File d = new File(path);
            if (d.exists()) return d;
        }
        File d = new File("src/entities");
        d.mkdirs();
        return d;
    }

    private static String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? "" : String.valueOf(v).trim();
    }

    private static boolean bool(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return Boolean.TRUE.equals(v) || "true".equalsIgnoreCase(String.valueOf(v));
    }

    private static int intVal(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return 0;
        try { return (int) Math.round(Double.parseDouble(String.valueOf(v))); }
        catch (NumberFormatException e) { return 0; }
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

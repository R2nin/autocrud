package br.edu.autocrud.core;

import br.edu.autocrud.core.EntityMetadata.ColumnMetadata;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class CrudRepository<T> {

    protected final EntityMetadata meta;
    protected final Database       db;

    public CrudRepository(EntityMetadata meta, Database db) {
        this.meta = meta;
        this.db   = db;
    }

    public List<Map<String, Object>> findAll() throws SQLException {
        String sql = "SELECT * FROM " + meta.getTableName() + " ORDER BY ID DESC";
        return executeQuery(sql);
    }

    public Optional<Map<String, Object>> findById(long id) throws SQLException {
        String sql = "SELECT * FROM " + meta.getTableName() + " WHERE ID = ?";
        List<Map<String, Object>> rows = executeQuery(sql, id);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public long insert(Map<String, Object> data) throws SQLException {
        List<ColumnMetadata> cols = meta.getColumns();
        String colNames = cols.stream()
                .map(c -> c.columnName().toUpperCase())
                .collect(Collectors.joining(", "));
        String placeholders = cols.stream().map(c -> "?")
                .collect(Collectors.joining(", "));

        String sql = "INSERT INTO " + meta.getTableName()
                + " (" + colNames + ") VALUES (" + placeholders + ")";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bindColumns(ps, cols, data);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : -1;
            }
        }
    }

    public boolean update(long id, Map<String, Object> data) throws SQLException {
        List<ColumnMetadata> cols = meta.getColumns();
        String setClauses = cols.stream()
                .map(c -> c.columnName().toUpperCase() + " = ?")
                .collect(Collectors.joining(", "));

        String sql = "UPDATE " + meta.getTableName()
                + " SET " + setClauses + " WHERE ID = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindColumns(ps, cols, data);
            ps.setLong(cols.size() + 1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM " + meta.getTableName() + " WHERE ID = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Map<String, Object>> executeCustomQuery(String sql, Object... params)
            throws SQLException {
        return executeQuery(sql, params);
    }

    private List<Map<String, Object>> executeQuery(String sql, Object... params)
            throws SQLException {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++)
                ps.setObject(i + 1, params[i]);

            try (ResultSet rs = ps.executeQuery()) {
                return mapResultSet(rs);
            }
        }
    }

    private List<Map<String, Object>> mapResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData rsMeta = rs.getMetaData();
        int colCount = rsMeta.getColumnCount();
        List<Map<String, Object>> rows = new ArrayList<>();

        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", rs.getLong("ID"));
            for (int i = 1; i <= colCount; i++) {
                String colLabel = rsMeta.getColumnLabel(i);
                if ("ID".equalsIgnoreCase(colLabel)) continue;
                row.put(colLabel.toLowerCase(), rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }

    private void bindColumns(PreparedStatement ps, List<ColumnMetadata> cols,
                              Map<String, Object> data) throws SQLException {
        for (int i = 0; i < cols.size(); i++) {
            ColumnMetadata col = cols.get(i);
            Object value = data.get(col.javaName());
            if (value == null) value = data.get(col.columnName());
            if (value == null) value = data.get(col.columnName().toUpperCase());

            value = coerce(value, col.field());
            ps.setObject(i + 1, value);
        }
    }

    private Object coerce(Object value, Field field) {
        if (value == null) return null;
        Class<?> target = field.getType();
        String   str    = value.toString();
        try {
            if (target == int.class    || target == Integer.class)   return Integer.parseInt(str);
            if (target == long.class   || target == Long.class)      return Long.parseLong(str);
            if (target == double.class || target == Double.class)    return Double.parseDouble(str);
            if (target == float.class  || target == Float.class)     return Float.parseFloat(str);
            if (target == boolean.class|| target == Boolean.class)   return Boolean.parseBoolean(str);
            if (target == java.math.BigDecimal.class) return new java.math.BigDecimal(str);
            if (target == java.time.LocalDate.class)
                return java.time.LocalDate.parse(str);
            if (target == java.time.LocalDateTime.class)
                return java.time.LocalDateTime.parse(str);
        } catch (Exception e) { return null; }
        return value;
    }
}

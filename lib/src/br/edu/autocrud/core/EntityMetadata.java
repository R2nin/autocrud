package br.edu.autocrud.core;

import br.edu.autocrud.annotations.Column;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class EntityMetadata {

    private final Class<?>  entityClass;
    private final String    tableName;
    private final String    label;
    private final String    apiPath;
    private final List<ColumnMetadata> columns = new ArrayList<>();

    public EntityMetadata(Class<?> entityClass, String tableName, String label) {
        this.entityClass = entityClass;
        this.tableName   = tableName.toUpperCase();
        this.label       = label.isBlank() ? entityClass.getSimpleName() : label;
        this.apiPath     = "/api/" + entityClass.getSimpleName().toLowerCase();
    }

    public void addColumn(ColumnMetadata col) { columns.add(col); }

    public Class<?>            getEntityClass() { return entityClass; }
    public String              getTableName()   { return tableName; }
    public String              getLabel()       { return label; }
    public String              getApiPath()     { return apiPath; }
    public List<ColumnMetadata> getColumns()    { return Collections.unmodifiableList(columns); }

    public List<ColumnMetadata> getColumnsSorted() {
        return columns.stream()
                .sorted(Comparator.comparingInt(ColumnMetadata::order))
                .toList();
    }

    public record ColumnMetadata(
            Field  field,
            String javaName,
            String columnName,
            String label,
            String sqlType,
            int    length,
            boolean nullable,
            int    order,

            boolean required,
            int     minLength,
            int     maxLength,
            String  min,
            String  max,
            String  pattern,
            String  errorMsg,
            String  placeholder,
            String  mask
    ) {

        public static String inferSqlType(Field field, String override, int length) {
            if (!override.isBlank()) return override.toUpperCase();
            Class<?> type = field.getType();
            if (type == String.class)          return "VARCHAR(" + length + ")";
            if (type == int.class    || type == Integer.class)   return "INT";
            if (type == long.class   || type == Long.class)      return "BIGINT";
            if (type == double.class || type == Double.class)    return "DOUBLE";
            if (type == float.class  || type == Float.class)     return "FLOAT";
            if (type == boolean.class|| type == Boolean.class)   return "BOOLEAN";
            if (type == BigDecimal.class)      return "DECIMAL(19,2)";
            if (type == LocalDate.class)       return "DATE";
            if (type == LocalDateTime.class)   return "TIMESTAMP";
            return "VARCHAR(255)";
        }

        public Object convertFromResultSet(java.sql.ResultSet rs) throws java.sql.SQLException {
            Class<?> type = field.getType();
            String  name  = columnName;
            if (type == String.class)          return rs.getString(name);
            if (type == int.class    || type == Integer.class)   return rs.getInt(name);
            if (type == long.class   || type == Long.class)      return rs.getLong(name);
            if (type == double.class || type == Double.class)    return rs.getDouble(name);
            if (type == float.class  || type == Float.class)     return rs.getFloat(name);
            if (type == boolean.class|| type == Boolean.class)   return rs.getBoolean(name);
            if (type == BigDecimal.class)      return rs.getBigDecimal(name);
            if (type == LocalDate.class) {
                var d = rs.getDate(name);
                return d != null ? d.toLocalDate() : null;
            }
            if (type == LocalDateTime.class) {
                var ts = rs.getTimestamp(name);
                return ts != null ? ts.toLocalDateTime() : null;
            }
            return rs.getString(name);
        }

        public static String inferLabel(String fieldName, String annotationLabel) {
            if (!annotationLabel.isBlank()) return annotationLabel;

            String spaced = fieldName.replaceAll("([a-z])([A-Z])", "$1 $2");
            return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
        }

        public static String toSnakeCase(String name) {
            return name.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        }

        public static ColumnMetadata from(Field field, Column ann) {
            String colName  = toSnakeCase(field.getName());
            String sqlType  = inferSqlType(field, ann.sqlType(), ann.length());
            String label    = inferLabel(field.getName(), ann.label());
            return new ColumnMetadata(field, field.getName(), colName,
                    label, sqlType, ann.length(), ann.nullable(), ann.order(),
                    ann.required(), ann.minLength(), ann.maxLength(),
                    ann.min(), ann.max(), ann.pattern(), ann.errorMsg(), ann.placeholder(), ann.mask());
        }
    }
}

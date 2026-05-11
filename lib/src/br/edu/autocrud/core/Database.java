package br.edu.autocrud.core;

import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.*;
import java.util.List;

public class Database {

    private final JdbcConnectionPool pool;

    public Database(AppConfig config) {
        pool = JdbcConnectionPool.create(config.dbUrl(), config.dbUser(), config.dbPassword());
        pool.setMaxConnections(config.dbMaxConnections());
    }

    public Connection getConnection() throws SQLException {
        return pool.getConnection();
    }

    public void createTableIfNotExists(EntityMetadata meta) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ").append(meta.getTableName()).append(" (\n");
        sql.append("  ID BIGINT AUTO_INCREMENT PRIMARY KEY");

        for (EntityMetadata.ColumnMetadata col : meta.getColumns()) {
            sql.append(",\n  ").append(col.columnName().toUpperCase())
               .append(" ").append(col.sqlType());
            if (!col.nullable()) sql.append(" NOT NULL");
        }
        sql.append("\n)");

        try (Connection conn = getConnection();
             Statement  stmt = conn.createStatement()) {
            stmt.execute(sql.toString());
            System.out.println("[AutoCrud] Tabela pronta: " + meta.getTableName());
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela " + meta.getTableName(), e);
        }
    }

    public void createTables(List<EntityMetadata> metas) {
        metas.forEach(this::createTableIfNotExists);
    }

    public void shutdown() {
        pool.dispose();
    }
}

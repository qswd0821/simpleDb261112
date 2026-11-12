package com.ll.simpleDb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SimpleDb {
    private final String url;
    private final String username;
    private final String password;
    private boolean devMode;

    // 스레드마다 자기 커넥션을 쓴다 (트랜잭션도 스레드 단위)
    private final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    public SimpleDb(String host, String username, String password, String dbName) {
        this.url = "jdbc:mysql://%s:3306/%s?characterEncoding=UTF-8".formatted(host, dbName);
        this.username = username;
        this.password = password;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    public Sql genSql() {
        return new Sql(this);
    }

    public int run(String sql, Object... params) {
        return executeUpdate(sql, Arrays.asList(params));
    }

    public void startTransaction() {
        try {
            getConnection().setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void commit() {
        try {
            Connection connection = getConnection();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void rollback() {
        try {
            Connection connection = getConnection();
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        Connection connection = connectionHolder.get();
        connectionHolder.remove();

        if (connection == null) return;

        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    int executeUpdate(String sql, List<Object> params) {
        try (PreparedStatement stmt = prepare(sql, params, false)) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    long executeInsert(String sql, List<Object> params) {
        try (PreparedStatement stmt = prepare(sql, params, true)) {
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    List<Map<String, Object>> executeSelect(String sql, List<Object> params) {
        try (PreparedStatement stmt = prepare(sql, params, false);
             ResultSet rs = stmt.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            List<Map<String, Object>> rows = new ArrayList<>();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();

                for (int i = 1; i <= columnCount; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }

                rows.add(row);
            }

            return rows;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement prepare(String sql, List<Object> params, boolean returnKeys) throws SQLException {
        if (devMode) {
            System.out.println("== rawSql ==");
            System.out.println(sql.strip());
            System.out.println("== params == " + params);
        }

        PreparedStatement stmt = returnKeys
                ? getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                : getConnection().prepareStatement(sql);

        for (int i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }

        return stmt;
    }

    private Connection getConnection() throws SQLException {
        Connection connection = connectionHolder.get();

        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, username, password);
            connectionHolder.set(connection);
        }

        return connection;
    }
}

package com.ll.simpleDb;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Sql {
    private final SimpleDb simpleDb;
    private final StringBuilder sqlBuilder = new StringBuilder();
    private final List<Object> params = new ArrayList<>();

    Sql(SimpleDb simpleDb) {
        this.simpleDb = simpleDb;
    }

    public Sql append(String sqlPart, Object... params) {
        sqlBuilder.append(' ').append(sqlPart);
        Collections.addAll(this.params, params);
        return this;
    }

    // "IN (?)" 의 ? 하나를 값 개수만큼 "?, ?, ?" 로 펼친다
    public Sql appendIn(String sqlPart, Object... params) {
        String placeholders = String.join(", ", Collections.nCopies(params.length, "?"));
        return append(sqlPart.replace("?", placeholders), params);
    }

    public long insert() {
        return simpleDb.executeInsert(getSql(), params);
    }

    public int update() {
        return simpleDb.executeUpdate(getSql(), params);
    }

    public int delete() {
        return simpleDb.executeUpdate(getSql(), params);
    }

    public List<Map<String, Object>> selectRows() {
        return simpleDb.executeSelect(getSql(), params);
    }

    public Map<String, Object> selectRow() {
        List<Map<String, Object>> rows = selectRows();
        return rows.isEmpty() ? null : rows.getFirst();
    }

    public LocalDateTime selectDatetime() {
        return (LocalDateTime) selectValue();
    }

    public Long selectLong() {
        Object value = selectValue();
        return value == null ? null : ((Number) value).longValue();
    }

    public List<Long> selectLongs() {
        return selectRows().stream()
                .map(row -> ((Number) row.values().iterator().next()).longValue())
                .toList();
    }

    public String selectString() {
        Object value = selectValue();
        return value == null ? null : value.toString();
    }

    // BIT(1) 은 Boolean, "1 = 1" 같은 식은 숫자로 온다
    public Boolean selectBoolean() {
        Object value = selectValue();

        if (value == null) return null;
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return n.longValue() != 0;

        throw new IllegalStateException("boolean 으로 바꿀 수 없는 값: " + value);
    }

    private Object selectValue() {
        Map<String, Object> row = selectRow();
        return row == null ? null : row.values().iterator().next();
    }

    private String getSql() {
        return sqlBuilder.toString().strip();
    }
}

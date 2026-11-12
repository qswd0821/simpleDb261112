package com.ll.simpleDb;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Map;

// 조회 결과 행(Map)을 클래스 객체로 바꾼다.
// 컬럼 이름과 필드 이름을 그대로 맞추려고 getter/setter 대신 필드로 매핑한다
// (예: 컬럼 isBlind → 필드 isBlind).
final class RowMapper {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.ALL, Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

    private RowMapper() {
    }

    static <T> T map(Map<String, Object> row, Class<T> cls) {
        return MAPPER.convertValue(row, cls);
    }
}

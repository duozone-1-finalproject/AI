package com.example.demo.jsonschema;

import com.example.demo.constants.KeywordContants;

import java.util.stream.Collectors;

public final class SearchSchemas {
    private SearchSchemas() {}

    // ✅ 섹션별 스키마 (허용 키워드 enum 주입)
    public static final String BUSINESS = build(KeywordContants.BUS_KWD); // 사업 위험
    public static final String COMPANY  = build(KeywordContants.COM_KWD); // 회사 위험

    private static String build(java.util.List<String> allowed) {
        String enums = allowed.stream()
                .map(k -> "\"" + k + "\"")
                .collect(Collectors.joining(","));

        // 루트가 배열이고, 각 원소가 {keyword, candidates[]} 형태
        // URL/날짜는 format 대신 pattern 사용 (호환성 ↑)
        return """
        {
         "type": "array",
          "minItems": 1,
          "items": {
            "type": "object",
            "additionalProperties": false,
            "properties": {
              "keyword": { "type": "string", "enum": [%s] },
              "candidates": {
                "type": "array",
                "minItems": 3,
                "items": {
                  "type": "object",
                  "additionalProperties": false,
                  "properties": {
                    "title":  { "type": "string", "minLength": 1 },
                    "url":    { "type": "string", "pattern": "^https?://[^\\\\s]+$" },
                    "source": { "type": "string", "enum": ["뉴스"] },
                    "date":   { "type": "string", "pattern": "^[0-9]{4}-[0-9]{2}-[0-9]{2}$" }
                  },
                  "required": ["title","url","source","date"]
                }
              }
            },
            "required": ["keyword","candidates"]
        }
        """.formatted(enums);
    }
}


//enum은 특정한 키워드만 들어가도록 하는 제약 조건

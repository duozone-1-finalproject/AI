package com.example.demo.jsonschema;

import com.example.demo.constants.KeywordContants;
import java.util.stream.Collectors;

public final class SearchSchemas {
    private SearchSchemas() {}

    // ✅ 팀 공용 상수: 섹션별 JSON 스키마 (enum 고정)
    public static final String BUSINESS = build(KeywordContants.BUS_KWD); // 회사 위험용
    public static final String COMPANY  = build(KeywordContants.COM_KWD);  // 사업 위험용

    private static String build(java.util.List<String> allowed) {
        String enums = allowed.stream()
                .map(k -> "\"" + k + "\"")
                .collect(Collectors.joining(","));
        // 텍스트 블록에서도 \\d → 자바 이스케이프 때문에 \\\\d 로 적어야 JSON에 \\d가 들어갑니다.
        return """
        {
          "type":"object",
          "additionalProperties": false,
          "properties":{
            "articles":{
              "type":"array",
              "items":{
                "type":"object",
                "additionalProperties": false,
                "properties":{
                  "keyword":{"type":"string","enum":[%s]},
                  "title":{"type":"string","minLength":1},
                  "url":{"type":"string","format":"uri"},
                  "source":{"type":"string"},
                  "date": { "type": "string", "pattern": "^\\\\d{4}-\\\\d{2}-\\\\d{2}$" }
                },
                "required":["keyword","title","url","source"]
              }
            }
          },
          "required":["articles"]
        }
        """.formatted(enums);
    }
}


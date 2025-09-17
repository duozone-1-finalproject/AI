package com.example.demo.constants;

import java.util.List;
import java.util.stream.Collectors;

public final class WebConstants {
    public static final List<String> DOMAINS = List.of(
            "yna.co.kr",
            "chosun.com",
            "donga.com",
            "joongang.co.kr"
    );
    public static final String SEARCH_JSON_SCHEMA = """
            {
              "type": "object",
              "additionalProperties": false,
              "properties": {
                "items": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "additionalProperties": false,
                    "properties": {
                      "keyword": { "type": "string", "minLength": 1 },
                      "candidates": {
                        "type": "array",
                        "items": {
                          "type": "object",
                          "additionalProperties": false,
                          "properties": {
                            "title": { "type": "string", "minLength": 1 },
                            "url": { "type": "string", "pattern": "^https?://\\\\S+$" },
                            "topic": { "type": "string", "const": "news" },
                            "publishedDate": { "type": ["string","null"], "pattern": "^[0-9]{4}-[0-9]{2}-[0-9]{2}$" }
                          },
                          "required": ["title", "url", "topic", "publishedDate"]
                        }
                      }
                    },
                    "required": ["keyword", "candidates"]
                  }
                }
              },
              "required": ["items"]
            }
            """;

    public static final String VALIDATION_JSON_SCHEMA = """
            {
              "type": "object",
              "additionalProperties": false,
              "properties": {
                "reviews": {
                  "type": "array",
                  "minItems": 4,
                  "items": {
                    "type": "object",
                    "additionalProperties": false,
                    "properties": {
                      "title": { "type": "string", "minLength": 1 },
                      "gate": {
                        "type": "object",
                        "additionalProperties": false,
                        "properties": {
                          "lengthOk": { "type": "boolean" },
                          "passed":   { "type": "boolean" }
                        },
                        "required": ["lengthOk", "passed"]
                      },
                      "scores": {
                        "type": "object",
                        "additionalProperties": false,
                        "properties": {
                          "topicFit70": { "type": "integer", "minimum": 0, "maximum": 70 },
                          "source20":   { "type": "integer", "enum": [10, 14, 20] },
                          "adSpam10":   { "type": "integer", "enum": [0, 4, 10] }
                        },
                        "required": ["topicFit70", "source20", "adSpam10"]
                      },
                      "adSpamLevel": { "type": "string", "enum": ["none","soft","strong"] },
                      "sourceTier":  { "type": "string", "enum": ["trustedTextHint","semiTextHint","unknownText"] },
                      "total100":    { "type": "number", "minimum": 0, "maximum": 100 },
                      "decision":    { "type": "string", "enum": ["YES","NO"] },
                      "reasons": {
                        "type": "array",
                        "minItems": 1,
                        "items": { "type": "string", "minLength": 1 }
                      }
                    },
                    "required": ["title","gate","scores","adSpamLevel","sourceTier","total100","decision","reasons"]
                  }
                },
                "best": {
                  "type": "object",
                  "additionalProperties": false,
                  "properties": {
                    "index":    { "type": "integer", "minimum": 0 },
                    "total100": { "type": "number", "minimum": 0, "maximum": 100 },
                    "decision": { "type": "string", "enum": ["YES","NO"] }
                  },
                  "required": ["index","total100","decision"]
                },
                "finalResult": {
                  "type": "object",
                  "additionalProperties": false,
                  "properties": {
                    "validated":   { "type": "boolean" },
                    "contextHint": { "type": "string" }
                  },
                  "required": ["validated","contextHint"]
                }
              },
              "required": ["reviews","best","finalResult"]
            }
            """;

    private WebConstants() {
    }
}

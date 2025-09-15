package com.example.demo.constants;

import java.util.List;
import java.util.stream.Collectors;

public final class WebConstants {
    public static final List<String> DOMAINS = List.of(
            "yna.co.kr",
            "news1.kr",
            "newsis.com",
            "ytn.co.kr",
            "news.kbs.co.kr",
            "imnews.imbc.com",
            "news.sbs.co.kr",
            "jtbc.co.kr",
            "chosun.com",
            "donga.com",
            "joongang.co.kr",
            "hani.co.kr",
            "khan.co.kr",
            "hankookilbo.com",
            "seoul.co.kr",
            "munhwa.com",
            "segye.com",
            "kmib.co.kr",
            "mk.co.kr",
            "hankyung.com",
            "sedaily.com",
            "asiae.co.kr",
            "edaily.co.kr",
            "mt.co.kr",
            "heraldcorp.com",
            "fnnews.com",
            "etnews.com",
            "zdnet.co.kr",
            "ddaily.co.kr",
            "thebell.co.kr",
            "theguru.co.kr",
            "ohmynews.com",
            "pressian.com",
            "newdaily.co.kr"
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

    private WebConstants() {
    }
}

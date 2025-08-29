package com.example.demo.langgraph.nodes;

import java.util.Map;

public class Sub_NewsSearchNode {
    Map<String, String> query = Map.of(
            "engine", "google_news",
            "q", "<위에서 만든 단일 쿼리 문자열>",
            "hl", "ko",
            "gl", "kr",
            "sort_by", "date",
            "num", "5"
            );
//    "engine": "google_news",
//        "q": "<위에서 만든 단일 쿼리 문자열>",
//        "hl": "ko",
//        "gl": "kr",
//        "sort_by": "date",
//        "num": 5
}
// SerpAPI 검색 → 기사 제목/링크 추출 (newsResults)
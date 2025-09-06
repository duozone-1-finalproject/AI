//# Duck 서브그래프 실행 로직
package com.example.demo.graphweb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DuckService {

    // 뉴스 검색 전용
    public Collection<String> searchNews(String query, int limit, String sort) {
        System.out.println("[DuckService] 뉴스 검색: query=" + query +
                ", limit=" + limit + ", sort=" + sort);

        // 실제 구현에서는 DuckDuckGo News API 호출
        List<String> results = new ArrayList<>();
        for (int i = 1; i <= limit; i++) {
            results.add("[News] " + query + " 결과 " + i + " (정렬:" + sort + ")");
        }
        return results;
    }

    // 웹 검색 전용
    public Collection<String> searchWeb(String query, int limit, String sort) {
        System.out.println("[DuckService] 웹 검색: query=" + query +
                ", limit=" + limit + ", sort=" + sort);

        // 실제 구현에서는 DuckDuckGo Web API 호출
        List<String> results = new ArrayList<>();
        for (int i = 1; i <= limit; i++) {
            results.add("[Web] " + query + " 결과 " + i + " (정렬:" + sort + ")");
        }
        return results;
    }
}

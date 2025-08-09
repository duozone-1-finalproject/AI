package com.example.demo.controller;

import com.example.demo.dto.SerpApiResult;
import com.example.demo.dto.SerpApiSearchResponse;
import com.example.demo.service.SerpApiServiceBackUp;
import com.example.demo.util.JsonWriter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;  // ✅ ① 추가

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SerpApiController {

    private final SerpApiServiceBackUp serpApiServiceBackUp;

    @GetMapping("/AIString")
    public SerpApiSearchResponse searchWeb() {
        System.out.println("SerpAPI 웹 검색 애플리케이션이 시작되었습니다.");

        String query = "역할 : 기업  주가 분석가 / 검색 키워드 : 삼성전자 주가 / 응답패턴 : 제목, 링크 JSON 패턴 / 요구사항 : 한글이 깨지지 않게, 리스트는 10개";
        //String query = "삼성전자 주가";

        System.out.println("\n------------------------------------");
        System.out.println("검색어: \"" + query + "\"");
        System.out.println("------------------------------------");

        SerpApiSearchResponse response = serpApiServiceBackUp.search(query);

        if (response != null && response.getNewsResults() != null && !response.getNewsResults().isEmpty()) {

            // ✅ ② 뉴스 10개만 추출
            List<SerpApiResult> limitedResults = response.getNewsResults()
                    .stream()
                    .limit(10)
                    .collect(Collectors.toList());

            System.out.println("\n--- 검색 결과 (최대 10개) ---");
            for (int i = 0; i < limitedResults.size(); i++) {
                SerpApiResult result = limitedResults.get(i);
                System.out.printf("%d. 제목: %s\n", (i + 1), result.getTitle() != null ? result.getTitle() : "제목 없음");
                System.out.printf("   링크: %s\n\n", result.getLink() != null ? result.getLink() : "링크 없음");
            }

            // ✅ ③ JsonWriter에 10개만 전달
            JsonWriter.writeArticlesToFile(limitedResults, query);

        } else {
            System.out.println("❗ 검색 결과가 없습니다.");
        }

        System.out.println("------------------------------------");
        System.out.println("SerpAPI 검색 작업 완료.");

        return response;
    }
}


package com.example.demo;

import com.example.demo.dto.SerpApiResult;
import com.example.demo.dto.SerpApiSearchResponse;
import com.example.demo.service.SerpApiService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.List;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(SerpApiService serpApiService) {
		return args -> {
			System.out.println("SerpAPI 웹 검색 애플리케이션이 시작되었습니다.");

			String query = "삼성전자 주가"; // 원하는 순수한 검색어

			System.out.println("\n------------------------------------");
			System.out.println("검색어: \"" + query + "\"");
			System.out.println("------------------------------------");

			SerpApiSearchResponse response = serpApiService.search(query);
			
			List<SerpApiResult> resultsToDisplay = null;

			if (response != null && response.getNewsResults() != null && !response.getNewsResults().isEmpty()) {
				resultsToDisplay = response.getNewsResults();
				System.out.println("\n--- 뉴스 검색 결과 ---");
			} else {
				System.out.println("❗ 검색 결과가 없습니다. (SerpAPI에서 뉴스 결과를 찾지 못했습니다.)");
			}

			if (resultsToDisplay != null && !resultsToDisplay.isEmpty()) {
				for (int i = 0; i < resultsToDisplay.size(); i++) {
					SerpApiResult result = resultsToDisplay.get(i);
					System.out.printf("%d. 제목: %s\n", (i + 1), result.getTitle() != null ? result.getTitle() : "제목 없음");
					System.out.printf("   요약: %s\n", result.getSnippet() != null ? result.getSnippet() : "요약 없음");
					System.out.printf("   링크: %s\n\n", result.getLink() != null ? result.getLink() : "링크 없음");
				}
			} else {
				System.out.println("❗ 검색 결과가 없습니다. (SerpAPI에서 뉴스 또는 일반 웹 결과를 찾지 못했습니다.)");
			}
			// ------------------------------------

			System.out.println("------------------------------------");
			System.out.println("SerpAPI 검색 작업 완료.");
		};
	}
}
    

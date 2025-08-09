package com.example.demo.service;

import com.example.demo.dto.LlmSearchType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);
    private final ChatClient chatClient;

    public String determineSearchEngine(String userPrompt) {
        var outputParser = new BeanOutputParser<>(LlmSearchType.class);

        String promptString = """
            당신은 사용자의 질문 의도를 분석하여 가장 적합한 검색 엔진을 추천하는 전문가입니다.
            아래 규칙에 따라 사용자의 질문에 가장 적합한 엔진 하나를 JSON 형식으로 추천해주세요.

            - 주가, 환율, 최신 동향 등 시의성 있는 정보: "google_news"
            - 특정 기업의 개요, 역사, 제품 등 일반 정보: "google"
            - 특정 주제에 대한 심층 분석 보고서, 논문, 공식 문서: "naver"

            사용자 질문: "{userPrompt}"

            너의 응답은 반드시 다음 JSON 형식이어야만 해:
            {format}
            """;

        try {
            PromptTemplate promptTemplate = new PromptTemplate(promptString, Map.of(
                    "userPrompt", userPrompt,
                    "format", outputParser.getFormat()
            ));
            Prompt prompt = promptTemplate.create();
            ChatResponse response = chatClient.call(prompt);
            return outputParser.parse(response.getResult().getOutput().getContent()).getEngine();
        } catch (Exception e) {
            log.warn("LLM 엔진 판단 실패. 기본 'google' 엔진을 사용합니다. 오류: {}", e.getMessage());
            return "google"; // 실패 시 안전한 기본값 반환
        }
    }

    // ✅ 툴로 등록할 메서드
    @Tool(name = "search_news")
    public String searchNews(String query) {
        try {
            GoogleSearch search = new GoogleSearch(Map.of(
                    "q", query,
                    "api_key", "너의_실제_API_KEY",  // 🔥 application.yml에서 불러오도록 리팩토링 가능
                    "hl", "ko",
                    "gl", "kr",
                    "num", "10",
                    "sort_by", "date"
            ));
            Map<String, Object> result = search.getJson();
            return result.toString(); // LLM이 사용 가능한 문자열로 반환
        } catch (Exception e) {
            log.error("SerpAPI 검색 실패: {}", e.getMessage());
            return "검색 중 오류가 발생했습니다.";
        }
    }
}
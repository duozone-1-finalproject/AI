package com.example.demo.service;

import com.example.demo.dto.LlmSearchType;
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
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);
    private final ChatClient chatClient;

    public LlmService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * LLM을 사용하여 사용자의 프롬프트 의도를 분석하고, 신뢰할 수 있는 형식으로 검색 유형을 결정합니다.
     * BeanOutputParser를 사용하여 LLM이 예측 불가능한 텍스트 대신 항상 JSON 형식으로 응답하도록 강제합니다.
     * 이는 자동화 파이프라인의 안정성을 위해 매우 중요합니다.
     *
     * @param userPrompt 사용자가 입력한 원본 프롬프트
     * @return "news", "general", "report" 중 하나의 문자열
     */
    public String determineSearchType(String userPrompt) {
        try {
            // 1. LLM이 반환할 데이터 구조(POJO)를 기반으로 OutputParser를 생성합니다.
            var outputParser = new BeanOutputParser<>(LlmSearchType.class);

            // 2. PromptTemplate을 사용하여 프롬프트를 안전하고 체계적으로 구성합니다.
            // {userPrompt}는 사용자 입력으로 안전하게 대체됩니다.
            // {format} 플레이스홀더에는 outputParser가 생성한 JSON 형식 지침이 자동으로 삽입됩니다.
            String promptString = """
                사용자의 검색 목적을 분석해서 어떤 검색 엔진을 써야 할지 판단해줘.
                가능한 선택지는 'news', 'general', 'report' 중 하나야.
                사용자 질문: "{userPrompt}"
    
                너의 응답은 반드시 다음 JSON 형식이어야만 해:
                {format}
                """;

            PromptTemplate promptTemplate = new PromptTemplate(promptString, Map.of(
                    "userPrompt", userPrompt,
                    "format", outputParser.getFormat()
            ));
            Prompt prompt = promptTemplate.create();

            // 3. LLM을 호출하고, OutputParser를 통해 결과를 신뢰성 있게 파싱합니다.
            ChatResponse response = chatClient.call(prompt);
            LlmSearchType searchType = outputParser.parse(response.getResult().getOutput().getContent());

            // 4. 파싱된 객체에서 카테고리 값을 추출하여 반환합니다.
            // 이렇게 하면 LLM이 부가적인 설명을 추가하더라도 항상 정확한 'category' 값만 얻을 수 있습니다.
            if (searchType == null || searchType.getCategory() == null || searchType.getCategory().isBlank()) {
                log.warn("LLM이 유효한 카테고리를 반환하지 않았습니다. 기본 검색 유형 'general'을 사용합니다.");
                return "general";
            }

            return searchType.getCategory();

        } catch (Exception e) {
            // LLM 응답을 파싱하거나 처리하는 중 어떤 종류의 예외가 발생하더라도,
            // 시스템을 멈추지 않고 안전한 기본값("general")을 반환합니다.
            log.warn("LLM 응답 처리 중 예외 발생. 기본 검색 유형 'general'을 사용합니다. 오류: {}", e.getMessage());
            return "general";
        }
    }
}

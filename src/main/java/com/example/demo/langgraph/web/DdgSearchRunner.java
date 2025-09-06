package com.example.demo.langgraph.web;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DdgSearchRunner {

    private final ChatClient chatClient;

    public DdgSearchRunner(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String run(List<String> keywords) {
        // --- 템플릿 바인딩 ---
        String systemPrompt = """
        당신은 한국어 중심의 “키워드 웹 서치 요약기”입니다.
        ...
        [여기에 위 System Prompt 전문 붙여넣기]
        """;

        String userPrompt = """
        [검색 키워드]
        %s

        [작업 지시]
        ...
        [여기에 위 User Prompt 전문 붙여넣기 ( {per_keyword}=3 로 치환 )]
        """.formatted(toJsonArray(keywords));

        // --- 메시지 구성 ---
        SystemMessage sys = new SystemMessage(systemPrompt);
        UserMessage usr = new UserMessage(userPrompt);

        // --- 옵션: MCP 도구 호출 허용 ---
        // 모델/옵션은 환경에 맞게 조정 (예: gpt-5-mini 등)
        OpenAiChatOptions opts = OpenAiChatOptions.builder()
                .model(System.getenv().getOrDefault("OPENAI_MODEL", "gpt-5-mini"))
                .toolChoice("auto")           // 도구 자동 호출 허용
                .build();

        Prompt prompt = new Prompt(List.of(sys, usr), opts);

        // --- 실행: JSON 문자열을 그대로 반환하도록 모델에 지시했으므로 text() 사용 ---
        return chatClient.prompt(prompt).call().content(); // 반드시 JSON만 반환되도록 프롬프트에서 강제
    }

    private static String toJsonArray(List<String> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i=0; i<list.size(); i++) {
            sb.append("\"").append(escape(list.get(i))).append("\"");
            if (i < list.size()-1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

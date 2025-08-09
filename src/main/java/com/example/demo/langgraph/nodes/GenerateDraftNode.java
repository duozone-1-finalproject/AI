package com.example.demo.langgraph.nodes;

import com.example.demo.langgraph.state.RiskState;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.joining;

public class GenerateDraftNode implements AsyncNodeAction<RiskState> {

    private final ChatClient chat;

    public GenerateDraftNode(ChatClient chat) {
        this.chat = chat;
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(RiskState state) {
        // 1) 프롬프트 구성
        String basePrompt = state.draft();
        String evidText = state.evidence().stream()
                .map(Object::toString)
                .collect(joining("\n"));

        String finalPrompt = basePrompt
                + "\n\n[참고자료 요약]\n" + evidText
                + "\n\n[요구사항]\n- 산업/회사/기타 중 해당 유형만 작성\n"
                + "- 각 단락 끝에 '출처: 제목 URL/메타' 1줄 표기\n"
                + "- 한국어 엄정 어조, 과장 금지";

        // 2) 동기 call()을 비동기로 래핑해 CompletableFuture로 반환
        return CompletableFuture.supplyAsync(() -> {
            // 가장 간단한 반환: 본문만
            String content = chat
                    .prompt()               // fluent 시작
                    .user(finalPrompt)      // user 메시지
                    .call()                 // 동기 호출
                    .content();             // = ChatResponse#result#output#content

            return Map.of(RiskState.DRAFT, content);
        });
    }
}

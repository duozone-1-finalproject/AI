package com.example.demo.graphweb.nodes;

import com.example.demo.constants.WebConstants;
import com.example.demo.dto.SearchEnvelope;
import com.example.demo.dto.SearchLLMDto;
import com.example.demo.dto.ValidationResultDto;
import com.example.demo.graphweb.WebState;
import com.example.demo.service.PromptCatalogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ValidationNode implements AsyncNodeAction<WebState> {

    @Qualifier("chatWithMcp")
    private final ChatClient chatClient;
    private final PromptCatalogService catalog;
    private final ObjectMapper om;

    @Override
    public CompletableFuture<Map<String, Object>> apply(WebState state) {
        try {
            // 1) 단건 본문(content) 확보 (String)
            String content = state.getProArticles();
            if (content == null || content.isBlank()) {
                log.info("[ValidationNode] Empty fetched content -> validated=false");
                return CompletableFuture.completedFuture(Map.of(WebState.VALIDATED, false));
            }

            // 2) 제목(title) 확보: Search 단계에서 고른 후보 메타 사용 (없으면 임시 제목)
            SearchLLMDto.Item meta = state.getPickedArticle();
            String title = meta.getTitle();
            String query = state.getCurKeyword();

            // 3) LLM 입력 articles_json: 단건도 배열 형태로 전달
            Map<String, Object> vars = Map.of(
                    "title_str", title,
                    "content_str", content,
                    "query", query
            );

            // 5) 프롬프트 생성
            Prompt sys = catalog.createSystemPrompt("web_validator_sys", Map.of());
            Prompt usr = catalog.createPrompt("web_validator_user", vars);

//            // 6) JSON 객체만 받도록 강제
//            var options = OpenAiChatOptions.builder()
//                    .withResponseFormat(new ResponseFormat(ResponseFormat.Type.JSON_OBJECT))
//                    .build();

            // 7) 호출
            List<Message> msgs = new ArrayList<>(sys.getInstructions());
            msgs.addAll(usr.getInstructions());
//            String promptLog = msgs.stream()
//                    .map(m -> "[" + m.getMessageType() + "] " + m.getText())
//                    .collect(Collectors.joining("\n---\n"));
//            log.info("\n===== finalPrompt =====\n{}\n=======================", promptLog);

            // (3) JSON Schema 강제 (strict) 옵션 설정
            ResponseFormat.JsonSchema jsonSchema = ResponseFormat.JsonSchema.builder()
                    .name("ValidationResultDto")
                    .schema(WebConstants.VALIDATION_JSON_SCHEMA)
                    .strict(true)
                    .build();

            ResponseFormat rf = ResponseFormat.builder()
                    .type(ResponseFormat.Type.JSON_SCHEMA)
                    .jsonSchema(jsonSchema)
                    .build();

            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .responseFormat(rf)
                    .build();

            Prompt finalPrompt = new Prompt(msgs, options);

            ValidationResultDto results = chatClient
                    .prompt(finalPrompt)
                    .call()
                    .entity(ValidationResultDto.class);
            log.info("[ValidationNode] results: \n{}", results); //log로 바꾸면 터미널

            Boolean validated = results.getFinalResult().isValidated();

            return CompletableFuture.completedFuture(Map.of(WebState.VALIDATED, validated));

        } catch (Exception e) {
            log.error("[ValidationNode] 검증 실패 - 오류 발생", e);
            return CompletableFuture.completedFuture(Map.of(WebState.VALIDATED, false));
        }
    }
}


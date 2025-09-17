package com.example.demo.graphweb.nodes;

import com.example.demo.constants.WebConstants;
import com.example.demo.dto.graphweb.SearchLLMDto;
import com.example.demo.dto.graphweb.ValidationResultDto;
import com.example.demo.graphweb.WebState;
import com.example.demo.service.graphmain.impl.PromptCatalogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class ValidationNode implements AsyncNodeAction<WebState> {

    @Qualifier("chatWithMcp")
    private final ChatClient chatClient;
    private final PromptCatalogService catalog;
    private final ObjectMapper om;

//    // 검증 기준 키워드 상수 (멀티라인)
//    private static final String BIZ_KEY =
//            "시장환경/수요 변화, 경쟁 격화/진입장벽 변화, 단가 하락 압력\n" +
//                    "규제·정책 신설/강화로 사업 영향\n" +
//                    "기술 급변/제품 수명주기 단축, 지속 R&D 부담, 적응 실패 리스크\n" +
//                    "거래처 편중(매출 상위 고객 의존), 공급망/외주 의존(차질·품질·납기)\n" +
//                    "신규사업/사업양수·출자 실패·지연·자금조달 리스크\n" +
//                    "업종 특성 리스크(제약·바이오 임상/승인, 성공보수·라이선스 구조 등)\n";
//
//    private static final String COM_KEY =
//            "재무 악화/유동성(영업현금흐름 적자, 이자보상배율, 차입 의존, 차환 위험)\n" +
//                    "재고·매출 급감 사유, 손상/상각 가능성, 지배구조·약정(재무구조개선약정 미체결 등)\n";


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
//            String bizKey = BIZ_KEY;
//            String comKey = COM_KEY;

            // 3) LLM 입력 articles_json: 단건도 배열 형태로 전달
            Map<String, Object> vars = Map.of(
                    "title_str", title,
                    "content_str", content,
                    "query", query
//                    "biz_key", bizKey,
//                    "com_key", comKey
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
            log.info("[ValidationNode] Final validation result: {}", validated);

            return CompletableFuture.completedFuture(Map.of(WebState.VALIDATED, true));

        } catch (Exception e) {
            log.error("[ValidationNode] 검증 실패 - 오류 발생", e);
            return CompletableFuture.completedFuture(Map.of(WebState.VALIDATED, false));
        }
    }
}


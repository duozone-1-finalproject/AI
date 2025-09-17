package com.example.demo.graphweb.nodes;

import com.example.demo.dto.graphweb.SearchLLMDto;
import com.example.demo.dto.graphweb.WebDocs;
import com.example.demo.graphweb.WebState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
@Component
@RequiredArgsConstructor
public class AfterValidationNode implements AsyncNodeAction<WebState> {
    @Override
    public CompletableFuture<Map<String, Object>> apply(WebState s) {
        int keyIdx   = s.getCurKeyIdx();
        int candIdx  = s.getCurCandIdx();
        boolean ok   = s.getValidated();
        int keyCnt   = s.getArticlesSize();

        // 키 인덱스 범위 초과 시 종료
        if (keyIdx >= keyCnt) {
            return CompletableFuture.completedFuture(Map.of(
                    WebState.DECISION, "end"
            ));
        }

        int candCnt = s.getCandidatesSize(keyIdx);

        // 실패 흐름
        if (!ok) {
            // 후보가 없거나 모두 소진 → 실패 문서 1건 생성 후 키워드 전환
            if (candCnt <= 0 || candIdx + 1 >= candCnt) {
                WebDocs fail = new WebDocs();
                fail.setKeyword(s.getCurKeyword());
                fail.setTitle("키워드 관련 기사 없음");
                fail.setContent("해당 키워드에 대한 유효한 기사를 찾을 수 없습니다.");
                fail.setSource("N/A");
                fail.setUrl("N/A");
                fail.setDate(null);

                int nextKey = keyIdx + 1;
                if (nextKey >= keyCnt) {
                    return CompletableFuture.completedFuture(Map.of(
                            WebState.WEB_DOCS, List.of(fail),
                            WebState.DECISION, "end"
                    ));
                }
                return CompletableFuture.completedFuture(Map.of(
                        WebState.WEB_DOCS, List.of(fail),
                        WebState.CUR_KEY_IDX, nextKey,
                        WebState.CUR_CAND_IDX, 0,
                        WebState.DECISION, "continue"
                ));
            }

            // 후보 남음 → 후보 인덱스만 증가, WEB_DOCS 미반환
            return CompletableFuture.completedFuture(Map.of(
                    WebState.CUR_CAND_IDX, candIdx + 1,
                    WebState.DECISION, "continue"
            ));
        }

        // 성공 흐름: 기사 본문 확보됨 → 패스 문서 생성 후 키워드 전환
        String body = s.getProArticles();
        SearchLLMDto.Item meta = s.getPickedArticle();

        WebDocs pass = new WebDocs();
        pass.setKeyword(s.getCurKeyword());
        if (meta != null) {
            pass.setTitle(meta.getTitle());
            pass.setUrl(meta.getUrl());
            pass.setSource(meta.getTopic());
            pass.setDate(meta.getPublishedDate());
        }
        pass.setContent(body);

        int nextKey = keyIdx + 1;
        if (nextKey >= keyCnt) {
            return CompletableFuture.completedFuture(Map.of(
                    WebState.WEB_DOCS, List.of(pass),
                    WebState.DECISION, "end"
            ));
        }
        return CompletableFuture.completedFuture(Map.of(
                WebState.WEB_DOCS, List.of(pass),
                WebState.CUR_KEY_IDX, nextKey,
                WebState.CUR_CAND_IDX, 0,
                WebState.DECISION, "continue"
        ));
    }
}
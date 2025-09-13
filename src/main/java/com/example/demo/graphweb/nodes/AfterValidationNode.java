package com.example.demo.graphweb.nodes;

import com.example.demo.dto.SearchLLMDto;
import com.example.demo.dto.WebDocs;
import com.example.demo.graphweb.WebState;
import org.bsc.langgraph4j.action.AsyncNodeAction;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AfterValidationNode implements AsyncNodeAction<WebState> {
    @Override
    public CompletableFuture<Map<String, Object>> apply(WebState webState) {
        Integer keyIdx = webState.getCurKeyIdx();
        Integer candIdx = webState.getCurCandIdx();
        Boolean fetchOk = webState.getValidated();

        // 통과 안했을 때 -> candIdx+1 하고 return candIdx+1 -> PickedArticleNode로 컨디셔널 엣지(루프)
        if (!fetchOk) {
            candIdx += 1;
            return CompletableFuture.completedFuture(Map.of(
                    WebState.CUR_CAND_IDX, candIdx,
                    WebState.DECISION, "continue"
            ));
        } else {
            // String fetchedArticle = webstate.getFetchedArticle();
            String fetchedArticle = "더미 데이터 입니다.";
            String keyword = webState.getCurKeyword();
            SearchLLMDto.Item articleMeta = webState.getPickedArticle();

            WebDocs pass = new WebDocs();
            pass.setKeyword(keyword);
            pass.setTitle(articleMeta.getTitle());
            pass.setUrl(articleMeta.getUrl());
            pass.setSource(articleMeta.getSource());
            pass.setDate(articleMeta.getDate());
            pass.setContent(fetchedArticle);

            keyIdx += 1;
            if (keyIdx >= webState.getArticlesSize()) {
                return CompletableFuture.completedFuture(Map.of(
                        WebState.WEB_DOCS, List.of(pass),
                        WebState.DECISION, "end"
                ));
            }


            return CompletableFuture.completedFuture(Map.of(
                    WebState.WEB_DOCS, List.of(pass),
                    WebState.CUR_KEY_IDX, keyIdx,
                    WebState.CUR_CAND_IDX, 0,
                    WebState.DECISION, "continue"
            ));
        }
    }
}

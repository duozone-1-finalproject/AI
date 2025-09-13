package com.example.demo.graphweb.nodes;

import com.example.demo.dto.SearchLLMDto;
import com.example.demo.graphweb.WebState;
import org.bsc.langgraph4j.action.AsyncNodeAction;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PickedArticleNode implements AsyncNodeAction<WebState> {
    @Override
    public CompletableFuture<Map<String, Object>> apply(WebState webState) {
        List<SearchLLMDto> articleList = webState.getArticles();
        Integer keyIdx = webState.getCurKeyIdx();
        Integer candIdx = webState.getCurCandIdx();

        SearchLLMDto.Item pickedArticle = articleList.get(keyIdx).getCandidates().get(candIdx);
        String keyword = articleList.get(keyIdx).getKeyword();

        return CompletableFuture.completedFuture(Map.of(
                WebState.PICKED_ARTICLE, pickedArticle,
                WebState.CUR_KEYWORD, keyword
        ));
    }
}

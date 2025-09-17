package com.example.demo.graphdb.nodes;

import com.example.demo.graphdb.DbSubGraphState;
import com.example.demo.dto.graphdb.QueryRequestDto;
import com.example.demo.service.graphdb.DbSubGraphService;
import com.example.demo.service.graphdb.QueryGenerateService;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.example.demo.graphdb.DbSubGraphState.PEER_CODES;

@Component("corpCodeRetrieval")
@RequiredArgsConstructor
public class CorpCodeRetrievalNode implements AsyncNodeAction<DbSubGraphState> {

    private final DbSubGraphService dbSubGraphService;
    private final QueryGenerateService queryGenerateService;

    @Override
    public CompletableFuture<Map<String, Object>> apply(DbSubGraphState state) {
        List<String> peerCodes;
        try {
            QueryRequestDto request = queryGenerateService.generateQuery(
                    state.getFilterCriteria(),
                    state.getCorpCode(),
                    state.getIndustryCode()
            );

            peerCodes = dbSubGraphService.findPeerCompanies(request.query(), request.indexName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return CompletableFuture.completedFuture(Map.of(PEER_CODES, peerCodes));
    }
}

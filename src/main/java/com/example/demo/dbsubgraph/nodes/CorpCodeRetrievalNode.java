package com.example.demo.dbsubgraph.nodes;

import com.example.demo.dbsubgraph.state.DbSubGraphState;
import com.example.demo.dto.dbsubgraph.QueryRequestDto;
import com.example.demo.service.subgraph.db.DbSubGraphService;
import com.example.demo.service.subgraph.db.QueryGenerateService;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.example.demo.dbsubgraph.state.DbSubGraphState.PEER_CODES;

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
                    state.<String>value(DbSubGraphState.FILTER_CRITERIA).orElseThrow(),
                    state.<String>value(DbSubGraphState.CORP_CODE).orElseThrow(),
                    state.<String>value(DbSubGraphState.IND_CODE).orElseThrow()
            );

            peerCodes = dbSubGraphService.fetchPeerCorpCodes(request.query(), request.indexName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return CompletableFuture.completedFuture(Map.of(PEER_CODES, peerCodes));
    }
}

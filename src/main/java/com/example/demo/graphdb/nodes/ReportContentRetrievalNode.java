package com.example.demo.graphdb.nodes;

import com.example.demo.graphdb.DbSubGraphState;
import com.example.demo.service.graphdb.DbSubGraphService;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.example.demo.graphdb.DbSubGraphState.RAW_DOCS;

@Component("reportContentRetrieval")
@RequiredArgsConstructor
public class ReportContentRetrievalNode implements AsyncNodeAction<DbSubGraphState> {

    private final DbSubGraphService dbSubGraphService;

    @Override
    public CompletableFuture<Map<String, Object>> apply(DbSubGraphState state) {
        List<String> peerCodes = state.getPeerCodes();
        String sectionTitle = state.getSectionTitle();
        // sectionTitle = "기타 투자위험" 일 때, "기타위험" 으로 변경
        if ("기타 투자위험".equals(sectionTitle)) {
            sectionTitle = sectionTitle.replace(" 투자", "");
        }

        List<String> rawDocs;
        try {
            rawDocs = dbSubGraphService.getReportSections(peerCodes, sectionTitle);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(Map.of(RAW_DOCS, rawDocs));
    }
}

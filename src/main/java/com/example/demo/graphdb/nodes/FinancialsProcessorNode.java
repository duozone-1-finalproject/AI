package com.example.demo.graphdb.nodes;

import com.example.demo.graphdb.DbSubGraphState;
import com.example.demo.service.graphdb.DbSubGraphService;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.example.demo.graphdb.DbSubGraphState.FINANCIALS;
import static com.example.demo.util.graphdb.FinancialUtils.convertToMarkdown;

@Component("financialsProcessor")
@RequiredArgsConstructor
public class FinancialsProcessorNode implements AsyncNodeAction<DbSubGraphState> {

    private final DbSubGraphService dbSubGraphService;

    @Override
    public CompletableFuture<Map<String, Object>> apply(DbSubGraphState state) {
        String mdString;

        try {
            Map<String, Map<String, Double>> totalFinancials = dbSubGraphService.getAllFinancialAccounts(
                    state.getCorpCode()
            );
            mdString = convertToMarkdown(totalFinancials);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(Map.of(FINANCIALS, mdString));
    }
}

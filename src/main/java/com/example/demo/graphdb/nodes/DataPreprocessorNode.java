package com.example.demo.graphdb.nodes;

import com.example.demo.graphdb.DbSubGraphState;
import com.example.demo.dto.graphdb.DbDocDto;
import com.example.demo.service.graphdb.DataProcessService;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.example.demo.graphdb.DbSubGraphState.DB_DOCS;

@Component("dataProcessor")
@RequiredArgsConstructor
public class DataPreprocessorNode implements AsyncNodeAction<DbSubGraphState> {

    private final DataProcessService dataProcessService;

    @Override
    public CompletableFuture<Map<String, Object>> apply(DbSubGraphState state) {
        List<String> rawDocs = state.getRawDocs();
        List<DbDocDto> dbDocs = dataProcessService.processData(rawDocs);
        return CompletableFuture.completedFuture(Map.of(DB_DOCS, dbDocs));
    }
}

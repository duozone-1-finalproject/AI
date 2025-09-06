package com.example.demo.graphmain.nodes;

// RAG 소스 선택 노드
import com.example.demo.graphmain.DraftState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.example.demo.constants.YamlConstants.SOURCES_MAP;
import static com.example.demo.graphmain.DraftState.SECTION;
import static com.example.demo.graphmain.DraftState.SOURCES;

@Component("source_select")
@RequiredArgsConstructor
public class SourceSelectorNode implements AsyncNodeAction<DraftState> {

    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        String sectionKey = state.getSection();
        List<String> sources = SOURCES_MAP.get(sectionKey);

        return CompletableFuture.completedFuture(Map.of(SOURCES, sources));
    }
}
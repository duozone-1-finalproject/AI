package com.example.demo.service.graphmain.impl;

import com.example.demo.dto.graphmain.DraftRequestDto;
import com.example.demo.dto.graphmain.DraftResponseDto;
import com.example.demo.graphmain.DraftState;
import com.example.demo.service.graphmain.GraphService;
import lombok.RequiredArgsConstructor;
import org.bsc.async.AsyncGenerator;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.NodeOutput;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.example.demo.constants.YamlConstants.DEFAULT_ORDER;
import static com.example.demo.constants.YamlConstants.SECTION_MAP;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraphServiceImpl implements GraphService {
    private static final String MAX_ITEMS_LIMIT = "5";
    private static final long TIMEOUT_SEC = 1200; // 섹션별 타임아웃

    private final CompiledGraph<DraftState> graph;

    @Override
    public DraftResponseDto run(DraftRequestDto req) {
        try (ExecutorService es = Executors.newVirtualThreadPerTaskExecutor()) {
            DraftResponseDto dto = new DraftResponseDto();

            // 섹션별 병렬 실행 → 완료되면 DTO에 바로 set
            List<CompletableFuture<Void>> tasks = DEFAULT_ORDER.stream()
                    .map(key -> CompletableFuture
                            .supplyAsync(() -> runOne(key, req), es)
                            .orTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
                            // 부분 실패 허용: 예외를 메시지로 수렴(원하면 여기서 rethrow 가능)
                            .exceptionally(ex -> "[ERROR] " + ex.getClass().getSimpleName() + ": " + ex.getMessage())
                            .thenAccept(result -> dto.setBySectionKey(key, result))
                    ).toList();

            CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new)).join();
            return dto;
        }
    }

    private String runOne(String sectionKey, DraftRequestDto req) {
        String sectionLabel = SECTION_MAP.get(sectionKey);

        Map<String, Object> init = new LinkedHashMap<>();
        init.put(DraftState.CORP_CODE, req.getCorpCode());
        init.put(DraftState.CORP_NAME, req.getCorpName());
        init.put(DraftState.IND_CODE, req.getIndutyCode());
        init.put(DraftState.IND_NAME, req.getIndutyName());
        init.put(DraftState.SECTION, sectionKey);
        init.put(DraftState.SECTION_LABEL, sectionLabel);
        init.put(DraftState.MAX_ITEMS, MAX_ITEMS_LIMIT);
        log.debug("################################ MAIN GRAPH START ({}) ##########################################", sectionLabel);
        graph.setMaxIterations(100);

        AsyncGenerator<NodeOutput<DraftState>> stream = graph.stream(init);

        final AtomicReference<DraftState> finalStateRef = new AtomicReference<>();

        stream.forEach(nodeOutput -> {
            DraftState currentState = nodeOutput.state();
            log.debug("Main Graph node processed. Current node: {}", nodeOutput.node());
            log.debug("{}", currentState);
            log.debug("DrafteState drafts(size={}): {}",
                    currentState.getDrafts().size(), currentState.getDrafts());
            finalStateRef.set(currentState);
        });

        DraftState finalState = finalStateRef.get();
        if (finalState == null) {
            // 스트림이 비어있는 경우에 대한 처리
            finalState = new DraftState(Map.of());
        }
        log.debug("################################ MAIN GRAPH END   ({}) ##########################################", sectionLabel);
        return finalState.getDrafts().getLast();
    }
}

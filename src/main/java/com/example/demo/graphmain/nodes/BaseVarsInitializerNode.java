package com.example.demo.graphmain.nodes;

import com.example.demo.graphmain.DraftState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component("initBaseVars")
@RequiredArgsConstructor
public class BaseVarsInitializerNode implements AsyncNodeAction<DraftState> {

    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        Map<String, Object> baseVars = Map.copyOf(Map.of(
                        "corpName", state.getCorpName(),
                        "indutyName", state.getIndutyName(),
                        "webRagItems", List.of(),
                        "dartRagItems", List.of(),
                        "financialData", List.of(),
                        "otherRiskInputs", List.of(),
                        "maxItems", state.getMaxItems()
                ));
        return CompletableFuture.completedFuture(Map.of(DraftState.BASEVARS, baseVars));
    }
}

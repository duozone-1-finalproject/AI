package com.example.demo.graphmain.nodes;

import com.example.demo.graphmain.DraftState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.example.demo.constants.MainGraphConstants.OTHER_RISK_INPUT;

@Component("initBaseVars")
@RequiredArgsConstructor
public class BaseVarsInitializerNode implements AsyncNodeAction<DraftState> {

    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        Map<String, Object> baseVars = Map.copyOf(Map.of(
                        "corpName", state.getCorpName(), // String
                        "indutyName", state.getIndutyName(), // String
                        "webRagItems", state.getWebDocs(),  // JSON
                        "dartRagItems", state.getDbDocs(), // JSON
                        "financialData", state.getFinancials(), // String Markdown 테이블
                        "otherRiskInputs", OTHER_RISK_INPUT, // JSON 현재 더미 주입, 추후 수정예정
                        "maxItems", state.getMaxItems() // String e.g. "5"
                ));
        return CompletableFuture.completedFuture(Map.of(DraftState.BASEVARS, baseVars));
    }
}

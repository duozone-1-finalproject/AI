package com.example.demo.langgraph.node;

import com.example.demo.service.PromptTemplateService;
import org.bsc.langgraph4j.action.NodeAction;
import com.example.demo.langgraph.state.RiskState;
import java.util.Map;

public class BuildPromptNode implements NodeAction<RiskState> {
    private final PromptTemplateService prompts;
    private final String templateName;

    public BuildPromptNode(PromptTemplateService prompts, String templateName) {
        this.prompts = prompts;
        this.templateName = templateName;
    }

    @Override
    public Map<String, Object> apply(RiskState state) {
        var input = state.input();
        String corpName = (String) input.getOrDefault("corpName", "");
        String industry = (String) input.getOrDefault("industry", "");
        String prompt = prompts.render(templateName, Map.of(
                "corpName", corpName, "industry", industry
        ));
        return Map.of(RiskState.DRAFT, prompt); // 일단 프롬프트 슬롯에 세팅(다음 노드에서 컨텍스트 합성)
    }
}

package com.example.demo.dbsubgraph.nodes;

import com.example.demo.config.AiSectionProperties;
import com.example.demo.dbsubgraph.state.DbSubGraphState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.example.demo.dbsubgraph.state.DbSubGraphState.FILTER_CRITERIA;
import static com.example.demo.dbsubgraph.state.DbSubGraphState.LABEL;

@Component("filterCriteria")
@RequiredArgsConstructor
public class FilterCriteriaNode implements AsyncNodeAction<DbSubGraphState> {

    private final AiSectionProperties props;

    @Override
    public CompletableFuture<Map<String, Object>> apply(DbSubGraphState state) {
        String section = state.<String>value(DbSubGraphState.SECTION).orElseThrow();

        var sectionConfig = props.getSections().get(section);
        String filter = sectionConfig.getFilter();
        String label = sectionConfig.getLabel();

        return CompletableFuture.completedFuture(Map.of(
                FILTER_CRITERIA, filter,
                LABEL, label
        ));
    }
}

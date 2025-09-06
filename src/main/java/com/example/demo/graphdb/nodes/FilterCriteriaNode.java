package com.example.demo.graphdb.nodes;

import com.example.demo.graphdb.DbSubGraphState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.example.demo.constants.YamlConstants.FILTER_MAP;
import static com.example.demo.constants.YamlConstants.SECTION_MAP;
import static com.example.demo.graphdb.DbSubGraphState.*;

@Component("filterCriteria")
@RequiredArgsConstructor
public class FilterCriteriaNode implements AsyncNodeAction<DbSubGraphState> {

    @Override
    public CompletableFuture<Map<String, Object>> apply(DbSubGraphState state) {
        String section = state.getSectionName();

        String filter = FILTER_MAP.get(section);
        String label = SECTION_MAP.get(section);

        return CompletableFuture.completedFuture(Map.of(
                FILTER_CRITERIA, filter,
                SECTION_TITLE, label
        ));
    }
}

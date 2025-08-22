package com.example.demo.langgraph.state;

import com.example.demo.langgraph.state.DraftState;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.Channel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class DraftGraphWiring {

    @Bean
    public StateGraph<DraftState> draftStateGraph() {
        // DraftState.SCHEMA는 Map.ofEntries(...)로 불변일 수 있어 가변 Map으로 복사(안전)
        Map<String, Channel<?>> schema = new LinkedHashMap<>(DraftState.SCHEMA);

        // 스키마 + 상태 팩토리로 StateGraph 생성 (DraftState(Map<String,Object>) 사용)
        return new StateGraph<>(schema, DraftState::new);
        // 불변 Map을 그대로 써도 된다면 아래 한 줄로 끝:
        // return new StateGraph<>(DraftState.SCHEMA, DraftState::new);
    }
}
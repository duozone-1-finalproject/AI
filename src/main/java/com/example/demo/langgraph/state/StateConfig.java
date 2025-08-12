package com.example.demo.langgraph.state;

import com.example.demo.dto.LangGraphDto;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.example.demo.langgraph.state.ChannelKeys.*;

@Configuration
public class StateConfig {

    @Bean
    public Map<String, Channel<?>> graphChannels() {
        Map<String, Channel<?>> channels = new HashMap<>();
        // Supplier는 “초기값”을 제공
        channels.put(INPUT,  Channels.base(() -> new LangGraphDto.PromptRequest("")));
        channels.put(OUTPUT, Channels.base(() -> new LangGraphDto.GraphResult("")));
        return channels;
    }

    @Bean
    public StateGraph<AgentState> stateGraph(Map<String, Channel<?>> graphChannels) {
        // 상태 팩토리: AgentState::new
        return new StateGraph<>(graphChannels, AgentState::new);
    }
}

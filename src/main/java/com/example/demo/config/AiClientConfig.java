package com.example.demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
class AiClientConfig {

    /**
     * Spring AI가 오토컨피그로 제공하는 ChatClient.Builder를 주입받아
     * 애플리케이션 전역에서 재사용할 ChatClient 빈을 생성합니다.
     */
    @Bean("default")
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    @Bean("chatWithMcp")
    ChatClient chatWithMcp(ChatClient.Builder b,
                           SyncMcpToolCallbackProvider mcpTools) {
        return b.defaultToolCallbacks(mcpTools).build(); // MCP 툴(search/crawl) 사용
    }
}
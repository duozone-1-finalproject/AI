package com.example.demo.langgraph.web.client;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// 연동 필요함

@Configuration
class ChatClientsConfig {

    @Bean("chatPlain")
    ChatClient chatPlain(ChatClient.Builder b) {
        return b.build(); // 툴 없음
    }

    @Bean("chatWithMcp")
    ChatClient chatWithMcp(ChatClient.Builder b,
                           SyncMcpToolCallbackProvider mcpTools) {
        return b.defaultToolCallbacks(mcpTools).build(); // MCP 툴(search/fetch) 사용
    }
}
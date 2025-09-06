package com.example.demo.graphweb.client;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


@Component
public class DuckDuckClient {


    public String search(String query) {
// MCP 호출 로직 구현 부분
// 실제 MCP 통신 코드 작성
        return "{result: ...}"; // 임시 예시
    }
}

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
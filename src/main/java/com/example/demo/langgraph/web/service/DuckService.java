//# Duck 서브그래프 실행 로직
package com.example.demo.langgraph.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DuckService {

    @Qualifier("chatWithMcp")
    private final ChatClient chatWithMcp;

    public String search(String query) {
        return chatWithMcp.prompt(query).call().content();
    }
}



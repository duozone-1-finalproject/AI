package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class LlmServiceImpl implements LlmService {

    private final ChatClient.Builder chatClientBuilder;

    public LlmServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    @Override
    public String complete(String userInput) {
        return chatClientBuilder.build()
                .prompt()
                .user(userInput == null ? "" : userInput)
                .call()
                .content();
    }
}

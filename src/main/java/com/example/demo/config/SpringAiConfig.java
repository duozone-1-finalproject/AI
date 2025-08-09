package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.client.ChatClient;

@Configuration
public class SpringAiConfig {
    // spring-ai-starter-model-openai 가 자동 구성.
    // ChatClient 는 빈으로 바로 주입 가능. (필요시 별도 옵션 커스터마이즈)
    @Bean
    ChatClient chatClient(org.springframework.ai.openai.OpenAiChatModel openAiChatModel) {
        return ChatClient.create(openAiChatModel);
    }
}
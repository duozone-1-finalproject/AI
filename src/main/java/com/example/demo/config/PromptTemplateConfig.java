package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.template.st.StTemplateRenderer;

@Configuration
public class PromptTemplateConfig {

    @Bean
    public StTemplateRenderer stTemplateRenderer() {
        return StTemplateRenderer.builder()
                .startDelimiterToken('<')   // 변수 시작 구분자
                .endDelimiterToken('>')     // 변수 종료 구분자
                .build();
    }
}

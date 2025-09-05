package com.example.demo.config;

import org.springframework.ai.template.ValidationMode;
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
                .validateStFunctions()             // ST 내장 함수(validation 인식)
                .validationMode(ValidationMode.THROW) // 미스매치 시 예외 (fail-fast)
                .build();
    }
}

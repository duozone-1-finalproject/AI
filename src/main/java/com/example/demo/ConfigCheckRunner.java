package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ConfigCheckRunner implements CommandLineRunner {

    // application.yml 에 설정된 경로 그대로 주입받습니다.
    @Value("${ai.openai.api-key}")
    private String groqApiKey;

    @Value("${serp-api.api-key}")
    private String serpApiKey;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=========================================");
        // 값이 null 이거나 플레이스홀더 그대로 출력된다면 문제가 있는 것입니다.
        // .env 파일의 실제 키 값이 출력된다면 성공입니다.
        System.out.println("✅ GROQ API Key Loaded: " + groqApiKey);
        System.out.println("✅ SERP API Key Loaded: " + serpApiKey);
        System.out.println("=========================================");
    }
}
package com.example.demo.config;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "ai")
public class AiSectionProperties {

    // key: "risk_industry" ...
    private Map<String, SectionConfig> sections;
    private List<String> defaultOrder;
    private Map<String, String> prompts;

    @Setter
    @Getter
    public static class SectionConfig {
        private String prompt;         // ex) classpath:prompt/risk_industry.st
        private List<String> sources;  // ex) ["news","db"]
        private String label;          // ex) "사업위험"
        private String filter;
    }
}
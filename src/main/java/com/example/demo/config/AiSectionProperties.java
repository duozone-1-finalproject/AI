package com.example.demo.config;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai")
public class AiSectionProperties {

    // key: "risk_industry" ...
    private Map<String, SectionConfig> sections;
    private List<String> defaultOrder;

    public Map<String, SectionConfig> getSections() { return sections; }
    public void setSections(Map<String, SectionConfig> sections) { this.sections = sections; }

    public List<String> getDefaultOrder() { return defaultOrder; }
    public void setDefaultOrder(List<String> defaultOrder) { this.defaultOrder = defaultOrder; }

    @Setter
    @Getter
    public static class SectionConfig {
        private String prompt;         // ex) classpath:prompt/risk_industry.st
        private List<String> sources;  // ex) ["news","db"]
        private String label;          // ex) "사업위험"

    }
}
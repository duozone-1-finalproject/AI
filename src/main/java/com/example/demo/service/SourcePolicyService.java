package com.example.demo.service;

import java.util.List;
import java.util.Map;

import com.example.demo.config.AiSectionProperties;
import org.springframework.stereotype.Service;

@Service
public class SourcePolicyService {
    private final Map<String, AiSectionProperties.SectionConfig> sections;

    public SourcePolicyService(AiSectionProperties props) {
        this.sections = props.getSections();
    }

    public List<String> sourcesFor(String sectionKey) {
        return sections.get(sectionKey).getSources(); // ["NEWS","DB"] 등
    }
}
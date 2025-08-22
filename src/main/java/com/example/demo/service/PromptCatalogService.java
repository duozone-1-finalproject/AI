package com.example.demo.service;

import com.example.demo.config.AiSectionProperties;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PromptCatalogService {

    private final Map<String, AiSectionProperties.SectionConfig> sections;
    private final ResourceLoader resourceLoader;

    public PromptCatalogService(AiSectionProperties props, ResourceLoader resourceLoader) {
        this.sections = props.getSections();
        this.resourceLoader = resourceLoader;
    }

    private Resource resolve(String sectionKey) {
        String location = sections.get(sectionKey).getPrompt(); // ex) classpath:prompt/risk_industry.st
        return resourceLoader.getResource(location);
    }

    /** ① 문자열 프롬프트로 렌더링({var} 치환) */
    public String renderToString(String sectionKey, Map<String, Object> vars) {
        Resource res = resolve(sectionKey);
        PromptTemplate pt = new PromptTemplate(res);
        return pt.render(vars); // → 완성된 문자열
    }

    /** ② Prompt 객체 생성(시스템 프롬프트로 쓰고 싶을 때) */
    public Prompt createSystemPrompt(String sectionKey, Map<String, Object> vars) {
        Resource res = resolve(sectionKey);
        SystemPromptTemplate spt = new SystemPromptTemplate(res);
        return spt.create(vars); // → Prompt(roles/messages 포함)
    }

    /** ③ Prompt 객체 생성(일반 프롬프트로 쓰고 싶을 때) */
    public Prompt createPrompt(String sectionKey, Map<String, Object> vars) {
        Resource res = resolve(sectionKey);
        PromptTemplate pt = new PromptTemplate(res);
        return pt.create(vars); // → Prompt
    }
}

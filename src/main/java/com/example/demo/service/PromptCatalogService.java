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
    private final Map<String, String> prompts;
    private final ResourceLoader resourceLoader;

    public PromptCatalogService(AiSectionProperties props, ResourceLoader resourceLoader) {
        this.sections = props.getSections();
        this.prompts = props.getPrompts();
        this.resourceLoader = resourceLoader;
    }

    private Resource resolve(String key) {
        // 1. ai.sections 에서 먼저 찾기
        if (sections.containsKey(key)) {
            return resourceLoader.getResource(sections.get(key).getPrompt());
        }
        // 2. 없으면 ai.prompts 에서 찾기
        String location = prompts.get(key); // ex) classpath:prompts/adjust_default.st
        return resourceLoader.getResource(location);
    }

    /** ① 문자열 프롬프트로 렌더링({var} 치환) */
    public String renderToString(String key, Map<String, Object> vars) {
        Resource res = resolve(key);
        PromptTemplate pt = new PromptTemplate(res);
        return pt.render(vars); // → 완성된 문자열
    }

    /** ② Prompt 객체 생성(시스템 프롬프트로 쓰고 싶을 때) */
    public Prompt createSystemPrompt(String key, Map<String, Object> vars) {
        Resource res = resolve(key);
        SystemPromptTemplate spt = new SystemPromptTemplate(res);
        return spt.create(vars); // → Prompt(roles/messages 포함)
    }

    /** ③ Prompt 객체 생성(일반 프롬프트로 쓰고 싶을 때) */
    public Prompt createPrompt(String key, Map<String, Object> vars) {
        Resource res = resolve(key);
        PromptTemplate pt = new PromptTemplate(res);
        return pt.create(vars); // → Prompt
    }
}

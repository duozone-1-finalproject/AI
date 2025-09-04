package com.example.demo.service;

import com.example.demo.config.AiSectionProperties;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;   // ← 커스텀 구분자 렌더러
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PromptCatalogService {

    private final Map<String, AiSectionProperties.SectionConfig> sections;
    private final Map<String, String> prompts;
    private final ResourceLoader resourceLoader;
    private final StTemplateRenderer renderer; // ← < > 구분자 적용 렌더러 (PromptTemplateConfig에서 @Bean 등록)

    public PromptCatalogService(
            AiSectionProperties props,
            ResourceLoader resourceLoader,
            StTemplateRenderer renderer
    ) {
        this.sections = props.getSections();
        this.prompts = props.getPrompts();
        this.resourceLoader = resourceLoader;
        this.renderer = renderer;
    }

    private Resource resolve(String key) {
        // 1) ai.sections 에서 우선 검색
        if (sections.containsKey(key)) {
            return resourceLoader.getResource(sections.get(key).getPrompt());
        }
        // 2) 없으면 ai.prompts 에서 검색
        String location = prompts.get(key); // ex) classpath:prompts/adjust_default.st
        return resourceLoader.getResource(location);
    }

    /** ① 문자열 프롬프트로 렌더링 */
    public String renderToString(String key, Map<String, Object> vars) {
        Resource res = resolve(key);
        PromptTemplate pt = PromptTemplate.builder()
                .resource(res)
                .renderer(renderer) // ← 중요: { } 대신 < > 를 변수로 인식
                .build();
        return pt.render(vars);
    }

    /** ② 시스템 프롬프트 생성 */
    public Prompt createSystemPrompt(String key, Map<String, Object> vars) {
        Resource res = resolve(key);
        SystemPromptTemplate spt = SystemPromptTemplate.builder()
                .resource(res)
                .renderer(renderer) // ← 중요
                .build();
        return spt.create(vars);
    }

    /** ③ 일반 프롬프트 생성 */
    public Prompt createPrompt(String key, Map<String, Object> vars) {
        Resource res = resolve(key);
        PromptTemplate pt = PromptTemplate.builder()
                .resource(res)
                .renderer(renderer) // ← 중요
                .build();
        return pt.create(vars);
    }
}

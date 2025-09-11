package com.example.demo.service.graphmain.impl;

import com.example.demo.service.graphmain.MissingVarResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;   // ← 커스텀 구분자 렌더러
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.example.demo.constants.MainGraphConstants.VAR_PATTERN;
import static com.example.demo.constants.YamlConstants.*;

@Slf4j
@Service
public class PromptCatalogService {

    private final ResourceLoader resourceLoader;
    private final StTemplateRenderer renderer;

    public PromptCatalogService(ResourceLoader resourceLoader, StTemplateRenderer renderer) {
        this.resourceLoader = resourceLoader;
        this.renderer = renderer;
    }

    private Resource resolve(String key) {
        final Resource res;
        if (PROMPTS.containsKey(key)) {
            res = resourceLoader.getResource(PROMPTS.get(key));
        } else {
            throw new IllegalArgumentException("Unknown prompt key: " + key);
        }
        log.debug("[PromptCatalog] key='{}', resolved={}", key, res.getDescription());
        return res;
    }

    private String asString(Resource res) {
        try (var in = res.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read template resource: " + res, e);
        }
    }

    private Set<String> scanVariables(Resource res) {
        String tpl = asString(res);
        var m = VAR_PATTERN.matcher(tpl);
        Set<String> out = new LinkedHashSet<>();
        while (m.find()) {
            String name = m.group(1);
            if (!isProbablyHtmlTag(name)) out.add(name);
        }
        return out;
    }

    private boolean isProbablyHtmlTag(String name) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "b", "i", "u", "em", "strong", "p", "br", "div", "span", "ul", "ol", "li",
                 "h1", "h2", "h3", "h4", "h5", "h6", "img", "a", "table", "thead", "tbody",
                 "tr", "td", "th", "code", "pre" -> true;
            default -> false;
        };
    }

    /**
     * vars → resolver → defaults 순으로 채우고, 여전히 없으면 예외
     */
    private Map<String, Object> ensureVarsStrict(Resource res,
                                                 Map<String, Object> vars,
                                                 MissingVarResolver resolver) {
        Set<String> needed = scanVariables(res);
        Map<String, Object> safe = new HashMap<>(vars == null ? Map.of() : vars);
        List<String> missing = new ArrayList<>();

        for (String name : needed) {
            if (safe.containsKey(name)) continue;

            Object val = null;
            if (resolver != null) {
                try {
                    val = resolver.resolve(name);
                } catch (Exception e) {
                    log.debug("[PromptCatalog] resolver failed for '{}': {}", name, e.toString());
                }
            }
            if (val == null) val = DEFAULTS.get(name);

            if (val == null) {
                missing.add(name);
            } else {
                safe.put(name, val);
            }
        }

        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "Missing template variables " + missing +
                            " for " + res.getDescription() +
                            " (provided=" + safe.keySet() + ", defaults=" + DEFAULTS.keySet() + ")"
            );
        }
        return safe;
    }

    /**
     * ① 문자열 프롬프트로 렌더링
     */
    public String renderToString(String key, Map<String, Object> vars, MissingVarResolver resolver) {
        Resource res = resolve(key);
        Map<String, Object> safe = ensureVarsStrict(res, vars, resolver);
        return PromptTemplate.builder().resource(res).renderer(renderer).build().render(safe);
    }

    public String renderToString(String key, Map<String, Object> vars) {
        return renderToString(key, vars, null);
    }

    /**
     * ② 시스템 프롬프트 생성
     */
    public Prompt createSystemPrompt(String key, Map<String, Object> vars, MissingVarResolver resolver) {
        Resource res = resolve(key);
        Map<String, Object> safe = ensureVarsStrict(res, vars, resolver);
        return SystemPromptTemplate.builder().resource(res).renderer(renderer).build().create(safe);
    }

    public Prompt createSystemPrompt(String key, Map<String, Object> vars) {
        return createSystemPrompt(key, vars, null);
    }

    /**
     * ③ 일반 프롬프트 생성
     */
    public Prompt createPrompt(String key, Map<String, Object> vars, MissingVarResolver resolver) {
        Resource res = resolve(key);
        Map<String, Object> safe = ensureVarsStrict(res, vars, resolver);
        return PromptTemplate.builder().resource(res).renderer(renderer).build().create(safe);
    }

    public Prompt createPrompt(String key, Map<String, Object> vars) {
        return createPrompt(key, vars, null);
    }
}

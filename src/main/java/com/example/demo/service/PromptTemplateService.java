package com.example.demo.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class PromptTemplateService {

    public String render(String templateName, Map<String, Object> vars) {
        try {
            var res = new ClassPathResource("prompts/" + templateName + ".st");
            String tpl = new String(res.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            for (var e : vars.entrySet()) {
                tpl = tpl.replace("{{" + e.getKey() + "}}", String.valueOf(e.getValue()));
            }
            return tpl;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

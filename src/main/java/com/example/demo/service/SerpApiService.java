package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SerpApiService {

    @Value("${serpapi.api-key}")
    private String apiKey;

    public JsonObject search(String query, String engine) {
        Map<String, String> params = Map.of(
                "q", query,
                "engine", engine,
                "api_key", apiKey,
                "num", "5" // 뉴스 기사 5개 제한
        );

        GoogleSearch search = new GoogleSearch(params);
        return search.getJson();getJson
    }
}


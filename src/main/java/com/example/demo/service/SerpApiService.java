package com.example.demo.service;

import serpapi.GoogleSearch;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import serpapi.SerpApiSearchException;

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
                "num", "5"
        );

        GoogleSearch search = new GoogleSearch(params);
        try {
            return search.getJson();
        } catch (SerpApiSearchException e) {
            throw new RuntimeException("SerpAPI 호출 실패", e);
        }
    }
}


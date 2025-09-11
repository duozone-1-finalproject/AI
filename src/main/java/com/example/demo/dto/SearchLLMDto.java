package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchLLMDto {
    private List<Item> articles;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String keyword;
        private String title;
        private String url;
        private String source;
        private String date; // "YYYY-MM-DD"
    }
}


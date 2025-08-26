package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NewsResponseDto {
    private String title;
    private String content;
    private String date;
    private String source;
    private String url;
}


package com.example.demo.dto.graphvalidator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuideHitDto {
    private String id;      // chapId-secId-artId
    private String title;   // chapName - secName
    private String detail;  // content highlight or snippet
}
package com.example.demo.dto.graphvalidator;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckRequestDto {
    @NotBlank private String indutyName;
    @NotBlank private String section;
    @NotBlank private String draft;

}

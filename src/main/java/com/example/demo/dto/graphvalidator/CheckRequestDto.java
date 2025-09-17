package com.example.demo.dto.graphvalidator;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckRequestDto {
    @JsonProperty("induty_name")
    @NotBlank private String indutyName;
    @NotBlank private String section;
    @NotBlank private String draft;

}

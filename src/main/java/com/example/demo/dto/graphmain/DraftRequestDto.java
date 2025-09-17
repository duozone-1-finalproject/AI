package com.example.demo.dto.graphmain;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DraftRequestDto {
    @NotBlank private String corpCode;
    @NotBlank private String corpName;
    @NotBlank private String indutyCode;
    @NotBlank private String indutyName;
}

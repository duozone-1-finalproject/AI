package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DraftRequestDto {
    @NotBlank private String corpCode;
    @NotBlank private String corpName;
    @NotBlank private String indutyCode;
    @NotBlank private String indutyName;
    @NotNull  private Boolean rptExist;
}

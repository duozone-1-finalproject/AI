package com.example.demo.dto.graphmain;

import lombok.Data;

@Data
public class DraftResponseDto {
    private String riskIndustry;
    private String riskCompany;
    private String riskEtc;

    /** 섹션 키로 라우팅해서 해당 필드에 값 설정 */
    public void setBySectionKey(String key, String value) {
        switch (key) {
            case "risk_industry" -> this.riskIndustry = value;
            case "risk_company"  -> this.riskCompany  = value;
            case "risk_etc"      -> this.riskEtc      = value;
            default -> throw new IllegalArgumentException("Unknown section: " + key);
        }
    }
}

package com.example.demo.dto.graphweb;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class ValidationResultDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<Review> reviews;
    private Best best;
    private FinalResult finalResult;

    @Data
    @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
    public static class Review implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String title;
        private Gate gate;
        private Scores scores;
        private AdSpamLevel adSpamLevel;   // "none" | "soft" | "strong"
        private SourceTier sourceTier;     // "trustedTextHint" | "semiTextHint" | "unknownText"
        private BigDecimal total100;       // number
        private Decision decision;         // "YES" | "NO"
        private List<String> reasons;      // ["...", "..."]
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
    public static class Gate implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private boolean lengthOk; // length_ok -> lengthOk
        private boolean passed;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
    public static class Scores implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Integer topicFit70; // 0..70
        private Integer source20;    // 10|14|20
        private Integer adSpam10;    // 0|4|10
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
    public static class Best implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Integer index;
        private BigDecimal total100;
        private Decision decision;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
    public static class FinalResult implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private boolean validated;
        private String contextHint;
    }

    /**
     * "YES" | "NO"
     */
    public enum Decision {YES, NO}

    /**
     * "none" | "soft" | "strong" — JSON 문자열과 1:1 매칭을 위해 소문자 camel 유지
     */
    public enum AdSpamLevel {none, soft, strong}

    /**
     * "trustedTextHint" | "semiTextHint" | "unknownText"
     */
    public enum SourceTier {trustedTextHint, semiTextHint, unknownText}
}

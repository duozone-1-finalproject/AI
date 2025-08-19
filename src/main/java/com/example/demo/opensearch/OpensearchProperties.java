package com.example.demo.opensearch;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.Duration;
import java.util.List;

@Data
@Validated
@ConfigurationProperties(prefix = "opensearch")
public class OpensearchProperties {

    /** 연결할 호스트들 (기본: 로컬) */
    @NotEmpty
    private List<URI> uris;

    /** 타임아웃 & 커넥션 풀 */
    private Duration connectionTimeout = Duration.ofSeconds(5);
    private Duration socketTimeout = Duration.ofSeconds(60);
    private int maxConnTotal = 100;
    private int maxConnPerRoute = 100;
}
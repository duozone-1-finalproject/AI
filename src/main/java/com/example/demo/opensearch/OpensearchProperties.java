package com.example.demo.opensearch;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@Getter @Setter
@ConfigurationProperties(prefix = "spring.opensearch")
public class OpensearchProperties {
    private List<String> uris;
    private Duration connectionTimeout;
    private Duration socketTimeout;
    private int maxConnTotal;
    private int maxConnPerRoute;
    private boolean sniffEnabled; // (Apache HC5 transport에는 스니퍼 미사용)
}

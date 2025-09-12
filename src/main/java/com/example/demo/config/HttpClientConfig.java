package com.example.demo.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {

    @Bean
    RestClientCustomizer useApacheHttpClient5() {
        return (RestClient.Builder builder) -> {

            // 1) 연결/소켓 타임아웃은 ConnectionConfig로
            ConnectionConfig connCfg = ConnectionConfig.custom()
                    .setConnectTimeout(Timeout.ofSeconds(15))   // TCP+TLS 수립까지
                    .setSocketTimeout(Timeout.ofSeconds(300))    // 소켓 read/write 대기
                    .build();

            PoolingHttpClientConnectionManager cm =
                    PoolingHttpClientConnectionManagerBuilder.create()
                            .setDefaultConnectionConfig(connCfg)
                            .setMaxConnTotal(200)
                            .setMaxConnPerRoute(50)
                            .setValidateAfterInactivity(TimeValue.ofSeconds(5)) // stale conn 방지
                            .build();

            // 2) 응답/풀대기 타임아웃은 RequestConfig로
            RequestConfig reqCfg = RequestConfig.custom()
                    .setResponseTimeout(Timeout.ofSeconds(300))      // 서버 응답 대기
                    .setConnectionRequestTimeout(Timeout.ofSeconds(20)) // 풀에서 커넥션 빌리는 대기
                    .build();

            CloseableHttpClient http = HttpClients.custom()
                    .setConnectionManager(cm)
                    .setDefaultRequestConfig(reqCfg)
                    .evictExpiredConnections()
                    .evictIdleConnections(TimeValue.ofMinutes(1))
                    .build();

            builder.requestFactory(new HttpComponentsClientHttpRequestFactory(http));
        };
    }
}
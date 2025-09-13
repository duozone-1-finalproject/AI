package com.example.demo.opensearch;

import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(OpensearchProperties.class)
public class OpensearchConfig {

    private final OpensearchProperties props;

    /**
     * URI 그대로 HttpHost 생성, 포트 없이도 안전
     */
    private HttpHost toHost(String uriStr) {
        try {
            return HttpHost.create(uriStr);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URI for OpenSearch: " + uriStr, e);
        }
    }

    @Bean(destroyMethod = "close")
    public OpenSearchTransport openSearchTransport() {
        HttpHost[] hosts = props.getUris().stream()
                .map(this::toHost)
                .toArray(HttpHost[]::new);

        var builder = ApacheHttpClient5TransportBuilder.builder(hosts);

        PoolingAsyncClientConnectionManager cm = PoolingAsyncClientConnectionManagerBuilder.create()
                .setMaxConnTotal(props.getMaxConnTotal())
                .setMaxConnPerRoute(props.getMaxConnPerRoute())
                .build();

        builder.setHttpClientConfigCallback(http -> http
                .setConnectionManager(cm)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(Timeout.ofMilliseconds(props.getConnectionTimeout().toMillis()))
                        .setResponseTimeout(Timeout.ofMilliseconds(props.getSocketTimeout().toMillis()))
                        .build())
        );

        return builder.build();
    }

    @Bean
    public OpenSearchClient openSearchClient(OpenSearchTransport transport) {
        return new OpenSearchClient(transport);
    }
}

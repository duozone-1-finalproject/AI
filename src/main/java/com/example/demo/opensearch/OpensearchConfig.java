package com.example.demo.opensearch;

import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.config.ConnectionConfig;
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

import java.net.URI;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(OpensearchProperties.class)
public class OpensearchConfig {

    private final OpensearchProperties props;

    // Config 1: cm Bean 분리 (스프링이 소유)
    @Bean(destroyMethod = "close")
    public PoolingAsyncClientConnectionManager osConnectionManager(OpensearchProperties props) {
        ConnectionConfig connConfig = ConnectionConfig.custom()
                .setConnectTimeout(toTimeout(props.getConnectionTimeout()))
                .setSocketTimeout(toTimeout(props.getSocketTimeout()))
                .build();

        return PoolingAsyncClientConnectionManagerBuilder.create()
                .setMaxConnTotal(props.getMaxConnTotal())
                .setMaxConnPerRoute(props.getMaxConnPerRoute())
                .setDefaultConnectionConfig(connConfig)
                .build();
    }

    @Bean(destroyMethod = "close")
    public OpenSearchTransport openSearchTransport(PoolingAsyncClientConnectionManager cm) {
        HttpHost[] hosts = props.getUris().stream().map(this::toHttpHost).toArray(HttpHost[]::new);

        ApacheHttpClient5TransportBuilder builder =
                ApacheHttpClient5TransportBuilder.builder(hosts)
                        .setRequestConfigCallback(req -> req
                                // connectTimeout은 deprecated → ConnectionConfig로 옮겼고,
                                // 여긴 responseTimeout만 유지
                                .setResponseTimeout(toTimeout(props.getSocketTimeout())))
                        .setHttpClientConfigCallback(http -> {
                            http.setConnectionManager(cm);
                            http.setConnectionManagerShared(true); // ← 클라이언트가 cm를 닫지 않음
                            return http;
                        });

        return builder.build();
    }

    @Bean
    public OpenSearchClient openSearchClient(OpenSearchTransport transport) {
        return new OpenSearchClient(transport);
    }

    // ===== Helpers =====
    private HttpHost toHttpHost(URI uri) {
        int port = (uri.getPort() == -1)
                ? ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 9200)
                : uri.getPort();
        return new HttpHost(uri.getScheme(), uri.getHost(), port);
    }

    private Timeout toTimeout(Duration d) {
        return Timeout.ofMilliseconds(d.toMillis());
    }
}

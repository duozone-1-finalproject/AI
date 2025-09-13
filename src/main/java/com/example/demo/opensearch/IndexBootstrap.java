package com.example.demo.opensearch;

import com.example.demo.opensearch.OpensearchProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;                      // ★ 로우레벨은 Apache 4.x HttpHost
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.RestClient;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class IndexBootstrap {

    private final OpenSearchClient client;       // 타입드(HC5)
    private final OpensearchProperties props;

    @PostConstruct
    public void init() throws Exception {
        for (Map.Entry<String, String> e : OpensearchIndexRegistry.indexToResource().entrySet()) {
            String index = e.getKey();
            String resource = e.getValue();

            boolean exists = client.indices().exists(b -> b.index(index)).value();
            if (exists) continue;

            // JSON 로드
            var res = new ClassPathResource(resource);
            String body = new String(res.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            // 로우레벨 RestClient로 PUT /{index} (raw JSON)
            List<HttpHost> hosts4 = props.getUris().stream()
                    .map(HttpHost::create)  // 포트 없이 안전하게 처리
                    .toList();

            try (RestClient low = RestClient.builder(hosts4.toArray(new HttpHost[0])).build()) {
                Request req = new Request("PUT", "/" + index);
                req.setJsonEntity(body);
                low.performRequest(req);
            }
        }
    }
}

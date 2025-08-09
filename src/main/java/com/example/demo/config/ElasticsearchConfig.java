package com.example.demo.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.RequiredArgsConstructor;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration @RequiredArgsConstructor
public class ElasticsearchConfig {

    @Value("${elasticsearch.url}")
    private String url;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // 로컬 개발: 보안 OFF + 단일노드 → 인증 헤더 불필요
        RestClient rest = RestClient.builder(org.apache.http.HttpHost.create(url)).build();
        ElasticsearchTransport transport = new RestClientTransport(rest, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
package com.example.demo.controller;

import com.example.demo.service.graphvalidator.NoriTokenService;
import com.example.demo.util.StandardSearchHelper;
import com.example.demo.graphvalidator.nodes.StandardRetrieverNode;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final NoriTokenService noriTokenService;
    private final StandardRetrieverNode standardRetrieverNode;
    private final OpenSearchClient client;
    private final StandardSearchHelper standardSearchHelper;

    @Qualifier("chatWithMcp")
    private final ChatClient chatWithMcp;

    /**
     * Nori 형태소 분석기 토큰화 요청 DTO
     */
    @Data
    public static class AnalyzeRequestDto {
        private String index;
        private String analyzer;
        private String text;
    }

    /**
     * SearchRequest 빌더 테스트용 DTO
     */
    @Data
    public static class SearchRequestTestDto {
        private String index;
        @JsonAlias("queryString") // snake_case 외에 camelCase(별칭)도 허용
        private String queryString;
        @JsonAlias("chapIds")     // snake_case 외에 camelCase(별칭)도 허용
        private List<String> chapIds;
    }

    @PostMapping("/analyze")
    public String analyze(@RequestBody AnalyzeRequestDto request) {
        return noriTokenService.join(request.getIndex(), request.getAnalyzer(), request.getText());
    }

    @PostMapping("/mcp")
    public Mono<String> mcp_chat(@RequestParam String q) {
        return Mono.fromCallable(() -> chatWithMcp.prompt(q).call().content())
                .subscribeOn(Schedulers.boundedElastic());
    }

//    ------------------------------------------------
//    이쪽 사용하려면 private에서 public으로 변경해야 사용가능
//    ------------------------------------------------
//    @PostMapping("/search-response")
//    public List<Map<String, String>> testSearchResponse(@RequestBody SearchRequestTestDto request) throws IOException {
//        SearchRequest searchRequest = standardRetrieverNode.buildSearchRequest(
//                request.getIndex(), request.getQueryString(), request.getChapIds());
//
//        // OpenSearch에 검색 요청을 보내고 결과를 가공하여 반환합니다.
//        SearchResponse<Map> resp = client.search(searchRequest, Map.class);
//
//        return resp.hits().hits().stream().map(standardSearchHelper::transformHit).toList();
//    }
//
//    @PostMapping("/search-request")
//    public String testSearchRequest(@RequestBody SearchRequestTestDto request) {
//        SearchRequest searchRequest = standardRetrieverNode.buildSearchRequest(
//                request.getIndex(), request.getQueryString(), request.getChapIds());
//
//        // SearchRequest 객체를 JSON 문자열로 직렬화하여 실제 쿼리 내용을 확인합니다.
//        StringWriter writer = new StringWriter();
//        JsonpMapper mapper = client._transport().jsonpMapper();
//        try (JsonGenerator generator = mapper.jsonProvider().createGenerator(writer)) {
//            mapper.serialize(searchRequest, generator);
//        }
//        return writer.toString();
//    }
}

package com.example.demo.langgraph.nodes;

import com.example.demo.util.StandardSearchHelper;
import com.example.demo.langgraph.state.DraftState;
import com.example.demo.service.NoriTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito 기능을 JUnit5에서 사용하기 위한 어노테이션
class StandardRetrieverNodeTest {

    // @Mock: 가짜(Mock) 객체를 생성합니다.
    @Mock
    private OpenSearchClient client;

    @Mock
    private NoriTokenService noriTokenService;

    @Mock
    private StandardSearchHelper standardSearchHelper;

    // @InjectMocks: @Mock으로 생성된 객체들을 테스트 대상 클래스에 주입합니다.
    @InjectMocks
    private StandardRetrieverNode standardRetrieverNode;

    @Test
    @DisplayName("성공적인 검색 시, 상태에 가이드 결과와 인덱스를 정상적으로 추가한다")
    void apply_success() throws ExecutionException, InterruptedException, IOException {
        // --- 1. Given (준비) ---

        // 테스트에 사용할 DraftState 객체 생성
        DraftState state = new DraftState(new HashMap<>());
        state.put(DraftState.SECTION_LABEL, "사업위험");
        state.put(DraftState.DRAFT, "테스트 초안 텍스트");

        // Mock 객체들의 행동 정의
        // standardSearchHelper가 특정 입력에 대해 무엇을 반환할지 설정
        when(standardSearchHelper.pickIndex("사업위험")).thenReturn("risk_standard");
        when(standardSearchHelper.pickChapIds("사업위험")).thenReturn(List.of("5"));

        // noriTokenService가 무엇을 반환할지 설정
        when(noriTokenService.join(anyString(), anyString(), anyString())).thenReturn("토큰화된 텍스트");

        // OpenSearch 클라이언트의 search 메서드가 반환할 가짜 SearchResponse 설정
        // 복잡한 객체들은 Mockito.mock()으로 직접 생성할 수 있습니다.
        @SuppressWarnings("unchecked")
        SearchResponse<Map> mockResponse = mock(SearchResponse.class);
        @SuppressWarnings("unchecked")
        HitsMetadata<Map> mockHitsMetadata = mock(HitsMetadata.class);
        @SuppressWarnings("unchecked")
        Hit<Map> mockHit = mock(Hit.class);
        List<Hit<Map>> mockHits = List.of(mockHit);

        when(client.search(any(SearchRequest.class), eq(Map.class))).thenReturn(mockResponse);
        when(mockResponse.hits()).thenReturn(mockHitsMetadata);
        when(mockHitsMetadata.hits()).thenReturn(mockHits);

        // standardSearchHelper의 transformHit 메서드가 반환할 가공된 결과 설정
        Map<String, String> transformedHit = Map.of("id", "5-1-1", "title", "테스트 제목", "detail", "테스트 내용...");
        when(standardSearchHelper.transformHit(mockHit)).thenReturn(transformedHit);

        // --- 2. When (실행) ---

        // 테스트 대상 메서드 호출
        CompletableFuture<Map<String, Object>> future = standardRetrieverNode.apply(state);
        Map<String, Object> result = future.get(); // 비동기 작업이 완료될 때까지 기다림

        // --- 3. Then (검증) ---

        // 결과가 예상대로인지 확인
        assertNotNull(result);
        assertEquals("risk_standard", result.get(DraftState.GUIDE_INDEX));
        assertTrue(result.containsKey(DraftState.GUIDE_HITS));

        @SuppressWarnings("unchecked")
        List<Map<String, String>> guideHits = (List<Map<String, String>>) result.get(DraftState.GUIDE_HITS);
        assertEquals(1, guideHits.size());
        assertEquals(transformedHit, guideHits.get(0));
        assertNull(result.get(DraftState.ERRORS)); // 에러는 없어야 함

        // Mock 객체들이 예상대로 호출되었는지 검증
        verify(client, times(1)).search(any(SearchRequest.class), eq(Map.class));
        verify(standardSearchHelper, times(1)).transformHit(mockHit);
    }

    @Test
    @DisplayName("OpenSearch 검색 중 예외 발생 시, 상태에 에러 메시지를 기록한다")
    void apply_exception() throws ExecutionException, InterruptedException, IOException {
        // --- 1. Given (준비) ---

        // 테스트에 사용할 DraftState 객체 생성
        DraftState state = new DraftState(new HashMap<>());
        state.put(DraftState.SECTION_LABEL, "사업위험");
        state.put(DraftState.DRAFT, "테스트 초안 텍스트");

        // 헬퍼와 서비스의 기본 동작 설정
        when(standardSearchHelper.pickIndex(anyString())).thenReturn("risk_standard");
        when(noriTokenService.join(anyString(), anyString(), anyString())).thenReturn("토큰화된 텍스트");

        // OpenSearch 클라이언트가 search를 호출하면 RuntimeException을 던지도록 설정
        when(client.search(any(SearchRequest.class), eq(Map.class)))
                .thenThrow(new RuntimeException("OpenSearch connection failed"));

        // --- 2. When (실행) ---

        CompletableFuture<Map<String, Object>> future = standardRetrieverNode.apply(state);
        Map<String, Object> result = future.get();

        // --- 3. Then (검증) ---

        assertNotNull(result);
        assertTrue(result.containsKey(DraftState.ERRORS));
        String errorMessage = (String) result.get(DraftState.ERRORS);
        assertTrue(errorMessage.contains("OpenSearch connection failed"));

        // 에러가 발생했으므로 결과는 없어야 함
        assertNull(result.get(DraftState.GUIDE_HITS));
        assertNull(result.get(DraftState.GUIDE_INDEX));
    }
}
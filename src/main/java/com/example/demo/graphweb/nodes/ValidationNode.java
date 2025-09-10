/*
1단계: Rule 기반 검증
- 단순 규칙으로 광고성/홍보성 기사 필터링
- 출처(source) 신뢰도 체크
2단계: Embedding 기반 점수 검증
- Rule을 통과한 기사에 대해서만 Embedding 생성
- 기준 벡터(예: "위험요소 관련 기사")와 cosine similarity 계산
- 점수가 threshold 이상인 경우만 적합하다고 판정
 */

/*
package com.example.demo.langgraph.web.nodes;

import com.example.demo.webgraph.state.web.WebState;
import org.bsc.langgraph4j.action.AsyncNodeAction;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ValidationNode implements AsyncNodeAction<WebState> {

@Component
public class ValidationNode implements AsyncNodeAction<WebState> {

    private final EmbeddingService embeddingService; // 예: OpenAI, HuggingFace 등
    private static final double SIMILARITY_THRESHOLD = 0.75;

    @Override
    public CompletableFuture<Map<String, Object>> apply(WebState state) {
        List<WebResponseDto.Article> beforev = state.getBeforev();
        List<WebResponseDto.Article> articles = state.getArticles();
        List<WebResponseDto.Article> finalList = new ArrayList<>();

        for (WebResponseDto.Article art : beforev) {
            if (isRuleValid(art) && isEmbeddingValid(art)) {
                finalList.add(art);
            } else {
                // 대체 기사 찾기: 날짜 빠른 순
                articles.stream()
                        .filter(a -> a.getKeyword().equals(art.getKeyword()))
                        .sorted(Comparator.comparing(WebResponseDto.Article::getDate))
                        .forEachOrdered(alt -> {
                            if (isRuleValid(alt) && isEmbeddingValid(alt)) {
                                finalList.add(alt);
                            }
                        });
            }
        }

        return CompletableFuture.completedFuture(Map.of(WebState.FINAL_RESULT, finalList));
    }

1단계: Rule 기반 검증
private boolean isRuleValid(WebResponseDto.Article article) {
    if (article.getContent() == null || article.getContent().isBlank()) return false;

    String text = article.getContent();
    // 광고성 키워드 필터링
    if (text.contains("구매") || text.contains("이벤트") || text.contains("문의") || text.contains("보도자료")) {
        return false;
    }
    // 신뢰할 수 없는 출처 제외
    if ("기타".equals(article.getSource()) || "블로그".equals(article.getSource())) {
        return false;
    }
    return true;
}

2단계: Embedding 기반 검증
private boolean isEmbeddingValid(WebResponseDto.Article article) {
    try {
        String content = article.getContent();

        // 기사 본문 임베딩
        double[] articleEmbedding = embeddingService.embed(content);

        // 기준 벡터 (예: "리스크 관련 기사")
        double[] referenceEmbedding = embeddingService.embed("기업 및 산업 위험 요인 분석 기사");

        // cosine similarity 계산
        double score = cosineSimilarity(articleEmbedding, referenceEmbedding);

        return score >= SIMILARITY_THRESHOLD;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

private double cosineSimilarity(double[] a, double[] b) {
    double dot = 0.0, normA = 0.0, normB = 0.0;
    for (int i = 0; i < a.length; i++) {
        dot += a[i] * b[i];
        normA += Math.pow(a[i], 2);
        normB += Math.pow(b[i], 2);
    }
    return dot / (Math.sqrt(normA) * Math.sqrt(normB));
}
}
*/
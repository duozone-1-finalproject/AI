package com.example.demo.langgraph.state;

import com.example.demo.dto.ArticleMeta;
import com.example.demo.dto.ArticleFull;
import com.example.demo.dto.SummaryDto;
import com.example.demo.dto.ReportDto;
import com.example.demo.dto.ErrorEntry;
import java.util.ArrayList;
import java.util.List;

/**
 * LangGraph 실행 중 유지되는 State
 * 각 노드가 값을 채워넣고, 다음 노드가 읽어서 사용
 */
public class NewsState {

    // 검색어 리스트
    private List<String> queries = new ArrayList<>();

    // 뉴스 메타데이터 (title, link, source, date)
    private List<ArticleMeta> articlesMeta = new ArrayList<>();

    // 본문까지 포함된 기사 데이터
    private List<ArticleFull> articlesFull = new ArrayList<>();

    // 요약 결과
    private List<SummaryDto> summaries = new ArrayList<>();

    // 최종 집계 리포트
    private ReportDto report;

    // 에러 기록
    private List<ErrorEntry> errors = new ArrayList<>();

    // ===== Getter/Setter =====
    public List<String> getQueries() { return queries; }
    public void setQueries(List<String> queries) { this.queries = queries; }

    public List<ArticleMeta> getArticlesMeta() { return articlesMeta; }
    public void setArticlesMeta(List<ArticleMeta> articlesMeta) { this.articlesMeta = articlesMeta; }

    public List<ArticleFull> getArticlesFull() { return articlesFull; }
    public void setArticlesFull(List<ArticleFull> articlesFull) { this.articlesFull = articlesFull; }

    public List<SummaryDto> getSummaries() { return summaries; }
    public void setSummaries(List<SummaryDto> summaries) { this.summaries = summaries; }

    public ReportDto getReport() { return report; }
    public void setReport(ReportDto report) { this.report = report; }

    public List<ErrorEntry> getErrors() { return errors; }
    public void setErrors(List<ErrorEntry> errors) { this.errors = errors; }
}

package com.example.demo.dto;

//검색 관련
class NewsSearchRequestDto { String query; String section; }
class NewsArticleDto { String title; String link; }
class NewsSearchResponseDto { String section; List<NewsArticleDto> articles; LocalDateTime searchTime; }

//크롤링 관련
class CrawlingRequestDto { List<NewsArticleDto> articles; }
class CrawledArticleDto { String title; String link; String content; }

// 요약 관련
class SummaryRequestDto { List<CrawledArticleDto> crawledArticles; }
class SummaryDto { String title; String summary; }

// 최종 작성(?) 관련
class ContentWritingRequestDto { String section; List<SummaryDto> summaries; }
class ContentWritingResponseDto { String section; String content; }

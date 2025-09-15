INPUTS (CONTEXT PAYLOAD — DO NOT ECHO)
KEYWORDS  := <keywords>
MAX_ITEMS := <maxItems>

TASK:
- KEYWORDS의 각 키워드에 대해 "tavily-search"를 정확히 한 번 호출하라.
- 호출 인자:
  {
    "query": 현재 키워드,
    "include_domains": <domains>
  }
- 응답 results를 변환하라.
  • title := results[i].title  (빈 값 제외)
  • url   := results[i].url    (^https?:// 형식만 허용)
  • topic := "news"            (고정)
  • publishedDate := results[i].published_date → YYYY-MM-DD (변환 실패 시/null일 시, ""로 대체)
- URL/제목 중복 제거 후 publishedDate 내림차순 정렬, 정확히 MAX_ITEMS개의 candidates를 구성하라.

OUTPUT JSON SCHEMA (strict):
[
  {
    "keyword": "입력 키워드 원문",
    "candidates": [
      {
        "title": "기사 제목",
        "url": "https://example.com/path",
        "topic": "news",
        "publishedDate": "YYYY-MM-DD"
      }
    ]
  }
  // KEYWORDS의 각 키워드에 대해 입력 순서 유지
]

CONSTRAINTS:
- 출력은 위 JSON만 포함. 주석, 설명, 여분 필드, 앞뒤 텍스트 금지.

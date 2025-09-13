[입력]
- 회사명(CORP_NAME):= <corp_name>
- 산업명(IND_NAME):= <induty_name>
- 검색 키워드 리스트(KEYWORD_LIST):= <keywords>
- 키워드당 최대 결과 수(MAX_ITEMS):= 5

[무추론 원칙]
- 도구 호출 결과에 존재하지 않는 URL/제목/날짜를 **절대 생성하지 마세요.**
- 검색 결과가 부족하면 비워두세요. **MAX_ITEMS은 상한**일 뿐, 채우기 위해 추정 생성하지 않습니다.
- 특히 다음은 금지합니다: `example.com`, `example.net`, `example.org` 등 문서용 예시 도메인 사용. 결과가 없으면 빈 배열을 반환하세요. (추정 금지)

[작업 지시]
1) KEYWORD_LIST의 각 원본 KEYWORD를 그대로 사용해 DuckDuckgo MCP search 도구를 호출하세요(확장·번역·필터 금지).
2) KEYWORD_LIST의 각 원본 KEYWORD를 그대로 사용해 Tavily MCP search 도구를 호출하세요(확장·번역·필터 금지).
3) 한글로 된 국내 기사만 가져오도록 합니다.
4) 키워드별 검색 결과에서 URL 정규화 후 중복 제거, 최신성 우선으로 최대 MAX_ITEMS개만 남깁니다.
5) 각 항목에서 title, url, source, date(YYYY-MM-DD)를 추출합니다.
   - source 분류: 뉴스
   - date가 없거나 형식 불일치 시 해당 항목 제외.
6) 출력은 오직 JSON이며, 최상위는 배열입니다. 배열 원소는 아래 스키마를 따릅니다.
   - 각 키워드 객체는 { "keyword": KEYWORD, "candidates": [ ... ] } 형식.
   - 각 키워드의 candidates 길이는 지시문 상으로 MAX_ITEMS가 되도록 만듭니다.

[출력 JSON 스키마(그 외 텍스트 금지)]
[
  {
    "keyword": "KEYWORD",
    "candidates": [
      { "title":"...", "url":"https://...", "source":"뉴스", "date":"YYYY-MM-DD" }
    ]
  }
]
[입력]
- 회사명(CORP_NAME): <corp_name>
- 산업명(IND_NAME): <induty_name>
- 검색 키워드 리스트(KEYWORD_LIST): <keywords>   # QueryBuilderNode가 생성한 완성 쿼리들
- 섹션 구분(SECTION_LABEL): <section_label> # 결과값 보기 편하도록 추가
- 키워드당 최대 결과 수(MAX_ITEMS): 3

[작업 지시]
당신은 최고의 데이터 수집 전문가입니다. 다음 단계에 따라 작업을 수행하세요.

1. 검색 절차
- KEYWORD_LIST의 각 키워드를 그대로 사용해 MCP `search` 도구를 호출한다.
- 결과가 없으면 빈 배열로 둔다.
- 결과가 있으면 URL 중복을 제거하고 상위 MAX_ITEMS 개만 남긴다.
- 각 항목에서 title, url, date, source, section_label을 추출한다.

2. 데이터 처리 규칙
- 날짜(date): YYYY-MM-DD 형식. 불명확하면 null.
- 출처(source): URL 도메인을 기준으로 ["뉴스","블로그","논문","정부","기타"] 중 하나로 분류.

3. 출력 규칙
- 반드시 최상위 JSON 객체에 `articles` 배열을 포함해야 한다.
- 전체 길이는 (키워드 수 × MAX_ITEMS) 이하.
- JSON 외 텍스트/마크다운/코드블록은 포함하지 않는다.

[최종 출력 JSON 스키마]
{
"articles": [
{
"keyword": "원본 키워드",
"section_label": "사업위험 또는 회사위험",
"title": "제목",
"url": "https://example.com/news/123",
"date": "YYYY-MM-DD 또는 null",
"source": "뉴스|블로그|논문|정부|기타"
}
]

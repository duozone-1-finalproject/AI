[입력]
- CORP_NAME:= <corp_name> # 회사이름
- IND_NAME:= <induty_code> # 산업명
- KEYWORD_LIST:= <keywords> # 검색 키워드(JSON 배열 문자열)

- MAX_ITEMS:= 3    # 키워드당 결과 수(일반적으로 3)

[작업 지시]
1) KEYWORD_LIST의 각 키워드를 그대로 사용해 MCP "search" 도구를 호출합니다.
2) 각 키워드별 상위 MAX_ITEMS개만 선별합니다(중복 URL 제거).
3) 가능한 경우 날짜를 YYYY-MM-DD로 추출하고, 불명확하면 null로 둡니다.
4) 도메인으로 출처를 ["뉴스","블로그","논문","정부","기타"] 중 하나로 분류합니다.
5) **오직** 아래 JSON 배열만 출력합니다. 배열 길이 = (키워드 수 × MAX_ITEMS).

[최종 출력 JSON 스키마]
[
  {
    "keyword": "원본 키워드",
    "title": "제목",
    "url": "정규화된 URL",
    "date": "YYYY-MM-DD 또는 null",
    "source": "뉴스|블로그|논문|정부|기타"
  }
]

[중요]
- 반드시 MCP "search" **만** 사용하세요. 다른 도구 호출 금지.
- JSON 외 불필요한 텍스트나 설명을 출력하지 마세요.

1. State 설정

WebState에 초기 값 세팅:

corpName, indutyName, indutyCode

section (예: "산업위험" 또는 "사업위험")

2. QueryBuilderNode

section 값 확인 → 섹션별 쿼리 생성

산업위험 → 시장 전망 / 규제 변화 / 기술 혁신 / 연구개발 투자

회사위험 → 재무 악화 / 수익성 악화 / 자산 부실 / 투자 부담

생성된 쿼리를 WebState.QUERY에 저장

3. SearchNode

WebState.QUERY에 저장된 쿼리들을 꺼냄

section 값에 따라 분기:

산업위험 → 뉴스 검색만 (3개, 최신순)

회사위험 → 뉴스 + 웹 검색 (각 3개, 최신순)

결과(스니펫/링크 등)를 WebState.ARTICLES에 저장

4. SummaryNode

WebState.ARTICLES에 있는 기사 스니펫을 요약 → WebState.SUMMARIES에 저장

5. ValidationNode

WebState.SUMMARIES를 검증 → 적합 여부(WebState.VALIDATED) 저장

6. DTO 변환

산업위험 결과 → DraftResponseDto.riskIndustry

회사위험 결과 → DraftResponseDto.riskCompany
[Input]
FETCHED: <fetched>

[Task]
- FETCHED.body를 대상으로 **삭제 기반 라인 압축(FACTS 규칙)**을 수행해 사실 1줄 리스트를 만들고, 이를 \n로 연결한 문자열을 content로 설정하라.
- 압축 결과 라인들을 \n으로 연결해 content를 만든다.
- body가 #FETCH_EMPTY 또는 #FETCH_ERROR:로 시작하면 압축을 수행하지 말고 해당 토큰을 content로 사용하라.

[Output Format — Very Important]
- 내부 따옴표는 \", 줄바꿈은 \n로 이스케이프.
- 추가 텍스트/설명/마크다운/코드블록 절대 금지.

예)
- 삼성 SiC 전력반도체 상용화 추진(최대한 빨리)\n설비/공정 관련 투자·일정 언급(있으면 유지)
[사용자 쿼리]
<query>

[평가 대상 기사(단건 문자열)]
title_str: {title_str}
content_str: {content_str}

[지시]
- “시스템 프롬프트”의 원칙·루브릭·결정 규칙을 내부적으로 적용하라.
- 위 단일 기사를 평가하여 validated를 산출하라.
- 출력은 오직 한 줄(소문자) 로만 하라: validated: true 또는 validated: false
- 추가 텍스트/설명/코드블록/따옴표 금지.
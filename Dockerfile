# Java 21 런타임 베이스
FROM eclipse-temurin:21-jdk

# 1) Python + Node.js/npm(npx) 설치
RUN apt-get update \
 && apt-get install -y --no-install-recommends \
      python3 python3-venv python3-pip \
      nodejs npm \
 && npm i -g mcp-remote \
 && rm -rf /var/lib/apt/lists/*

# 2) MCP 전용 venv 생성 및 DuckDuckGo MCP 서버 설치
RUN python3 -m venv /opt/mcp/.mcpvenv \
 && /opt/mcp/.mcpvenv/bin/pip install --no-cache-dir --upgrade pip \
 && /opt/mcp/.mcpvenv/bin/pip install --no-cache-dir duckduckgo-mcp-server

# 3) 애플리케이션 JAR 배포
WORKDIR /app
COPY build/libs/*.jar app.jar

# 4) 스프링 부트 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

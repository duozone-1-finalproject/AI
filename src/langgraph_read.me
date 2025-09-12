증권신고서 작성 AI 서비스 -> AI read.me

## 1. 프로젝트 개요

본 프로젝트는 사용자의 요청에 따라 **뉴스, 웹, 데이터베이스 등 여러 소스에서 동적으로 정보를 수집**하고, LLM을 활용하여
"체계적인 리포트 초안을 자동으로 생성"하는 지능형 에이전트 시스템입니다.

`application.yml` 설정 파일을 통해 코드 수정 없이도 전체 작업 흐름(Workflow)을 유연하게 변경할 수 있으며,
LangGraph(langgraph4j) 프레임워크를 기반으로 조건인식-실행-검증-수정 사이클을 구현하여 결과물의 품질과 안정성을 보장합니다.

## 2. 핵심 기능

-   **동적 워크플로우**: `application.yml` 설정을 통해 리포트의 구조, 데이터 소스, 프롬프트를 자유롭게 조합하고 변경할 수 있습니다.
-   **다중 소스 데이터 통합**: 뉴스(SerpApi), 웹 검색, 내부 데이터베이스 등 여러 소스의 정보를 종합하여 리포트를 작성합니다.
-   **에이전트 기반 아키텍처**: LangGraph를 사용하여 '자료 수집', '초안 작성', '품질 검수', '재시도' 등 각 단계가 명확하게 분리된 견고한 워크플로우를 가집니다.
-   **외부 서비스 연동**: Python(FastAPI)으로 구현된 크롤링 서버, SerpApi 등 외부 서비스와 안정적으로 통신합니다.
-   **모듈화된 설계**: Controller, Service, Node, State 등 각 컴포넌트의 역할이 명확히 분리되어 유지보수와 확장이 용이합니다.

## 3. 프로젝트 아키텍처

본 프로젝트는 다음과 같은 흐름으로 동작합니다.


## 4. 주요 컴포넌트 설명

### 📁 `src/main/java/com/example/demo`

#### 📄 `controller`
애플리케이션의 진입점(Entry Point) 역할을 합니다.

-   **`GraphController.java`**: 리포트 초안 생성 요청(`POST /graph`)을 받아 `GraphService`에 작업을 위임합니다.
-   **`SubCrawlingController.java`**: Python FastAPI로 구현된 외부 크롤링 서버를 호출하는 프록시(Proxy) 역할을 합니다. (`GET /crawl/news`)

#### 📄 `service`
핵심 비즈니스 로직을 담당합니다.

-   **`GraphService.java` (인터페이스)** & **`impl/GraphServiceImpl.java` (구현체)**:
    -   이 시스템의 **총괄 지휘자(Orchestrator)**입니다.
    -   `application.yml`의 설정을 해석하여 전체 작업 순서를 결정하고, LangGraph 에이전트를 실행합니다.
-   **`SerpApiService.java`**: 외부 검색 엔진 API(SerpApi)를 호출하여 뉴스 기사 등을 검색합니다.
-   **`NewsCrawlerService.java`**: Jsoup 라이브러리를 사용하여 뉴스 기사 URL의 HTML 본문을 추출(크롤링)합니다.

#### 📄 `langgraph`
**AI 에이전트의 두뇌**에 해당하는 부분으로, LangGraph 프레임워크의 핵심 컴포넌트들이 위치합니다.

-   **`state/DraftState.java`**:
    -   그래프의 **중앙 메모리** 또는 **"스마트 작업 바구니"** 역할을 합니다.
    -   그래프의 모든 작업 단계를 거치면서 데이터(사용자 요청, 검색 결과, 생성된 초안, 재시도 횟수 등)를 저장하고 전달합니다.
    -   `SCHEMA`를 통해 각 데이터의 특성(덮어쓰기/누적)을 정의하여 상태를 안정적으로 관리합니다.

-   **`nodes/`**: 그래프의 각 작업 단계를 수행하는 **"전문 작업자"**들입니다.
    -   `SourceSelectorNode.java`: 리포트 작성에 필요한 데이터 소스(news, db 등)를 결정하는 **계획 수립 노드**.
    -   `ContextAggregatorNode.java`: 결정된 모든 소스로부터 실제 데이터를 수집하고 하나로 통합하는 **자료 수집 노드**.
    -   `PromptSelectorNode.java`: 수집된 자료의 내용에 따라 가장 적절한 LLM 프롬프트를 선택하는 **전략 수립 노드**.
    -   `DraftGeneratorNode.java`: 수집된 자료와 프롬프트를 바탕으로 LLM을 호출하여 실제 초안을 작성하는 **초안 작성 노드**.
    -   `GlobalValidatorNode.java`: 생성된 초안이 요구사항(글자 수, 형식 등)에 맞는지 검증하는 **품질 검수 노드**.
    -   `RetryAdjustNode.java`: 검수에 실패했을 경우, 재작성을 위해 프롬프트 수정 및 재시도 횟수를 조정하는 **수정/재시도 노드**.

-   **`DraftGraphWiring.java`**:
    -   그래프의 **전체 조립 설계도**입니다.
    -   모든 노드(작업자)를 그래프에 등록하고, 작업 순서를 연결합니다.
    -   `GlobalValidatorNode`의 검수 결과에 따라 "성공 시 종료" 또는 "실패 시 재시도"로 흐름을 나누는 **조건부 분기** 로직을 정의합니다.

#### 📄 `config`
애플리케이션의 설정을 관리합니다.

-   **`AiProperties.java`**: `application.yml` 파일의 `ai` 관련 설정들을 Java 객체로 매핑하여 타입-세이프(Type-safe)하게 사용할 수 있도록 돕습니다.

#### 📄 `dto`
계층 간 데이터 전송을 위한 객체(Data Transfer Object)들을 정의합니다.

-   `DraftRequestDto.java`, `DraftResponseDto.java`, `NewsResponseDto.java` 등

### 📁 `src/main/resources`

-   **`application.yml`**:
    -   이 프로젝트의 **가장 중요한 동적 설정 파일**입니다.
    -   리포트의 섹션별(`risk_industry`, `risk_company` 등) 작업 순서, 사용할 데이터 소스, 프롬프트 파일 경로 등을 정의합니다.
-   **`prompts/`**: LLM에 전달할 프롬프트 템플릿(`.st` 파일)들을 보관합니다.

## 5. 실행 방법

### 사전 준비

1.  Java 21 이상
2.  Gradle
3.  Python 3.6 및 FastAPI (외부 크롤링 서버용)
4.  프로젝트 루트에 `.env` 파일 생성 후, 필요한 API 키 설정
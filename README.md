# AI Repo

이 프로젝트는 LangGraph4j를 사용하여 초안 생성을 위한 그래프 기반 접근 방식을 사용하는 Spring Boot 애플리케이션입니다. 그래프의 각 노드는 초안 생성 프로세스에서 특정 작업을 수행하며, Opensearch와 통합하여 검색 기능을 활용합니다.

## 프로젝트 구조

```
src/main/java/com/example/demo
├── DemoApplication.java
├── config
│   ├── AiClientConfig.java
│   ├── AiSectionProperties.java
│   ├── AppConfig.java
│   ├── DotEnvConfig.java
│   └── PromptTemplateConfig.java
├── constants
│   └── CommonConstant.java
├── controller
│   ├── GraphController.java
│   └── TestController.java
├── dto
│   ├── ContextDoc.java
│   ├── DraftRequestDto.java
│   ├── DraftResponseDto.java
│   ├── GuideHitDto.java
│   └── ValidationDto.java
├── langgraph
│   ├── DraftGraphConfig.java
│   ├── nodes
│   │   ├── AdjustDraftNode.java
│   │   ├── ContextAggregatorNode.java
│   │   ├── DraftGeneratorNode.java
│   │   ├── GlobalValidatorNode.java
│   │   ├── PromptSelectorNode.java
│   │   ├── SourceSelectorNode.java
│   │   └── StandardRetrieverNode.java
│   └── state
│       └── DraftState.java
├── opensearch
│   ├── IndexBootstrap.java
│   ├── OpensearchConfig.java
│   ├── OpensearchIndexRegistry.java
│   └── OpensearchProperties.java
├── service
│   ├── GraphService.java
│   ├── GraphServiceImpl.java
│   ├── NoriTokenService.java
│   ├── NoriTokenServiceImpl.java
│   ├── PromptCatalogService.java
│   └── SourcePolicyService.java
└── util
    ├── HtmlUtils.java
    └── StandardSearchHelper.java
```

## 클래스 설명

### `com.example.demo`

-   **DemoApplication.java**: Spring Boot 애플리케이션의 기본 진입점입니다.

### `com.example.demo.config`

-   **AppConfig.java**: 일반적인 애플리케이션 구성 빈을 설정합니다.
-   **DotEnvConfig.java**: `.env` 파일에서 환경 변수를 로드합니다.
-   **AiClientConfig.java**: AI 클라이언트(예: OpenAI 또는 다른 LLM 제공업체)를 구성합니다.
-   **AiSectionProperties.java**: AI 처리의 여러 섹션에 대한 속성을 정의합니다.
-   **PromptTemplateConfig.java**: 프롬프트 템플릿을 관리하고 로드하기 위한 빈을 구성합니다.

### `com.example.demo.constants`

-   **CommonConstant.java**: 애플리케이션 전체에서 사용되는 공통 상수의 모음입니다.

### `com.example.demo.controller`

-   **TestController.java**: 테스트 목적으로 사용되는 컨트롤러입니다.
-   **GraphController.java**: LangGraph 그래프 실행을 트리거하는 엔드포인트를 제공합니다.

### `com.example.demo.dto`

-   **ContextDoc.java**: 컨텍스트의 문서를 나타냅니다.
-   **GuideHitDto.java**: 검색된 가이드 히트에 대한 DTO입니다.
-   **ValidationDto.java**: 유효성 검사 결과에 대한 DTO입니다.
-   **DraftRequestDto.java**: 초안 생성 요청을 위한 DTO입니다.
-   **DraftResponseDto.java**: 초안 생성 응답을 위한 DTO입니다.

### `com.example.demo.langgraph`

-   **DraftGraphConfig.java**: 노드와 엣지를 포함한 LangGraph 그래프의 구조를 정의합니다.

#### `com.example.demo.langgraph.nodes`

-   **AdjustDraftNode.java**: 유효성 검사 결과에 따라 생성된 초안을 조정하는 노드입니다.
-   **DraftGeneratorNode.java**: 초기 초안을 생성하는 노드입니다.
-   **PromptSelectorNode.java**: 생성 작업에 적합한 프롬프트를 선택하는 노드입니다.
-   **SourceSelectorNode.java**: 사용할 데이터 소스를 선택하는 노드입니다.
-   **GlobalValidatorNode.java**: 생성된 초안을 규칙 집합에 대해 유효성을 검사하는 노드입니다.
-   **ContextAggregatorNode.java**: 여러 소스에서 컨텍스트를 집계하는 노드입니다.
-   **StandardRetrieverNode.java**: 표준 정보나 가이드라인을 검색하는 노드입니다.

#### `com.example.demo.langgraph.state`

-   **DraftState.java**: 노드 간에 전달되는 모든 데이터를 보유하는 그래프의 상태를 나타냅니다.

### `com.example.demo.opensearch`

-   **IndexBootstrap.java**: 애플리케이션 시작 시 Opensearch 인덱스를 초기화합니다.
-   **OpensearchConfig.java**: Opensearch 클라이언트를 구성합니다.
-   **OpensearchProperties.java**: Opensearch 연결을 위한 속성을 정의합니다.
-   **OpensearchIndexRegistry.java**: Opensearch 인덱스 이름을 위한 레지스트리입니다.

### `com.example.demo.service`

-   **GraphService.java**: 그래프 실행 서비스를 위한 인터페이스입니다.
-   **GraphServiceImpl.java**: LangGraph 그래프를 실행하는 `GraphService`의 구현체입니다.
-   **NoriTokenService.java**: Nori 토크나이저를 사용하는 서비스를 위한 인터페이스입니다.
-   **SourcePolicyService.java**: Section에 대한 RAG 소스를 매핑하는 서비스입니다.
-   **NoriTokenServiceImpl.java**: `NoriTokenService`의 구현체입니다.
-   **PromptCatalogService.java**: 프롬프트를 관리하고 불러오는 서비스입니다.

### `com.example.demo.util`

-   **HtmlUtils.java**: HTML 처리를 위한 유틸리티 클래스입니다.
-   **StandardSearchHelper.java**: Opensearch에서 표준 검색을 수행하기 위한 헬퍼 클래스입니다.

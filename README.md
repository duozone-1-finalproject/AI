# AI Repo

이 프로젝트는 LangGraph4j를 사용하여 여러 서브그래프로 구성된 그래프 기반 접근 방식을 사용하는 Spring Boot 애플리케이션입니다. Kafka를 통해 외부 시스템과 비동기적으로 통신하며, OpenSearch를 활용하여 데이터를 검색하고, 각 서브그래프는 특정 비즈니스 로직(예: DB 데이터 조회, 초안 생성, 유효성 검증)을 수행합니다.

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
│   └── ...
├── controller
│   ├── GraphController.java
│   └── TestController.java
├── dto
│   └── ...
├── graphdb
│   ├── DbSubGraphConfig.java
│   ├── DbSubGraphState.java
│   └── nodes
│       ├── CorpCodeRetrievalNode.java
│       ├── DataPreprocessorNode.java
│       ├── FilterCriteriaNode.java
│       ├── FinancialsProcessorNode.java
│       └── ReportContentRetrievalNode.java
├── graphmain
│   ├── DraftGraphConfig.java
│   ├── DraftState.java
│   └── nodes
│       ├── ContextAggregatorNode.java
│       ├── DraftGeneratorNode.java
│       ├── PromptSelectorNode.java
│       └── SourceSelectorNode.java
├── graphvalidator
│   ├── ValidatorGraphConfig.java
│   ├── ValidatorState.java
│   └── nodes
│       ├── AdjustDraftNode.java
│       ├── GlobalValidatorNode.java
│       └── StandardRetrieverNode.java
├── kafka
│   └── KafkaReportConsumer.java
├── opensearch
│   ├── IndexBootstrap.java
│   ├── OpensearchConfig.java
│   └── ...
├── repository
│   └── OpenSearchRepository.java
├── service
│   └── ...
└── util
    └── ...
```

<details>
<summary>클래스 설명</summary>

### `com.example.demo`

-   **DemoApplication.java**: Spring Boot 애플리케이션의 기본 진입점입니다.

### `com.example.demo.config`

-   **AppConfig.java**: 일반적인 애플리케이션 구성 빈을 설정합니다.
-   **DotEnvConfig.java**: `.env` 파일에서 환경 변수를 로드합니다.
-   **AiClientConfig.java**: AI 클라이언트(예: OpenAI 또는 다른 LLM 제공업체)를 구성합니다.
-   **AiSectionProperties.java**: AI 처리의 여러 섹션에 대한 속성을 정의합니다.
-   **PromptTemplateConfig.java**: 프롬프트 템플릿을 관리하고 로드하기 위한 빈을 구성합니다.

### `com.example.demo.kafka`

-   **KafkaReportConsumer.java**: Kafka의 `ai-report-request` 토픽에서 메시지를 수신하여 AI 처리(그래프 실행)를 트리거하고, `ai-report-response` 토픽으로 결과를 전송하는 컨슈머입니다.

### `com.example.demo.graphdb`

OpenSearch에서 원시 데이터를 조회하고 전처리하는 서브그래프를 정의합니다.

-   **DbSubGraphConfig.java**: 데이터 조회 서브그래프의 노드와 엣지를 정의하는 설정 클래스입니다.
-   **DbSubGraphState.java**: 데이터 조회 서브그래프의 상태를 정의하며, 회사 코드, 산업 코드, 조회된 문서 등의 데이터를 관리합니다.

#### `com.example.demo.graphdb.nodes`

-   **FilterCriteriaNode.java**: 입력된 섹션에 따라 OpenSearch 쿼리를 위한 필터 기준을 생성합니다.
-   **FinancialsProcessorNode.java**: 회사의 재무 정보를 OpenSearch에서 조회하고 마크다운 형식으로 변환합니다.
-   **CorpCodeRetrievalNode.java**: 필터 기준에 맞는 동종업계 회사 코드를 OpenSearch에서 조회합니다.
-   **ReportContentRetrievalNode.java**: 조회된 회사 코드를 바탕으로 특정 섹션의 보고서 내용을 OpenSearch에서 가져옵니다.
-   **DataPreprocessorNode.java**: 조회된 원시 보고서 내용을 분석 및 사용에 용이한 `DbDocDto` 형태로 전처리합니다.

### `com.example.demo.graphmain`

여러 서브그래프를 호출하여 전체 초안 생성 프로세스를 관장하는 메인 그래프를 정의합니다.

-   **DraftGraphConfig.java**: 메인 그래프의 구조를 정의합니다. `SourceSelectorNode`를 통해 `dbSubGraph` 또는 웹 검색과 같은 다른 소스를 선택적으로 호출합니다.
-   **DraftState.java**: 메인 그래프의 상태를 정의하며, 최종적으로 생성될 초안과 관련된 모든 데이터를 관리합니다.

#### `com.example.demo.graphmain.nodes`

-   **SourceSelectorNode.java**: 사용할 데이터 소스(예: DB 검색, 웹 검색)를 선택하고 해당 서브그래프(`dbSubGraph`)를 호출합니다.
-   **ContextAggregatorNode.java**: 여러 소스(예: `dbSubGraph` 결과)에서 집계된 컨텍스트를 통합합니다.
-   **PromptSelectorNode.java**: 생성 작업에 적합한 프롬프트를 선택합니다.
-   **DraftGeneratorNode.java**: 통합된 컨텍스트를 바탕으로 LLM을 통해 초기 초안을 생성합니다.

### `com.example.demo.graphvalidator`

생성된 초안을 가이드라인에 따라 검증하고 수정하는 서브그래프를 정의합니다.

-   **ValidatorGraphConfig.java**: 초안 검증 서브그래프의 노드와 엣지를 정의합니다. 검증-수정-재검증의 루프 구조를 가집니다.
-   **ValidatorState.java**: 검증 서브그래프의 상태를 정의하며, 초안, 가이드라인, 검증 결과 등의 데이터를 관리합니다.

#### `com.example.demo.graphvalidator.nodes`

-   **StandardRetrieverNode.java**: 초안 내용과 관련된 가이드라인을 OpenSearch에서 검색합니다. (기존 `langgraph`에서 이동)
-   **GlobalValidatorNode.java**: LLM을 사용하여 초안이 검색된 가이드라인을 준수하는지 검증하고, 수정이 필요한 경우 이슈를 생성합니다. (기존 `langgraph`에서 이동)
-   **AdjustDraftNode.java**: `GlobalValidatorNode`가 생성한 이슈를 바탕으로 LLM을 통해 초안을 수정합니다. (기존 `langgraph`에서 이동)

### `com.example.demo.repository`

-   **OpenSearchRepository.java**: OpenSearch에 대한 실제 쿼리를 실행하는 리포지토리 클래스입니다. 동종업계 회사 조회, 보고서 내용 조회, 재무 정보 조회 등 다양한 검색 기능을 수행합니다.

### `com.example.demo.controller`

-   **GraphController.java**: `graphmain` 그래프 실행을 트리거하는 엔드포인트를 제공합니다.
-   **TestController.java**: 테스트 목적으로 사용되는 컨트롤러입니다.

### `com.example.demo.opensearch`

-   **IndexBootstrap.java**: 애플리케이션 시작 시 Opensearch 인덱스를 초기화합니다.
-   **OpensearchConfig.java**: Opensearch 클라이언트를 구성합니다.

</details>

## 라이선스

이 프로젝트는 MIT 라이선스에 따라 배포됩니다. 자세한 내용은 `LICENSE` 파일을 참조하십시오.

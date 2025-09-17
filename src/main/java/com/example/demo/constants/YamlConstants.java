package com.example.demo.constants;

import java.util.List;
import java.util.Map;

public class YamlConstants {

    public static final Map<String, String> FILTER_MAP = Map.of(
            "risk_industry", "industry",
            "risk_company", "financial",
            "risk_etc", "latest"
    );

    public static final Map<String, String> SECTION_MAP = Map.ofEntries(
            Map.entry("risk_core", "핵심투자위험"),
            Map.entry("off_gen", "모집 또는 매출에 관한 일반사항"),
            Map.entry("off_sum", "공모개요"),
            Map.entry("off_method", "공모방법"),
            Map.entry("off_price", "공모가격 결정방법"),
            Map.entry("off_proc", "모집 또는 매출절차 등에 관한 사항"),
            Map.entry("uw", "인수 등에 관한 사항"),
            Map.entry("rights", "증권의 주요 권리내용"),
            Map.entry("risk_industry", "사업위험"),
            Map.entry("risk_company", "회사위험"),
            Map.entry("risk_etc", "기타 투자위험"),
            Map.entry("uw_op", "인수인의 의견(분석기관의 평가의견)"),
            Map.entry("proceeds", "자금의 사용목적"),
            Map.entry("protect", "그 밖에 투자자보호를 위해 필요한 사항"),
            Map.entry("co_view", "회사의 개요"),
            Map.entry("co_hist", "회사의 연혁"),
            Map.entry("cap_chg", "자본금 변동사항"),
            Map.entry("shares", "주식의 총수 등"),
            Map.entry("aoi", "정관에 관한 사항"),
            Map.entry("biz_view", "사업의 개요"),
            Map.entry("products", "주요 제품 및 서비스"),
            Map.entry("raw_mat", "원재료 및 생산설비"),
            Map.entry("sales", "매출 및 수주상황"),
            Map.entry("risk_mgt", "위험관리 및 파생거래"),
            Map.entry("ctr_rnd", "주요계약 및 연구개발활동"),
            Map.entry("ref_oth", "기타 참고사항"),
            Map.entry("fin_sum", "요약재무정보"),
            Map.entry("fs_con", "연결재무제표"),
            Map.entry("fs_con_n", "연결재무제표 주석"),
            Map.entry("fs", "재무제표"),
            Map.entry("fs_n", "재무제표 주석"),
            Map.entry("dividend", "배당에 관한 사항"),
            Map.entry("finance", "증권의 발행을 통한 자금조달에 관한 사항"),
            Map.entry("fin_oth", "기타 재무에 관한 사항"),
            Map.entry("audit_ext", "외부감사에 관한 사항"),
            Map.entry("ctrl_int", "내부통제에 관한 사항"),
            Map.entry("bod", "이사회에 관한 사항"),
            Map.entry("audit_sys", "감사제도에 관한 사항"),
            Map.entry("sh_meet", "주주총회 등에 관한 사항"),
            Map.entry("sh_info", "주주에 관한 사항"),
            Map.entry("exec_stat", "임원 및 직원 등의 현황"),
            Map.entry("exec_comp", "임원의 보수 등"),
            Map.entry("affil", "계열회사 등에 관한 사항"),
            Map.entry("rel_trans", "대주주 등과의 거래내용"),
            Map.entry("disc_chg", "공시내용 진행 및 변경사항"),
            Map.entry("cont_liab", "우발부채 등에 관한 사항"),
            Map.entry("sanction", "제재 등과 관련된 사항"),
            Map.entry("post_event", "작성기준일 이후 발생한 주요사항 등 기타사항"),
            Map.entry("tables", "상세표"),
            Map.entry("other_notice", "기타 공지 사항")
    );

    public static final Map<String, List<String>> SOURCES_MAP = Map.of(
            "risk_industry", List.of("web", "db"),
            "risk_company", List.of("web", "db"),
            "risk_etc", List.of("db")
    );


    public static final List<String> DEFAULT_ORDER = List.of(
            "risk_industry",
            "risk_company",
            "risk_etc"
    );

    public static final Map<String, Object> DEFAULTS = Map.of(
            "maxItems", 5,
            "webRagItems", List.of(),
            "dartRagItems", List.of(),
            "financialData", "",
            "otherRiskInputs", List.of()
    );

    // 프롬프트 템플릿 위치 (ai.prompts)
    public static final Map<String, String> PROMPTS = Map.ofEntries(
            Map.entry("draft_sys", "classpath:prompts/draft_sys.st"),
            Map.entry("risk_industry", "classpath:prompts/risk_industry.st"),
            Map.entry("risk_company", "classpath:prompts/risk_company.st"),
            Map.entry("risk_etc", "classpath:prompts/risk_etc.st"),
            Map.entry("check_sys", "classpath:prompts/check_sys.st"),
            Map.entry("risk_industry_checklist", "classpath:prompts/risk_industry_checklist.st"),
            Map.entry("risk_company_checklist", "classpath:prompts/risk_company_checklist.st"),
            Map.entry("risk_etc_checklist", "classpath:prompts/risk_etc_checklist.st"),
            Map.entry("validator_sys", "classpath:prompts/validator_sys.st"),
            Map.entry("validator_user_risk", "classpath:prompts/validator_user_risk.st"),
            Map.entry("validator_user_default", "classpath:prompts/validator_user_default.st"),
            Map.entry("adjust_sys", "classpath:prompts/adjust_sys.st"),
            Map.entry("adjust_user", "classpath:prompts/adjust_user.st"),
            Map.entry("check_revise_sys", "classpath:prompts/check_revise_sys.st"),
            Map.entry("check_revise_user", "classpath:prompts/check_revise_user.st"),
            Map.entry("search_sys", "classpath:prompts/search_sys.st"),
            Map.entry("search_user", "classpath:prompts/search_user.st"),
            Map.entry("fetch_sys", "classpath:prompts/fetch_sys.st"),
            Map.entry("fetch_user", "classpath:prompts/fetch_user.st"),
            Map.entry("preprocess_sys", "classpath:prompts/preprocess_sys.st"),
            Map.entry("preprocess_user", "classpath:prompts/preprocess_user.st"),
            Map.entry("web_validator_sys", "classpath:prompts/web_validator_sys.st"),
            Map.entry("web_validator_user", "classpath:prompts/web_validator_user.st")

    );

}

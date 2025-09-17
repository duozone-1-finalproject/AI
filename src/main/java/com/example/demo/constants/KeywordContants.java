package com.example.demo.constants;

import java.util.List;

public final class KeywordContants {

    public static final String SECTION_BUSINESS = "사업위험";
    public static final String SECTION_COMPANY  = "회사위험";

    public static final List<String> BUS_KWD = List.of("시장 전망", "산업 동향");

    public static final List<String> COM_KWD = List.of("재무 상태", "지배 구조");

    //인스턴스화 방지?
    private KeywordContants() {
    }
}
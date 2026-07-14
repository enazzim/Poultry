package com.poultry.platform.domain;

/** 공고 유입 경로. MES는 소스일 뿐 소유자는 항상 Organization(FARM). */
public enum ListingSource {
    /** 플랫폼 UI에서 농가가 직접 등록 (MES 미연동 농가 포함) */
    MANUAL,
    /** 외부 MES(EggFactory) webhook — farmCode로 매핑된 연동 농가만 */
    MES
}

package com.escapemanor.core;

/** 모든 방에서 공통으로 사용하는 아이템 ID. 팀원은 임의의 문자열 대신 이 ID 사용. */
public enum ItemId {
    BEDROOM_STUDY_KEY("침실·서재 열쇠", "침실과 서재의 퍼즐로 얻는 열쇠"),
    KITCHEN_STORAGE_KEY("주방·저장고 열쇠", "주방과 저장고의 퍼즐로 얻는 열쇠"),
    DINING_ENTRANCE_KEY("식당·현관 열쇠", "식당과 현관의 퍼즐로 얻는 열쇠");

    private final String title;
    private final String detail;
    ItemId(String title, String detail) { this.title = title; this.detail = detail; }
    public String title() { return title; }
    public String detail() { return detail; }
    @Override public String toString() { return title; }
}

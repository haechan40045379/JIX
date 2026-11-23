package com.escapemanor.core;

/** 방 ID는 팀 전체에서 고정. 화면의 순서는 임시 테스트 순서이며 실제 맵은 추후 별도 정의. */
public enum RoomId {
    BEDROOM("침실", "bedroom.png"),
    STUDY("서재", "study.png"),
    KITCHEN("주방", "kitchen.png"),
    STORAGE("저장고", "storage.png"),
    DINING("식당", "dining.png"),
    ENTRANCE("현관", "entrance.png");

    private final String displayName;
    private final String imageFile;
    RoomId(String displayName, String imageFile) {
        this.displayName = displayName;
        this.imageFile = imageFile;
    }
    public String displayName() { return displayName; }
    public String imagePath() { return "/images/rooms/" + imageFile; }
    public RoomId offset(int delta) {
        RoomId[] ids = values();
        return ids[Math.floorMod(ordinal() + delta, ids.length)];
    }
    @Override public String toString() { return displayName; }
}

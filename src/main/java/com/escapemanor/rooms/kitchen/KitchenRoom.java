package com.escapemanor.rooms.kitchen;

import com.escapemanor.core.GameContext;
import com.escapemanor.core.RoomId;
import com.escapemanor.core.RoomModule;
import com.escapemanor.ui.RoomCanvas;
import javafx.scene.Parent;

/** 담당 팀원이 실제 방/퍼즐 코드를 추가할 위치. 공통 파일 수정 없이 교체 가능. */
public final class KitchenRoom implements RoomModule {
    @Override public RoomId id() { return RoomId.KITCHEN; }
    @Override public Parent createView(GameContext context) {
        RoomCanvas room = new RoomCanvas(id().imagePath(), "주방");
        room.addHotspot(0.25, 0.20, 0.50, 0.60, "주방 조사 (임시)",
                () -> context.say("주방의 실제 퍼즐과 상호작용은 담당 팀원 코드 통합 후 연결됩니다."));
        return room;
    }
}

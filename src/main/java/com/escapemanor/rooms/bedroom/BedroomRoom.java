package com.escapemanor.rooms.bedroom;

import com.escapemanor.core.GameContext;
import com.escapemanor.core.RoomId;
import com.escapemanor.core.RoomModule;
import com.escapemanor.ui.RoomCanvas;
import javafx.scene.Parent;

/** 담당 팀원이 실제 방/퍼즐 코드를 추가할 위치. 공통 파일 수정 없이 교체 가능. */
public final class BedroomRoom implements RoomModule {
    @Override public RoomId id() { return RoomId.BEDROOM; }
    @Override public Parent createView(GameContext context) {
        RoomCanvas room = new RoomCanvas(id().imagePath(), "침실");
        room.addHotspot(0.25, 0.20, 0.50, 0.60, "침실 조사 (임시)",
                () -> context.say("침실의 실제 퍼즐과 상호작용은 담당 팀원 코드 통합 후 연결됩니다."));
        return room;
    }
}

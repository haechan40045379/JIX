package com.escapemanor.rooms.dining;

import com.escapemanor.core.GameContext;
import com.escapemanor.core.RoomId;
import com.escapemanor.core.RoomModule;
import com.escapemanor.ui.RoomCanvas;
import javafx.scene.Parent;

/** 식당: 배경 및 테스트용 조사 영역. 실제 퍼즐은 다음 개발 단계에서 이 클래스에 추가. */
public final class DiningRoom implements RoomModule {
    @Override public RoomId id() { return RoomId.DINING; }
    @Override public Parent createView(GameContext context) {
        RoomCanvas room = new RoomCanvas(id().imagePath(), "식당");
        room.addHotspot(0.30, 0.49, 0.42, 0.34, "식탁 조사",
                () -> context.addClue("dining.table.look", "식탁 위에는 무언가를 올려놓은 흔적이 있다."));
        room.addHotspot(0.78, 0.16, 0.20, 0.70, "문 조사",
                () -> context.say("문이 보인다. 식당과 현관의 실제 이동 조건은 퍼즐 설계 후 연결한다."));
        return room;
    }
}

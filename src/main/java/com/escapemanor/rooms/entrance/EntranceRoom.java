package com.escapemanor.rooms.entrance;

import com.escapemanor.core.GameContext;
import com.escapemanor.core.RoomId;
import com.escapemanor.core.RoomModule;
import com.escapemanor.ui.RoomCanvas;
import javafx.scene.Parent;

/** 현관: 최종 탈출문 클릭은 공통 GameManager에서 열쇠 3개를 확인한다. */
public final class EntranceRoom implements RoomModule {
    @Override public RoomId id() { return RoomId.ENTRANCE; }
    @Override public Parent createView(GameContext context) {
        RoomCanvas room = new RoomCanvas(id().imagePath(), "현관");
        room.addHotspot(0.30, 0.13, 0.40, 0.78, "현관 탈출문 열기",
                context::attemptEscape);
        return room;
    }
}

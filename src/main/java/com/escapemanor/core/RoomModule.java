package com.escapemanor.core;

import javafx.scene.Parent;

/** 팀원이 방별로 구현할 계약. createView는 그 방을 보여줄 때마다 호출된다. */
public interface RoomModule {
    RoomId id();
    Parent createView(GameContext context);
}

package com.escapemanor.core;

import com.escapemanor.rooms.bedroom.BedroomRoom;
import com.escapemanor.rooms.study.StudyRoom;
import com.escapemanor.rooms.kitchen.KitchenRoom;
import com.escapemanor.rooms.storage.StorageRoom;
import com.escapemanor.rooms.dining.DiningRoom;
import com.escapemanor.rooms.entrance.EntranceRoom;
import java.util.EnumMap;
import java.util.Map;
import javafx.scene.Parent;

/** 각 팀원은 자기 방 모듈만 교체하고 이 등록표를 최종 통합에서 수정. */
public final class RoomManager {
    private final Map<RoomId, RoomModule> rooms = new EnumMap<>(RoomId.class);
    public void register(RoomModule room) { rooms.put(room.id(), room); }
    public Parent createRoomView(RoomId id, GameContext context) {
        RoomModule room = rooms.get(id);
        if (room == null) throw new IllegalStateException("등록되지 않은 방: " + id);
        return room.createView(context);
    }
    public static RoomManager createDefault() {
        RoomManager manager = new RoomManager();
        manager.register(new BedroomRoom());
        manager.register(new StudyRoom());
        manager.register(new KitchenRoom());
        manager.register(new StorageRoom());
        manager.register(new DiningRoom());
        manager.register(new EntranceRoom());
        return manager;
    }
}

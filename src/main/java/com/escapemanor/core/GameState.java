package com.escapemanor.core;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/** 방을 이동해도 사라지면 안 되는 게임 상태를 한곳에서 보관한다. */
public final class GameState {
    private final ObjectProperty<RoomId> currentRoom = new SimpleObjectProperty<>(RoomId.BEDROOM);
    private final IntegerProperty remainingSeconds = new SimpleIntegerProperty(60 * 60);
    private final StringProperty narration = new SimpleStringProperty("눈을 뜨자 낯선 저택이었다. 이곳에서 탈출해야 한다.");
    private final ObservableList<ItemId> inventory = FXCollections.observableArrayList();
    private final ObservableList<String> journal = FXCollections.observableArrayList();
    private final ObservableMap<String, Boolean> puzzleFlags = FXCollections.observableHashMap();
    private boolean ended;

    public ObjectProperty<RoomId> currentRoomProperty() { return currentRoom; }
    public RoomId currentRoom() { return currentRoom.get(); }
    public void setCurrentRoom(RoomId room) { currentRoom.set(room); }
    public IntegerProperty remainingSecondsProperty() { return remainingSeconds; }
    public int remainingSeconds() { return remainingSeconds.get(); }
    public void setRemainingSeconds(int value) { remainingSeconds.set(Math.max(0, value)); }
    public StringProperty narrationProperty() { return narration; }
    public void setNarration(String message) { narration.set(message); }
    public ObservableList<ItemId> inventory() { return inventory; }
    public ObservableList<String> journal() { return journal; }
    public boolean flag(String id) { return puzzleFlags.getOrDefault(id, false); }
    public void setFlag(String id, boolean value) { puzzleFlags.put(id, value); }
    public boolean ended() { return ended; }
    public void setEnded(boolean ended) { this.ended = ended; }
}

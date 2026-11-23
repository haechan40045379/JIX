package com.escapemanor.core;

/** 각 방 클래스가 공통 시스템을 사용할 때 의존하는 좁은 인터페이스. */
public final class GameContext {
    private final GameManager game;
    public GameContext(GameManager game) { this.game = game; }
    public RoomId currentRoom() { return game.state().currentRoom(); }
    public void goTo(RoomId room) { game.goTo(room); }
    public void say(String message) { game.say(message); }
    public void addClue(String clueId, String message) { game.addClue(clueId, message); }
    public void grantItem(ItemId id) { game.grantItem(id); }
    public boolean hasItem(ItemId id) { return game.hasItem(id); }
    public void setFlag(String id) { game.setFlag(id); }
    public boolean flag(String id) { return game.flag(id); }
    public void attemptEscape() { game.attemptEscape(); }
}

package com.escapemanor.core;

import java.util.Objects;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/** 타이머·이동·아이템·탈출 조건의 유일한 관리자. */
public final class GameManager {
    private final GameState state = new GameState();
    private final Timeline timer;
    private Runnable onVictory = () -> {};
    private Runnable onTimeout = () -> {};

    public GameManager() {
        state.journal().add("[목표] 열쇠 세 개를 모아 현관으로 탈출하라.");
        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> tick()));
        timer.setCycleCount(Timeline.INDEFINITE);
    }
    public GameState state() { return state; }
    public void setOnVictory(Runnable onVictory) { this.onVictory = Objects.requireNonNull(onVictory); }
    public void setOnTimeout(Runnable onTimeout) { this.onTimeout = Objects.requireNonNull(onTimeout); }
    public void startTimer() { if (!state.ended()) timer.play(); }
    public void stopTimer() { timer.stop(); }
    private void tick() {
        if (state.ended()) return;
        state.setRemainingSeconds(state.remainingSeconds() - 1);
        if (state.remainingSeconds() == 0) {
            state.setEnded(true);
            timer.stop();
            say("제한 시간이 끝났다. 탈출에 실패했다.");
            onTimeout.run();
        }
    }
    public void say(String message) {
        if (message == null || message.isBlank()) return;
        state.setNarration(message);
        state.journal().add(message);
        if (state.journal().size() > 120) state.journal().remove(0);
    }
    public void addClue(String clueId, String message) {
        String key = "clue:" + clueId;
        if (!state.flag(key)) {
            state.setFlag(key, true);
            say("[단서 발견] " + message);
        } else say(message);
    }
    public void goTo(RoomId room) {
        if (state.ended()) return;
        state.setCurrentRoom(Objects.requireNonNull(room));
        say(room.displayName() + "(으)로 이동했다. 주변을 조사해 보자.");
    }
    public boolean hasItem(ItemId id) { return state.inventory().contains(id); }
    public void grantItem(ItemId id) {
        if (state.ended()) return;
        if (!hasItem(id)) {
            state.inventory().add(id);
            say("아이템 획득: " + id.title());
        } else say(id.title() + "은(는) 이미 가지고 있다.");
    }
    public void setFlag(String id) { state.setFlag(id, true); }
    public boolean flag(String id) { return state.flag(id); }
    public boolean hasAllKeys() {
        return hasItem(ItemId.BEDROOM_STUDY_KEY)
            && hasItem(ItemId.KITCHEN_STORAGE_KEY)
            && hasItem(ItemId.DINING_ENTRANCE_KEY);
    }
    public void attemptEscape() {
        if (state.ended()) return;
        if (state.currentRoom() != RoomId.ENTRANCE) {
            say("탈출문은 현관에 있다.");
            return;
        }
        if (!hasAllKeys()) {
            long count = java.util.Arrays.stream(ItemId.values()).filter(this::hasItem).count();
            say("현관문에는 열쇠 구멍이 세 개 있다. 열쇠가 " + count + "/3개 모였다.");
            return;
        }
        state.setEnded(true);
        timer.stop();
        say("세 개의 열쇠가 맞물리며 현관문이 열렸다. 탈출 성공!");
        onVictory.run();
    }
}

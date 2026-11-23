package com.escapemanor;

import com.escapemanor.core.GameManager;
import com.escapemanor.core.RoomManager;
import com.escapemanor.ui.GameView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.escapemanor.ui.menu.FxMainMenu;
import com.escapemanor.ui.menu.FxScenarioSelect;

/** 유일한 애플리케이션 진입점. 팀원이 별도의 Application을 만들지 않도록 한다. */
public final class MainApp extends Application {
    private Stage stage;
    private FxScenarioSelect.View scenarioView;
    private GameManager activeGame;

    @Override public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Mystery Story — Escape Manor (JavaFX)");
        stage.setMinWidth(860);
        stage.setMinHeight(560);
        showTitle();
        stage.show();
    }

    private void showTitle() {
        stopPreviousGame();
        Scene scene = FxMainMenu.createScene(
                this::showScenario,
                size -> { stage.setWidth(size.width()); stage.setHeight(size.height()); },
                Platform::exit);
        stage.setScene(scene);
    }

    private void showScenario() {
        stopPreviousGame();
        scenarioView = FxScenarioSelect.createView(this::showTitle, this::startNewGame);
        stage.setScene(scenarioView.scene());
    }

    private void startNewGame() {
        if (scenarioView != null) scenarioView.resetTransition();
        stopPreviousGame();
        GameManager game = new GameManager();
        GameView gameView = new GameView(game, RoomManager.createDefault(), this::showTitle,
                this::startNewGame);
        activeGame = game;
        stage.setScene(new Scene(gameView.getRoot(), 1200, 700));
        gameView.loadInitialRoom();
        game.startTimer();
    }

    private void stopPreviousGame() {
        if (activeGame != null) {
            activeGame.stopTimer();
            activeGame = null;
        }
    }

    @Override public void stop() { stopPreviousGame(); }
    public static void main(String[] args) { launch(args); }
}

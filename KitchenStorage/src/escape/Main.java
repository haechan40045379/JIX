package escape;

import java.io.IOException;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBase;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    /** 결과 화면의 "다시 하기 / 타이틀로" 버튼에서 쓰기 위한 현재 실행 인스턴스. */
    private static Main instance;

    private Stage stage;
    private Scene scene;
    private Region gameRoot;
    private MainController gameController;
    private MenuScreens menu;

    @Override
    public void start(Stage stage) {
        instance = this;
        this.stage = stage;
        scene = new Scene(new StackPane());
        scene.getStylesheets().add(Main.class.getResource("game.css").toExternalForm());
        scene.getStylesheets().add(Main.class.getResource("menu.css").toExternalForm());
        showMenu();

        // F11 또는 Alt+Enter: 전체화면 전환 (ESC 로도 빠져나올 수 있다)
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            boolean pauseKey = e.getCode() == KeyCode.ESCAPE && !stage.isFullScreen();
            if ((e.getCode() == KeyCode.M || e.getCode() == KeyCode.H || pauseKey) && gameController != null) {
                gameController.handleKey(e.getCode());
                e.consume();
            } else if (e.getCode() == KeyCode.F9 && gameRoot != null) {
                if (!gameRoot.getStyleClass().remove("show-hitboxes")) {
                    gameRoot.getStyleClass().add("show-hitboxes");
                }
                e.consume();
            } else if (e.getCode() == KeyCode.F11 || (e.getCode() == KeyCode.ENTER && e.isAltDown())) {
                toggleFullScreen(stage);
                e.consume();
            }
        });
        // 모든 버튼(사물 클릭 포함) 클릭음
        scene.addEventFilter(ActionEvent.ACTION, e -> {
            if (e.getTarget() instanceof ButtonBase) {
                SoundManager.sfx("sfx_click", 0.6);
            }
        });
        stage.setFullScreenExitHint("ESC 또는 F11: 창 모드로 돌아가기");
        stage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("ESC"));

        // 창을 닫으면 진행 상황 저장 → 다음에 "이어하기" 가능
        stage.setOnCloseRequest(e -> {
            if (gameController != null) {
                gameController.saveNow();
            }
        });

        stage.setTitle("MYSTERY STORY");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(640);
        stage.setMinHeight(440);
        MenuScreens.WindowSize.NORMAL.apply(stage);
        stage.show();
    }

    private void showMenu() {
        stopGame();
        SoundManager.stopSfx("sfx_tick");
        SoundManager.bgm("bgm_menu");
        menu = new MenuScreens(stage);
        menu.setOnStart((difficulty, episode) -> {
            GameState.get().reset(difficulty, episode);
            if (loadGameView()) {
                menu.stopFog();
            } else {
                menu.showLoadFailure();
            }
        });
        menu.setOnContinue(() -> {
            if (SaveManager.load(GameState.get()) && loadGameView()) {
                menu.stopFog();
            } else {
                menu.showLoadFailure();
            }
        });
        scene.setRoot(wrapScalable(menu.root()));
    }

    /** 같은 난이도로 새 게임 시작. */
    private boolean startGame() {
        GameState.get().reset(GameState.get().getDifficulty());
        return loadGameView();
    }

    /** 현재 GameState 그대로(새 게임 또는 불러온 세이브) 게임 화면을 연다. */
    private boolean loadGameView() {
        if (gameController != null) {
            gameController.stopTimer();
        }
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("MainView.fxml"));
            gameRoot = loader.load();
            gameController = loader.getController();
            scene.setRoot(wrapScalable(gameRoot));
            return true;
        } catch (IOException e) {
            System.getLogger(Main.class.getName()).log(System.Logger.Level.ERROR,
                    "게임 화면을 불러오지 못했습니다.", e);
            return false;
        }
    }

    private void stopGame() {
        if (gameController != null) {
            gameController.saveNow();
            gameController.stopTimer();
            gameController = null;
        }
        gameRoot = null;
    }

    /** 결과 화면 → 같은 에피소드 처음부터 다시. */
    public static void restartGame() {
        if (instance != null) {
            instance.startGame();
        }
    }

    /** 결과 화면 → 타이틀 메뉴. */
    public static void backToTitle() {
        if (instance != null) {
            instance.showMenu();
        }
    }

    public static void toggleFullScreen(Stage stage) {
        stage.setFullScreen(!stage.isFullScreen());
    }

    /**
     * 게임 화면(설계 크기 고정)을 검은 배경 틀 안에 넣고, 창 크기에 맞춰 비율을 유지한 채 확대/축소한다.
     * 남는 공간은 검은 여백(레터박스)이 된다. 클릭 좌표는 JavaFX가 자동으로 변환해 준다.
     */
    public static StackPane wrapScalable(Region game) {
        game.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        game.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        StackPane frame = new StackPane(game);
        frame.setStyle("-fx-background-color: black;");

        Runnable rescale = () -> {
            double baseW = game.prefWidth(-1);
            double baseH = game.prefHeight(-1);
            if (baseW <= 0 || baseH <= 0 || frame.getWidth() <= 0 || frame.getHeight() <= 0) {
                return;
            }
            double scale = Math.min(frame.getWidth() / baseW, frame.getHeight() / baseH);
            game.setScaleX(scale);
            game.setScaleY(scale);
        };
        frame.widthProperty().addListener((obs, o, n) -> rescale.run());
        frame.heightProperty().addListener((obs, o, n) -> rescale.run());
        return frame;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

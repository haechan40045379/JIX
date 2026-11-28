package escape;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    private Region gameRoot;

    @Override
    public void start(Stage stage) {
        MenuScreens menu = new MenuScreens(stage);
        StackPane frame = wrapScalable(menu.root());
        Scene scene = new Scene(frame);
        scene.getStylesheets().add(Main.class.getResource("game.css").toExternalForm());
        scene.getStylesheets().add(Main.class.getResource("menu.css").toExternalForm());
        menu.setOnStart(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("MainView.fxml"));
                gameRoot = loader.load();
                scene.setRoot(wrapScalable(gameRoot));
                menu.stopFog();
            } catch (IOException e) {
                System.getLogger(Main.class.getName()).log(System.Logger.Level.ERROR,
                        "게임 화면을 불러오지 못했습니다.", e);
                menu.showLoadFailure();
            }
        });

        // F11 또는 Alt+Enter: 전체화면 전환 (ESC 로도 빠져나올 수 있다)
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F9 && gameRoot != null) {
                if (!gameRoot.getStyleClass().remove("show-hitboxes")) {
                    gameRoot.getStyleClass().add("show-hitboxes");
                }
                e.consume();
            } else if (e.getCode() == KeyCode.F11 || (e.getCode() == KeyCode.ENTER && e.isAltDown())) {
                toggleFullScreen(stage);
                e.consume();
            }
        });
        stage.setFullScreenExitHint("ESC 또는 F11: 창 모드로 돌아가기");
        stage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("ESC"));

        stage.setTitle("MYSTERY STORY");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(640);
        stage.setMinHeight(440);
        MenuScreens.WindowSize.NORMAL.apply(stage);
        stage.show();
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

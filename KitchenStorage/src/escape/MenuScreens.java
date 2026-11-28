package escape;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

final class MenuScreens {
    private static final double WIDTH = 960;
    private static final double HEIGHT = 540;

    record WindowSize(int width, int height) {
        static final WindowSize NORMAL = new WindowSize(1200, 800);
        static final WindowSize SMALL = new WindowSize(980, 680);

        void apply(Stage stage) {
            stage.setWidth(width);
            stage.setHeight(height);
        }
    }

    private final Stage stage;
    private final StackPane root = new StackPane();
    private final Pane navigation = new Pane();
    private final TranslateTransition fog;
    private Runnable onStart;
    private IrisTransition iris;
    private StackPane dialog;

    MenuScreens(Stage stage) {
        this.stage = stage;
        root.setPrefSize(WIDTH, HEIGHT);
        root.getStyleClass().add("mystery-menu");
        ImageView background = image("main_menu_bk.png", WIDTH, HEIGHT);
        background.setId("menuBackground");
        root.getChildren().add(background);
        fog = addMovingFog();
        root.getChildren().add(navigation);
        showTitle();
    }

    StackPane root() {
        return root;
    }

    void setOnStart(Runnable onStart) {
        this.onStart = onStart;
    }

    void stopFog() {
        fog.stop();
    }

    private void showTitle() {
        VBox buttons = new VBox(12,
                button("게임 시작", this::showEpisodes),
                button("게임 설정", this::showSettings),
                button("도움말", this::showHelp),
                button("게임 종료", this::showExit));
        buttons.relocate(374, 246);
        navigation.getChildren().setAll(buttons);
        FadeTransition fade = new FadeTransition(Duration.millis(450), buttons);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
        navigation.requestFocus();
    }

    private void showEpisodes() {
        Label heading = label("에피소드 선택", "episode-heading");
        heading.setPrefSize(WIDTH, 28);
        heading.setAlignment(Pos.CENTER);
        heading.relocate(0, 225);

        VBox first = episodeContent("EPISODE 01");
        // 제목과 이미지 위치를 유지하도록 삭제한 상태 문구의 높이와 간격을 남긴다.
        first.setPadding(new Insets(0, 0, 22, 0));
        first.getChildren().addAll(label("THE INITIATE", "episode-name"),
                image("entrance_bg.png", 160, 90),
                label("비밀 조직의 시험", "episode-detail"));
        HBox cards = new HBox(20,
                card(first, "EPISODE 01 THE INITIATE", this::startEpisode),
                lockedCard("EPISODE 02"), lockedCard("EPISODE 03"));
        cards.relocate(172, 259);
        Button back = button("타이틀로 돌아가기", this::showTitle);
        back.relocate(374, 478);
        navigation.getChildren().setAll(heading, cards, back);
        navigation.requestFocus();
    }

    private void startEpisode() {
        if (iris != null) {
            return;
        }
        navigation.setDisable(true);
        iris = IrisTransition.play(root, () -> {
            try {
                onStart.run();
            } finally {
                iris.dispose();
                iris = null;
                navigation.setDisable(dialog != null);
            }
        });
    }

    private Button lockedCard(String number) {
        VBox content = episodeContent(number);
        content.getChildren().addAll(label("?", "episode-question"), label("미공개", "episode-status"));
        Button card = card(content, number + ", 미공개", () -> showDialog("미공개 에피소드",
                "아직 공개되지 않은 에피소드입니다.", button("확인", this::closeDialog)));
        card.getStyleClass().add("episode-locked");
        return card;
    }

    private static VBox episodeContent(String number) {
        VBox content = new VBox(4, label(number, "episode-number"));
        content.setAlignment(Pos.CENTER);
        content.setMouseTransparent(true);
        return content;
    }

    private static Button card(VBox content, String name, Runnable action) {
        Button card = new Button();
        card.setAccessibleText(name);
        card.setGraphic(content);
        card.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        card.getStyleClass().add("episode-card");
        card.setMinSize(192, 194);
        card.setPrefSize(192, 194);
        card.setMaxSize(192, 194);
        card.setOnAction(event -> action.run());

        ScaleTransition hover = new ScaleTransition(Duration.millis(170), card);
        hover.setInterpolator(Interpolator.EASE_BOTH);
        card.hoverProperty().addListener((obs, wasHovered, hovered) -> {
            hover.stop();
            hover.setToX(hovered ? 1.035 : 1);
            hover.setToY(hovered ? 1.035 : 1);
            hover.playFromStart();
        });
        return card;
    }

    private void showSettings() {
        showDialog("게임 설정", "창 크기 또는 화면 모드를 선택하세요.",
                button("기본 창 크기", () -> resize(WindowSize.NORMAL)),
                button("작은 창 크기", () -> resize(WindowSize.SMALL)),
                button("전체화면 전환", () -> {
                    closeDialog();
                    Main.toggleFullScreen(stage);
                }), button("닫기", this::closeDialog));
    }

    private void resize(WindowSize size) {
        closeDialog();
        stage.setFullScreen(false);
        stage.setMaximized(false);
        size.apply(stage);
    }

    private void showHelp() {
        showDialog("도움말", """
                게임 목표
                비밀 조직이 운영하는 저택을 탐색하며 퍼즐을 해결하고 열쇠 조각 세 개를 획득한 뒤 현관문을 통해 탈출하세요.

                조작 방법
                • 클릭으로 오브젝트 조사 및 퍼즐 조작
                • 출입문 또는 이동 화살표를 클릭해 방 이동
                • 인벤토리에서 사용 가능한 아이템을 클릭해 선택한 뒤 관련 오브젝트 클릭
                • 아이템을 더블클릭하거나 우클릭해 상세 조사
                • 단서 기록 버튼을 눌러 발견한 단서 확인
                • F11 또는 Alt+Enter로 전체화면 전환""", button("닫기", this::closeDialog));
    }

    private void showExit() {
        showDialog("종료 확인", "게임을 종료하시겠습니까?",
                button("확인", Platform::exit), button("취소", this::closeDialog));
    }

    void showLoadFailure() {
        showDialog("게임 시작 오류", "게임 화면을 불러오지 못했습니다.", button("닫기", this::closeDialog));
    }

    private void showDialog(String title, String body, Button... actions) {
        Label text = label(body, "menu-dialog-text");
        text.setWrapText(true);
        text.setMaxWidth(450);
        VBox panel = new VBox(14, label(title, "menu-dialog-title"), text);
        panel.getStyleClass().add("menu-dialog");
        panel.setAlignment(Pos.CENTER);
        panel.setMaxSize(520, VBox.USE_PREF_SIZE);
        for (Button action : actions) {
            panel.getChildren().add(action);
        }
        navigation.setDisable(true);
        dialog = new StackPane(panel);
        dialog.getStyleClass().add("menu-shade");
        root.getChildren().add(dialog);
        actions[actions.length - 1].requestFocus();
    }

    private void closeDialog() {
        root.getChildren().remove(dialog);
        dialog = null;
        navigation.setDisable(false);
        navigation.requestFocus();
    }

    private TranslateTransition addMovingFog() {
        Pane layer = new Pane();
        layer.setMouseTransparent(true);
        layer.setClip(new Rectangle(WIDTH, HEIGHT));
        Ellipse patch = new Ellipse(245, 62);
        patch.setLayoutY(403);
        patch.setOpacity(0.14);
        patch.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(192, 174, 153, 0.65)), new Stop(1, Color.TRANSPARENT)));
        layer.getChildren().add(patch);
        root.getChildren().add(layer);

        TranslateTransition move = new TranslateTransition(Duration.seconds(42), patch);
        move.setFromX(WIDTH + patch.getRadiusX());
        move.setToX(-patch.getRadiusX());
        move.setInterpolator(Interpolator.LINEAR);
        move.setCycleCount(Animation.INDEFINITE);
        move.play();
        return move;
    }

    private static ImageView image(String file, double width, double height) {
        ImageView view = new ImageView(Images.load(file));
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setPreserveRatio(true);
        view.setMouseTransparent(true);
        return view;
    }

    private static Button button(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("title-button");
        button.setMinSize(212, 42);
        button.setPrefSize(212, 42);
        button.setMaxSize(212, 42);
        button.setOnAction(event -> action.run());
        return button;
    }

    private static Label label(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }
}

package com.escapemanor.ui.menu;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/** 단독 JavaFX Scene에서 동작하는 기존 메인 메뉴 화면. */
public final class FxMainMenu {

    // 게임 제목 위치: LEFT가 커지면 오른쪽, Y가 작아지면 위쪽으로 이동.
    private static final double TITLE_LEFT = 90;
    private static final double TITLE_OFFSET_Y = -100;

    // 안개 강도: 0.0 (투명) ~ 1.0 (진함)
    private static final double FOG_OPACITY = 0.80;

    private FxMainMenu() {
        // 유틸리티 클래스: 인스턴스를 만들지 않음
    }

    public static Scene createScene(Runnable onStart,
                                    Consumer<WindowSize> onResize,
                                    Runnable onExit) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #180f12;");

        // 1. 메인 배경 이미지
        Image background = loadImage("/assets/Main_Menu_background.png");
        if (background == null) {
            System.out.println("새 배경을 찾지 못해 기존 배경을 사용합니다.");
            background = loadImage("/assets/main_background.png");
        }
        if (background != null) {
            ImageView imageView = new ImageView(background);
            imageView.setPreserveRatio(false);
            imageView.fitWidthProperty().bind(root.widthProperty());
            imageView.fitHeightProperty().bind(root.heightProperty());
            root.getChildren().add(imageView);
        } else {
            System.err.println("메인 배경 이미지가 없습니다. src/main/resources/assets 를 확인하세요.");
        }

        // 2. 안개: 배경 위에, 제목과 버튼 아래에 표시
        addMovingFog(root);

        // 3. 게임 제목: 기존 왼쪽 배치 유지
        VBox brand = new VBox(-12);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setMouseTransparent(true);

        Label mystery = new Label("Mystery");
        Label story = new Label("Story");
        mystery.getStyleClass().add("game-title");
        story.getStyleClass().add("game-title");

        // CSS 파일이 없어도 제목이 보이도록 기본 스타일 지정
        String titleStyle = "-fx-font-family: 'Georgia';"
                + "-fx-font-size: 120px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #ead8be;"
                + "-fx-effect: dropshadow(gaussian, black, 12, 0.7, 3, 4);";
        mystery.setStyle(titleStyle);
        story.setStyle(titleStyle);
        brand.getChildren().addAll(mystery, story);

        root.getChildren().add(brand);
        StackPane.setAlignment(brand, Pos.CENTER_LEFT);
        StackPane.setMargin(brand, new Insets(0, 0, 0, TITLE_LEFT));
        brand.setTranslateY(TITLE_OFFSET_Y);

        // 4. 오른쪽 버튼: 기존 위치/크기와 동작 유지
        VBox menu = new VBox(18);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(30, 28, 30, 28));
        menu.setPrefWidth(390);
        menu.setMinWidth(390);
        menu.setMaxWidth(390);
        menu.prefHeightProperty().bind(root.heightProperty());
        // 버튼 뒤 배경 패널은 투명, 버튼 자체 스타일은 CSS에서 유지
        menu.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");

        Button start = menuButton("게임 시작");
        start.setOnAction(e -> onStart.run());

        Button settings = menuButton("게임 설정");
        settings.setOnAction(e -> {
            String normal = "기본 크기 (1200 x 800)";
            String small = "작은 크기 (980 x 680)";
            ChoiceDialog<String> dialog = new ChoiceDialog<>(normal, List.of(normal, small));
            dialog.setTitle("게임 설정");
            dialog.setHeaderText("게임 창 크기 설정");
            dialog.setContentText("창 크기를 선택하세요.");
            Optional<String> answer = dialog.showAndWait();
            if (answer.isPresent()) {
                onResize.accept(normal.equals(answer.get())
                        ? new WindowSize(1200, 800) : new WindowSize(980, 680));
            }
        });

        Button help = menuButton("도움말");
        help.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("도움말");
            alert.setHeaderText("Mystery Story 게임 안내");
            alert.setContentText("1. 게임 시작을 눌러 시나리오를 선택하세요.\n"
                    + "2. 단서를 모으며 방을 탐색하세요.\n"
                    + "3. 세 개의 열쇠를 모아 현관에서 탈출하세요.\n\n"
                    + "※ 일부 방은 통합 전 임시 화면입니다.");
            alert.showAndWait();
        });

        Button exit = menuButton("종료");
        exit.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "게임을 종료하시겠습니까?", ButtonType.YES, ButtonType.NO);
            alert.setTitle("종료 확인");
            alert.setHeaderText(null);
            if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                onExit.run();
            }
        });

        menu.getChildren().addAll(start, settings, help, exit);
        root.getChildren().add(menu);
        StackPane.setAlignment(menu, Pos.CENTER_RIGHT);

        menu.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(450), menu);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        // 5. CSS 연결: 기존 리소스 경로 유지
        Scene scene = new Scene(root, 1200, 800);
        java.net.URL css = FxMainMenu.class.getResource("/css/main-menu.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        } else {
            System.err.println("/css/main-menu.css 파일을 찾지 못했습니다.");
        }
        return scene;
    }

    /** 서로 떨어진 안개 덩어리가 오른쪽에서 왼쪽으로 이동한다. */
    private static void addMovingFog(StackPane root) {
        Pane fogLayer = new Pane();
        fogLayer.setMouseTransparent(true);
        fogLayer.setMinSize(0, 0);
        fogLayer.setPrefSize(0, 0);
        fogLayer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // 창 바깥의 안개는 그리지 않도록 클리핑
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(fogLayer.widthProperty());
        clip.heightProperty().bind(fogLayer.heightProperty());
        fogLayer.setClip(clip);

        // 높이(비율), 가로/세로 반지름(px), 등장 지연(초), 이동 시간(초)
        addFogPatch(fogLayer, 0.75, 300, 95, 0, 36);
        addFogPatch(fogLayer, 0.60, 230, 75, 12, 42);
        addFogPatch(fogLayer, 0.86, 320, 105, 24, 40);
        addFogPatch(fogLayer, 0.70, 210, 70, 36, 45);

        root.getChildren().add(fogLayer);
    }

    private static void addFogPatch(Pane layer,
                                    double heightRatio,
                                    double radiusX,
                                    double radiusY,
                                    double delaySeconds,
                                    double durationSeconds) {
        Ellipse fog = new Ellipse(radiusX, radiusY);
        fog.setManaged(false);
        fog.setMouseTransparent(true);

        // 창 높이가 달라져도 안개 높이는 일정한 비율로 유지
        fog.layoutYProperty().bind(layer.heightProperty().multiply(heightRatio));

        // 중심은 연하고 가장자리로 갈수록 사라지는 모양
        RadialGradient gradient = new RadialGradient(
                0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(215, 222, 228, 0.65)),
                new Stop(0.55, Color.rgb(215, 222, 228, 0.32)),
                new Stop(1.0, Color.TRANSPARENT));
        fog.setFill(gradient);
        fog.setOpacity(FOG_OPACITY);
        layer.getChildren().add(fog);

        TranslateTransition move = new TranslateTransition(
                Duration.seconds(durationSeconds), fog);
        move.setFromX(1250);
        move.setToX(-radiusX - 10);
        move.setDelay(Duration.seconds(delaySeconds));
        move.setInterpolator(Interpolator.LINEAR);
        move.setCycleCount(Animation.INDEFINITE);

        // 화면에 머무는 동안 애니메이션 참조를 유지
        fog.getProperties().put("fogAnimation", move);
        move.play();
    }


private static Button menuButton(String text) {
    Button button = new Button(text);
    button.getStyleClass().add("game-menu-button");

    applyMenuButtonStyle(button, 22);

    button.setPrefSize(330, 63);
    button.setMinSize(330, 63);
    button.setMaxSize(330, 63);

    return button;
}

public static void applyMenuButtonStyle(Button button, int fontSize) {

    String baseStyle =
            "-fx-font-family: 'Malgun Gothic';"
            + "-fx-font-size: " + fontSize + "px;"
            + "-fx-font-weight: bold;"
            + "-fx-text-fill: #f5e5c9;"
            + "-fx-border-color: #ccad7e;"
            + "-fx-border-width: 2px;"
            + "-fx-border-radius: 3px;"
            + "-fx-background-radius: 3px;"
            + "-fx-cursor: hand;";

    String normalStyle = baseStyle
            + "-fx-background-color: #2c1718;";

    String hoverStyle = baseStyle
            + "-fx-background-color: #6e272a;";

    String pressedStyle = baseStyle
            + "-fx-background-color: #8b383b;";

    button.setStyle(normalStyle);

    button.setOnMouseEntered(e ->
            button.setStyle(hoverStyle));

    button.setOnMouseExited(e ->
            button.setStyle(normalStyle));

    button.setOnMousePressed(e ->
            button.setStyle(pressedStyle));

    button.setOnMouseReleased(e ->
            button.setStyle(
                button.isHover() ? hoverStyle : normalStyle
            ));
}


    private static Image loadImage(String path) {
        try (InputStream stream = FxMainMenu.class.getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }
            Image image = new Image(stream);
            return image.isError() ? null : image;
        } catch (Exception ex) {
            System.err.println("이미지 로딩 실패 (" + path + "): " + ex.getMessage());
            return null;
        }
    }
}

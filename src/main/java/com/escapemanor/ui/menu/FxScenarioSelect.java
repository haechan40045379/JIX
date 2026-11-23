package com.escapemanor.ui.menu;

import java.io.InputStream;
import java.net.URL;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/** 메인화면과 별도로 관리하는 시나리오 선택 JavaFX UI. */
public final class FxScenarioSelect {
    private FxScenarioSelect() { }

    public static final class View {
        private final Scene scene;
        private final StackPane root;
        private IrisTransition transition;
        private boolean transitioning;

        private View(Scene scene, StackPane root) {
            this.scene = scene;
            this.root = root;
        }

        public Scene scene() { return scene; }

        private void startPoisonSoup(Runnable onStartPoisonSoup) {
            if (transitioning) return;
            transitioning = true;
            transition = IrisTransition.play(root, onStartPoisonSoup);
        }

        /** 게임 화면으로 전환이 끝난 시점에서 호출한다. */
        public void resetTransition() {
            if (transition != null) {
                transition.dispose();
                transition = null;
            }
            transitioning = false;
        }
    }

    public static View createView(Runnable onBackToTitle, Runnable onStartPoisonSoup) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #180f12;");

        // 기존 메인화면과 완전히 동일한 원본 이미지 사용
        Image background = loadImage("/assets/Main_Menu_background.png");
        if (background != null) {
            ImageView backgroundView = new ImageView(background);
            backgroundView.setPreserveRatio(false);
            backgroundView.fitWidthProperty().bind(root.widthProperty());
            backgroundView.fitHeightProperty().bind(root.heightProperty());
            root.getChildren().add(backgroundView);
        }

        // 기존 복도 배경이 잘 보이도록 아주 옅은 어두운 레이어만 올린다.
        Rectangle veil = new Rectangle();
        veil.widthProperty().bind(root.widthProperty());
        veil.heightProperty().bind(root.heightProperty());
        veil.setFill(Color.rgb(10, 5, 8, 0.24));
        veil.setMouseTransparent(true);
        root.getChildren().add(veil);

        BorderPane layout = new BorderPane();
        layout.setMinSize(0, 0);
        layout.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Label heading = new Label("시나리오 선택");
        heading.getStyleClass().add("scenario-heading");
        HBox headingLine = ornamentLine();
        VBox header = new VBox(11, heading, headingLine);
        header.setAlignment(Pos.CENTER);
        BorderPane.setMargin(header, new Insets(54, 20, 0, 20));
        layout.setTop(header);

        HBox cards = new HBox(28);
        cards.setAlignment(Pos.CENTER);
        Button poison = createPoisonCard(root);
        Button second = createUnknownCard(root);
        Button third = createUnknownCard(root);
        cards.getChildren().addAll(poison, second, third);
        BorderPane.setMargin(cards, new Insets(14, 28, 4, 28));
        layout.setCenter(cards);


        Button back = new Button("타이틀로 돌아가기");

        back.getStyleClass().addAll(
        		"game-menu-button",
        		"scenario-back-button"
        		);

        FxMainMenu.applyMenuButtonStyle(back, 20);

        back.setPrefSize(330, 60);
        back.setMinSize(330, 60);

        back.setOnAction(e -> onBackToTitle.run());

        VBox footer = new VBox(back);
        footer.setAlignment(Pos.CENTER);
        BorderPane.setMargin(footer, new Insets(0, 15, 58, 15));
        layout.setBottom(footer);
        root.getChildren().add(layout);

        Scene scene = new Scene(root, 1200, 800);
        addCss(scene, "/css/main-menu.css");
        addCss(scene, "/css/scenario-select.css");
        View view = new View(scene, root);
        poison.setOnAction(e -> view.startPoisonSoup(onStartPoisonSoup));

        Runnable notYetAvailable = () -> {
            if (view.transitioning) return;
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("미공개 시나리오");
            alert.setHeaderText("아직 공개되지 않은 시나리오입니다.");
            alert.setContentText("현재는 시나리오 1 · Escape Manor를 플레이할 수 있습니다.");
            alert.showAndWait();
        };
        second.setOnAction(e -> notYetAvailable.run());
        third.setOnAction(e -> notYetAvailable.run());
        return view;
    }

    private static Button createPoisonCard(StackPane root) {
        Label number = new Label("시나리오 1");
        number.getStyleClass().add("scenario-number");
        Label name = new Label("Escape Manor");
        name.getStyleClass().add("scenario-name");
        Label motif = new Label("✦");
        motif.getStyleClass().add("scenario-motif");
        Label time = new Label("제한시간 : 1시간");
        Label difficulty = new Label("난이도 : 하");
        time.getStyleClass().add("scenario-detail");
        difficulty.getStyleClass().add("scenario-detail");

        VBox text = new VBox(7);
        text.setAlignment(Pos.CENTER);
        text.setPadding(new Insets(18, 12, 18, 12));

        StackPane imageArea = new StackPane();
        VBox.setVgrow(imageArea, Priority.ALWAYS);

        Image soupImage = loadImage("/assets/select_Poison_Soup.png");

        if (soupImage != null) {
        	ImageView soupView = new ImageView(soupImage);

        	soupView.setFitWidth(225);
        	soupView.setFitHeight(175);
        	soupView.setPreserveRatio(true);
        	soupView.setMouseTransparent(true);

        	imageArea.getChildren().add(soupView);
        }


        text.getChildren().addAll(
        		number,
        		thinLine(),
        		name,
        		imageArea,
        		motif,
        		spaceFor(11),
        		thinLine(),
        		time,
        		difficulty
        		);

        return cardButton(root, text, false);
    }

    private static Button createUnknownCard(StackPane root) {
        Label question = new Label("?");
        question.getStyleClass().add("scenario-question");
        Label caption = new Label("미공개");
        caption.getStyleClass().add("scenario-locked-caption");
        VBox inner = new VBox(16, question, thinLine(), caption);
        inner.setAlignment(Pos.CENTER);
        return cardButton(root, inner, true);
    }

    private static Button cardButton(StackPane root, VBox content, boolean locked) {
        Button card = new Button();
        card.getStyleClass().add("scenario-card");
        if (locked) card.getStyleClass().add("scenario-card-locked");
        card.setGraphic(content);
        card.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        card.setCursor(Cursor.HAND);
        card.setFocusTraversable(true);
        card.prefWidthProperty().bind(root.widthProperty().multiply(0.24));
        card.prefHeightProperty().bind(root.heightProperty().multiply(0.53));
        card.setMinSize(220, 345);
        card.setMaxSize(310, 470);
        content.prefWidthProperty().bind(card.widthProperty().subtract(42));
        content.prefHeightProperty().bind(card.heightProperty().subtract(42));

        // 버튼 위에 마우스를 올렸을 때 크기를 부드럽게 변경
        card.setOnMouseEntered(e -> hover(card, 1.035));
        card.setOnMouseExited(e -> hover(card, 1.0));
        return card;
    }

    private static void hover(Button card, double scale) {
        ScaleTransition previous = (ScaleTransition) card.getProperties().get("hoverAnimation");
        if (previous != null) previous.stop();
        ScaleTransition animation = new ScaleTransition(Duration.millis(170), card);
        animation.setFromX(card.getScaleX());
        animation.setFromY(card.getScaleY());
        animation.setToX(scale);
        animation.setToY(scale);
        animation.setInterpolator(Interpolator.EASE_BOTH);
        card.getProperties().put("hoverAnimation", animation);
        animation.play();
    }

    private static HBox ornamentLine() {
        Region left = new Region();
        Region right = new Region();
        left.getStyleClass().add("scenario-heading-rule");
        right.getStyleClass().add("scenario-heading-rule");
        left.setPrefWidth(125);
        right.setPrefWidth(125);
        Label center = new Label("◆");
        center.getStyleClass().add("scenario-heading-ornament");
        HBox line = new HBox(12, left, center, right);
        line.setAlignment(Pos.CENTER);
        return line;
    }

    private static Region thinLine() {
        Region rule = new Region();
        rule.getStyleClass().add("scenario-card-rule");
        rule.setPrefWidth(165);
        rule.setMaxWidth(165);
        return rule;
    }

    private static Region spaceFor(double height) {
        Region spacer = new Region();
        spacer.setMinHeight(height);
        spacer.setMaxHeight(height);
        return spacer;
    }

    private static Image loadImage(String path) {
        try (InputStream in = FxScenarioSelect.class.getResourceAsStream(path)) {
            if (in == null) {
                System.err.println("시나리오 선택 배경 이미지를 찾지 못했습니다: " + path);
                return null;
            }
            Image image = new Image(in);
            return image.isError() ? null : image;
        } catch (Exception ex) {
            System.err.println("이미지를 불러오는 중 오류가 발생했습니다: " + ex.getMessage());
            return null;
        }
    }

    private static void addCss(Scene scene, String path) {
        URL css = FxScenarioSelect.class.getResource(path);
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        else System.err.println("스타일 파일을 찾지 못했습니다: " + path);
    }
}

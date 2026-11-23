package com.escapemanor.ui.menu;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

/**
 * 화면 중앙의 타원형 구멍이 닫히면서 화면 전체를 어둡게 덮는 아이리스 아웃.
 * JavaFX Application Thread에서 호출한다.
 */
public final class IrisTransition {
    private final Pane parent;
    private final Path darkMask = new Path();
    private final DoubleProperty opening = new SimpleDoubleProperty(1.0);
    private final InvalidationListener refresh = ignored -> drawMask();
    private Timeline timeline;

    private IrisTransition(Pane parent) {
        this.parent = parent;
        darkMask.setFill(Color.rgb(4, 2, 3));
        darkMask.setFillRule(FillRule.EVEN_ODD); // 내부 타원은 투명한 구멍
        darkMask.setManaged(false);
        darkMask.setMouseTransparent(false);
        darkMask.setPickOnBounds(true); // 애니메이션 동안 다른 버튼 클릭 차단
        darkMask.setOnMousePressed(event -> event.consume());
        darkMask.setOnMouseClicked(event -> event.consume());

        opening.addListener(refresh);
        parent.widthProperty().addListener(refresh);
        parent.heightProperty().addListener(refresh);
        parent.getChildren().add(darkMask);
        drawMask();
    }

    public static IrisTransition play(Pane parent, Runnable onComplete) {
        IrisTransition transition = new IrisTransition(parent);
        transition.timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(transition.opening, 1.0)),
                new KeyFrame(Duration.millis(820),
                        new KeyValue(transition.opening, 0.0, Interpolator.EASE_IN)));
        transition.timeline.setOnFinished(e -> onComplete.run());
        transition.timeline.play();
        return transition;
    }

    private void drawMask() {
        double width = Math.max(1, parent.getWidth());
        double height = Math.max(1, parent.getHeight());
        double centerX = width * 0.5;
        double centerY = height * 0.5;
        // 처음에는 화면 네 귀퉁이까지 모두 타원 안에 들어가게 크게 시작한다.
        double rx = Math.max(0.01, width * opening.get());
        double ry = Math.max(0.01, height * opening.get());
        darkMask.getElements().setAll(
                new MoveTo(0, 0),
                new LineTo(width, 0),
                new LineTo(width, height),
                new LineTo(0, height),
                new ClosePath(),
                new MoveTo(centerX + rx, centerY),
                new ArcTo(rx, ry, 0, centerX - rx, centerY, false, true),
                new ArcTo(rx, ry, 0, centerX + rx, centerY, false, true),
                new ClosePath());
    }

    /** 게임으로 전환한 후 마스크 제거. 다음 플레이 시 재사용 가능. */
    public void dispose() {
        if (timeline != null) {
            timeline.stop();
        }
        opening.removeListener(refresh);
        parent.widthProperty().removeListener(refresh);
        parent.heightProperty().removeListener(refresh);
        parent.getChildren().remove(darkMask);
    }
}

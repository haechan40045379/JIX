package escape;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

/** [G] 기관실 시계. 시침·분침을 돌려 등불 점등 시각(11:40)에 맞춘 뒤 태엽을 잠근다. */
public class Ep2ClockController extends Ep2Puzzle {

    private static final double SIZE = 380;
    private static final double CX = SIZE * 0.5;
    private static final double CY = SIZE * 0.479;

    int hour = 3;
    int minute = 0;
    private Line hourHand;
    private Line minuteHand;
    private Label readout;
    private Label result;

    @Override
    protected void build() {
        Pane board = new Pane();
        board.setPrefSize(SIZE, SIZE);
        board.setMinSize(SIZE, SIZE);
        board.setMaxSize(SIZE, SIZE);
        hourHand = hand(7);
        minuteHand = hand(4.5);
        hourHand.setOnMouseClicked(e -> turnHour(1));
        minuteHand.setOnMouseClicked(e -> turnMinute(5));
        hourHand.setCursor(javafx.scene.Cursor.HAND);
        minuteHand.setCursor(javafx.scene.Cursor.HAND);
        Circle pin = new Circle(CX, CY, 8, Color.web("#7a5424"));
        pin.setMouseTransparent(true);
        board.getChildren().addAll(picture("ep2_clock_closeup.png", SIZE, SIZE), hourHand, minuteHand, pin);

        readout = text("");
        readout.setVisible(state.getDifficulty() == GameState.Difficulty.EASY);
        result = text("");
        result.setMinHeight(20);
        HBox hourRow = new HBox(8, button("◀", () -> turnHour(-1)), text("시침"), button("▶", () -> turnHour(1)));
        HBox minuteRow = new HBox(8, button("◀", () -> turnMinute(-5)), text("분침"), button("▶", () -> turnMinute(5)));
        hourRow.setAlignment(Pos.CENTER);
        minuteRow.setAlignment(Pos.CENTER);
        HBox controls = new HBox(30, hourRow, minuteRow, button("태엽 잠그기", this::lock));
        controls.setAlignment(Pos.CENTER);
        root.getChildren().addAll(board, controls, readout, result);
        draw();
    }

    private Line hand(double width) {
        Line l = new Line();
        l.setStroke(Color.web("#2b2118"));
        l.setStrokeWidth(width);
        l.setStrokeLineCap(StrokeLineCap.ROUND);
        return l;
    }

    void turnHour(int d) {
        hour = ((hour - 1 + d) % 12 + 12) % 12 + 1;
        SoundManager.sfx(SoundManager.first("sfx_clockwork", "sfx_click"), 0.3);
        draw();
    }

    void turnMinute(int d) {
        minute = ((minute + d) % 60 + 60) % 60;
        SoundManager.sfx(SoundManager.first("sfx_clockwork", "sfx_click"), 0.3);
        draw();
    }

    private void draw() {
        setHand(hourHand, (hour % 12 + minute / 60.0) * 30, SIZE * 0.2);
        setHand(minuteHand, minute * 6, SIZE * 0.31);
        readout.setText(String.format("현재 %d시 %02d분", hour, minute));
        result.setText("");
    }

    private static void setHand(Line l, double angleDeg, double length) {
        double rad = Math.toRadians(angleDeg);
        l.setStartX(CX);
        l.setStartY(CY);
        l.setEndX(CX + Math.sin(rad) * length);
        l.setEndY(CY - Math.cos(rad) * length);
    }

    void lock() {
        if (hour == Episode2.CLOCK_HOUR && minute == Episode2.CLOCK_MINUTE) {
            SoundManager.sfx(SoundManager.first("sfx_clock_chime", "sfx_correct"));
            solve();
        } else {
            result.setText("태엽이 헛돌며 바늘이 부르르 떨린다. 이 시각이 아니다.");
            state.penalize(GameState.PENALTY_SMALL, "시계 바늘 오답");
        }
    }
}

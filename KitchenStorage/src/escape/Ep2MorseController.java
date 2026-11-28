package escape;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * [A] 장대 위 신호등: 철문 번호(0113)를 숫자 모스 부호로 깜빡인다.
 * 쉬움은 느리게, 어려움은 빠르게 재생하고 받아 적기 표시가 없다.
 */
public class Ep2MorseController extends Ep2Puzzle {

    static final String[] MORSE = {
        "-----", ".----", "..---", "...--", "....-", ".....", "-....", "--...", "---..", "----.",
    };

    private static final double SIZE = 360;
    private Circle light;
    private Label transcript;
    private Button play;
    private Timeline timeline;

    @Override
    protected void build() {
        Pane board = new Pane();
        board.setPrefSize(SIZE, SIZE);
        board.setMinSize(SIZE, SIZE);
        board.setMaxSize(SIZE, SIZE);
        light = new Circle(SIZE * 0.5, SIZE * 0.655, SIZE * 0.19);
        light.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 250, 210, 0.95)), new Stop(0.55, Color.rgb(255, 214, 110, 0.75)),
                new Stop(1, Color.rgb(255, 190, 60, 0))));
        light.setEffect(new GaussianBlur(6));
        light.setOpacity(0);
        light.setMouseTransparent(true);
        board.getChildren().addAll(picture("ep2_signal_lamp_closeup.png", SIZE, SIZE), light);

        transcript = new Label(" ");
        transcript.getStyleClass().addAll("puzzle-text", "morse-transcript");
        transcript.setVisible(state.getDifficulty() != GameState.Difficulty.HARD);
        play = button("▶ 신호 보기", this::playSignal);
        HBox buttons = new HBox(10, play);
        buttons.setAlignment(javafx.geometry.Pos.CENTER);
        root.getChildren().addAll(board,
                text("짧은 빛과 긴 빛이 숫자 네 개를 보낸다. 오두막의 선원 수첩으로 읽을 수 있다."),
                transcript, buttons);
    }

    private double unit() {
        switch (state.getDifficulty()) {
            case EASY: return 0.33;
            case HARD: return 0.2;
            default: return 0.25;
        }
    }

    private void playSignal() {
        stop();
        play.setDisable(true);
        transcript.setText(" ");
        timeline = new Timeline();
        double u = unit();
        double t = 0.6;
        String code = Episode2.DIAL_CODE;
        StringBuilder written = new StringBuilder();
        for (int d = 0; d < code.length(); d++) {
            String symbols = MORSE[code.charAt(d) - '0'];
            for (int i = 0; i < symbols.length(); i++) {
                boolean dash = symbols.charAt(i) == '-';
                written.append(dash ? '−' : '·');
                String shown = written.toString();
                timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(t), e -> {
                    light.setOpacity(1);
                    transcript.setText(shown);
                    SoundManager.sfx("sfx_beep_1", 0.25);
                }));
                t += dash ? 3 * u : u;
                timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(t), e -> light.setOpacity(0)));
                t += u;
            }
            if (d < code.length() - 1) {
                written.append("   ");
            }
            t += 2.5 * u;
        }
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(t), e -> {
            play.setDisable(false);
            play.setText("↻ 다시 보기");
        }));
        timeline.play();
    }

    @Override
    protected void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        if (light != null) {
            light.setOpacity(0);
        }
    }
}

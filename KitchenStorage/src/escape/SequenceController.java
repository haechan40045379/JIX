package escape;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * 퍼즐 2: 숫자/색깔 순서 기억하기.
 * 1 빨강 / 2 파랑 / 3 노랑 / 4 초록 버튼이 순서대로 빛나고, 재생이 끝나면 같은 순서로 누른다.
 * 패널 그림(sequence_closeup.png)의 버튼 위치에 투명한 원을 겹쳐 클릭/발광을 처리한다.
 */
public class SequenceController implements PuzzleController {

    private static final int SEQUENCE_LENGTH = 4;
    private static final String[] NAMES = {"빨강", "파랑", "노랑", "초록"};
    private static final String[] DIM_COLORS = {"#6e2a24", "#243f63", "#6e5d1f", "#2a5a34"};
    private static final String[] LIT_COLORS = {"#ff5a4a", "#4aa0ff", "#ffe04a", "#5adf78"};

    // 420x420 패널 기준 좌표 (원본 1254x1254 그림의 버튼/번호판/표시등 위치를 축소한 값)
    private static final double[][] PAD_CENTER = {{153, 137}, {266, 137}, {153, 256}, {266, 256}};
    private static final double PAD_RADIUS = 30;
    private static final double[][] PLATE_CENTER = {{153, 191}, {266, 191}, {153, 308}, {266, 308}};
    private static final double[] LAMP_CENTER = {208, 79};

    @FXML private Label infoLabel;
    @FXML private Label progressLabel;
    @FXML private Button playButton;
    @FXML private Pane panel;
    @FXML private ImageView panelImage;

    private final Circle[] pads = new Circle[4];
    private Circle lamp;
    private boolean hasImage;
    private final List<Integer> answer = new ArrayList<>();
    private final Random random = new Random();
    private Runnable onSolved = () -> { };
    private SequentialTransition playback;
    private boolean accepting;
    private boolean solved;
    private int inputIndex;

    @FXML
    private void initialize() {
        hasImage = Images.apply(panelImage, "sequence_closeup.png");

        for (int i = 0; i < pads.length; i++) {
            final int idx = i;
            Circle c = new Circle(PAD_CENTER[i][0], PAD_CENTER[i][1], PAD_RADIUS);
            c.setStroke(hasImage ? Color.TRANSPARENT : Color.web("#141210"));
            c.setStrokeWidth(3);
            c.setStyle("-fx-cursor: hand;");
            c.setOnMouseClicked(e -> press(idx));
            pads[i] = c;

            Label num = new Label(String.valueOf(i + 1));
            num.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2a2118;");
            num.setMouseTransparent(true);
            num.setMinWidth(30);
            num.setAlignment(javafx.geometry.Pos.CENTER);
            num.setLayoutX(PLATE_CENTER[i][0] - 15);
            num.setLayoutY(PLATE_CENTER[i][1] - 13);
            if (!hasImage) {
                num.setStyle(num.getStyle() + "-fx-background-color: #d9caa5;");
            }
            panel.getChildren().addAll(c, num);
            light(i, false);
        }

        lamp = new Circle(LAMP_CENTER[0], LAMP_CENTER[1], 11, Color.TRANSPARENT);
        lamp.setMouseTransparent(true);
        panel.getChildren().add(lamp);

        for (int i = 0; i < SEQUENCE_LENGTH; i++) {
            answer.add(random.nextInt(pads.length));
        }
        setPadsDisabled(true);
        progressLabel.setText("");
    }

    @Override
    public void setOnSolved(Runnable onSolved) {
        this.onSolved = onSolved;
    }

    @Override
    public void onClose() {
        if (playback != null) {
            playback.stop();
        }
    }

    /** 정답 순서를 하이라이트로 재생한다. 재생 중에는 입력이 잠긴다. */
    @FXML
    private void onPlay() {
        if (solved) {
            return;
        }
        accepting = false;
        inputIndex = 0;
        setPadsDisabled(true);
        playButton.setDisable(true);
        infoLabel.setText("잘 기억해라...");
        progressLabel.setText("");
        setLamp("#ffb84a");
        for (int i = 0; i < pads.length; i++) {
            light(i, false);
        }

        playback = new SequentialTransition();
        playback.getChildren().add(new PauseTransition(Duration.seconds(0.6)));
        for (int idx : answer) {
            playback.getChildren().add(step(0.05, () -> light(idx, true)));
            playback.getChildren().add(step(0.55, () -> light(idx, false)));
            playback.getChildren().add(step(0.25, null));
        }
        playback.setOnFinished(e -> {
            accepting = true;
            setPadsDisabled(false);
            playButton.setDisable(false);
            setLamp(null);
            infoLabel.setText("같은 순서로 눌러라. (" + SEQUENCE_LENGTH + "개)");
            updateProgress();
        });
        playback.play();
    }

    /** 버튼(원) 클릭 처리. 테스트에서도 직접 호출할 수 있게 분리. */
    void press(int idx) {
        if (!accepting || solved) {
            return;
        }
        flash(idx);

        if (answer.get(inputIndex) != idx) {
            accepting = false;
            setPadsDisabled(true);
            setLamp("#ff3a2a");
            infoLabel.setText("틀렸다! (" + (idx + 1) + " " + NAMES[idx] + ") 재생 버튼으로 다시 도전하자.");
            progressLabel.setText("");
            return;
        }

        inputIndex++;
        updateProgress();
        if (inputIndex == answer.size()) {
            solved = true;
            accepting = false;
            setPadsDisabled(true);
            playButton.setDisable(true);
            setLamp("#5adf78");
            infoLabel.setText("정답! 패널의 불빛이 모두 켜진다.");
            for (int i = 0; i < pads.length; i++) {
                light(i, true);
            }
            PauseTransition wait = new PauseTransition(Duration.seconds(1.0));
            wait.setOnFinished(e -> onSolved.run());
            wait.play();
        }
    }

    private void updateProgress() {
        StringBuilder sb = new StringBuilder("입력: ");
        for (int i = 0; i < SEQUENCE_LENGTH; i++) {
            sb.append(i < inputIndex ? "● " : "○ ");
        }
        progressLabel.setText(sb.toString().trim());
    }

    private PauseTransition step(double seconds, Runnable action) {
        PauseTransition p = new PauseTransition(Duration.seconds(seconds));
        if (action != null) {
            p.setOnFinished(e -> action.run());
        }
        return p;
    }

    private void flash(int idx) {
        light(idx, true);
        PauseTransition off = new PauseTransition(Duration.seconds(0.2));
        off.setOnFinished(e -> {
            if (!solved) {
                light(idx, false);
            }
        });
        off.play();
    }

    private void light(int idx, boolean on) {
        Circle c = pads[idx];
        if (on) {
            Color lit = Color.web(LIT_COLORS[idx]);
            c.setFill(lit.deriveColor(0, 1, 1, 0.85));
            c.setEffect(new DropShadow(28, lit));
        } else {
            // 그림이 있으면 그림 속 버튼이 보이도록 투명, 없으면 어두운 색
            c.setFill(hasImage ? Color.rgb(0, 0, 0, 0.01) : Color.web(DIM_COLORS[idx]));
            c.setEffect(null);
        }
    }

    private void setLamp(String color) {
        if (color == null) {
            lamp.setFill(Color.TRANSPARENT);
            lamp.setEffect(null);
        } else {
            Color c = Color.web(color);
            lamp.setFill(c);
            lamp.setEffect(new DropShadow(16, c));
        }
    }

    private void setPadsDisabled(boolean disabled) {
        for (Circle pad : pads) {
            pad.setMouseTransparent(disabled);
        }
    }
}

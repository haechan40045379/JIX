package escape;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 퍼즐 1: 스톱워치 시간 맞추기. 목표 시간 ± 오차 안에 정지하면 성공.
 * 다이얼은 60초에 한 바퀴. 위쪽 빨간 버튼 또는 아래 버튼으로 조작한다.
 */
public class StopwatchController implements PuzzleController {

    private static final double TARGET_SECONDS = 5.00;
    private static final double TOLERANCE_SECONDS = 0.20;
    /** 이 시간이 지나면 숫자와 바늘을 가려 감으로 맞추게 한다. 가리지 않으려면 Double.MAX_VALUE. */
    private static final double BLIND_AFTER_SECONDS = 3.00;
    private static final double SECONDS_PER_TURN = 60.0;
    private static final double DIAL_CX = 282;
    private static final double DIAL_CY = 164;

    @FXML private Label targetLabel;
    @FXML private Label timeLabel;
    @FXML private Label resultLabel;
    @FXML private Button startButton;
    @FXML private Button stopButton;
    @FXML private Button resetButton;
    @FXML private ImageView deviceImage;
    @FXML private Rectangle handCover;
    @FXML private Line hand;

    private final Rotate handRotate = new Rotate(0, DIAL_CX, DIAL_CY);
    private Runnable onSolved = () -> { };
    private long startNanos;
    private boolean running;
    private boolean solved;

    private final AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            double elapsed = (now - startNanos) / 1_000_000_000.0;
            boolean blind = elapsed >= BLIND_AFTER_SECONDS;
            timeLabel.setText(blind ? "??.??" : format(elapsed));
            hand.setVisible(!blind);
            setHand(elapsed);
        }
    };

    @FXML
    private void initialize() {
        if (Images.apply(deviceImage, "stopwatch_closeup.png")) {
            handCover.setVisible(true);
        }
        hand.getTransforms().add(handRotate);

        // 주방 싱크대 단서(물방울 다섯 번)를 봤으면 목표 시간이 보이고, 아니면 가려진다.
        if (GameState.get().hasClue(GameState.CLUE_DRIP)) {
            targetLabel.setText(String.format("목표: %.2f초 (±%.2f초) 에 정지하라", TARGET_SECONDS, TOLERANCE_SECONDS));
        } else {
            targetLabel.setText(String.format("목표: ?.??초 (±%.2f초) - 명판이 닳아 숫자가 보이지 않는다. 주방에 단서가 있을지도.",
                    TOLERANCE_SECONDS));
        }
        resultLabel.setText(BLIND_AFTER_SECONDS < TARGET_SECONDS
                ? String.format("※ %.0f초가 지나면 숫자와 바늘이 보이지 않는다. 위쪽 빨간 버튼으로도 조작할 수 있다.", BLIND_AFTER_SECONDS)
                : "");
    }

    @Override
    public void setOnSolved(Runnable onSolved) {
        this.onSolved = onSolved;
    }

    @Override
    public void onClose() {
        SoundManager.stopSfx("sfx_tick");
        timer.stop();
        running = false;
    }

    /** 그림 위 빨간 버튼: 멈춰 있으면 시작, 돌고 있으면 정지. 실패 후에는 리셋 후 시작. */
    @FXML
    private void onTopButton() {
        if (solved) {
            return;
        }
        if (running) {
            onStop();
        } else {
            onReset();
            onStart();
        }
    }

    @FXML
    private void onStart() {
        if (running || solved) {
            return;
        }
        startNanos = System.nanoTime();
        running = true;
        hand.setVisible(true);
        setHand(0);
        timer.start();
        SoundManager.loopSfx("sfx_tick");
        startButton.setDisable(true);
        stopButton.setDisable(false);
        resultLabel.setText("");
    }

    @FXML
    private void onStop() {
        if (!running) {
            return;
        }
        double elapsed = (System.nanoTime() - startNanos) / 1_000_000_000.0;
        timer.stop();
        SoundManager.stopSfx("sfx_tick");
        running = false;
        stopButton.setDisable(true);
        timeLabel.setText(format(elapsed));
        hand.setVisible(true);
        setHand(elapsed);

        if (Math.abs(elapsed - TARGET_SECONDS) <= TOLERANCE_SECONDS) {
            solved = true;
            resetButton.setDisable(true);
            resultLabel.setText("성공! " + format(elapsed) + "초. 장치가 딸깍 소리를 낸다.");
            PauseTransition wait = new PauseTransition(Duration.seconds(1.0));
            wait.setOnFinished(e -> onSolved.run());
            wait.play();
        } else {
            String diff = elapsed < TARGET_SECONDS ? "너무 빨랐다" : "너무 늦었다";
            resultLabel.setText(format(elapsed) + "초 - " + diff + ". 리셋 후 다시 시도하자.");
            GameState.get().penalize(GameState.PENALTY_SMALL, "스톱워치 실패");
        }
    }

    @FXML
    private void onReset() {
        if (solved) {
            return;
        }
        timer.stop();
        SoundManager.stopSfx("sfx_tick");
        running = false;
        timeLabel.setText("00.00");
        hand.setVisible(true);
        setHand(0);
        resultLabel.setText("");
        startButton.setDisable(false);
        stopButton.setDisable(true);
    }

    private void setHand(double seconds) {
        handRotate.setAngle(seconds / SECONDS_PER_TURN * 360.0);
    }

    private static String format(double seconds) {
        return String.format("%05.2f", seconds);
    }
}

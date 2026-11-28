package escape;

import java.util.Random;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * [E] 멈춘 감시 화면. 모니터 6대가 에피소드 1 저택의 방을 비춘다.
 * 화면마다 시계가 1초씩 흐르는데, 한 대만 시간이 멈춰 있다(녹화 화면) → 그 화면을 누른다.
 * 어려움: 멈춘 화면이 30초마다 바뀐다.
 */
public class Ep2MonitorController extends Ep2Puzzle {

    private static final String[] ROOMS = {"bedroom.png", "study.png", "entrance_bg.png",
        "dining_bg.png", "kitchen_bg.png", "storage_bg.png"};
    private static final String[] NAMES = {"침실", "서재", "현관 홀", "식당", "주방", "지하 저장고"};
    private static final double MW = 196;
    private static final double MH = 110;

    private final Random random = new Random();
    private final Label[] clocks = new Label[ROOMS.length];
    private int frozen;
    private int frozenTime;
    private int now;
    private int ticks;
    private Timeline timer;
    private Label result;
    private boolean done;

    @Override
    protected void build() {
        SoundManager.sfx("sfx_static", 0.6);
        frozen = random.nextInt(ROOMS.length);
        now = 23 * 3600 + Math.min(state.getUsedSeconds(), 38 * 60) + 7;
        frozenTime = now - frozen - 1 + random.nextInt(3);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(javafx.geometry.Pos.CENTER);
        for (int i = 0; i < ROOMS.length; i++) {
            grid.add(monitor(i), i % 3, i / 3);
        }
        result = text("");
        result.setMinHeight(20);
        root.getChildren().addAll(grid,
                text("여섯 화면이 저택의 방들을 비추고 있다. 화면 아래 시계를 잘 보자."), result);
        updateClocks();

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        timer.setCycleCount(Animation.INDEFINITE);
        timer.play();
    }

    private Button monitor(int i) {
        ImageView view = picture(ROOMS[i], MW, MH);
        view.setEffect(new ColorAdjust(0, -0.75, -0.18, 0.1));
        Rectangle scan = new Rectangle(MW, MH);
        scan.getStyleClass().add("monitor-scan");
        scan.setMouseTransparent(true);
        Label cam = new Label("CAM " + (i + 1) + "  " + NAMES[i]);
        cam.getStyleClass().add("monitor-cam");
        StackPane screen = new StackPane(view, scan, cam);
        StackPane.setAlignment(cam, javafx.geometry.Pos.TOP_LEFT);
        clocks[i] = new Label();
        clocks[i].getStyleClass().add("monitor-clock");
        VBox box = new VBox(4, screen, clocks[i]);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        Button b = new Button();
        b.setGraphic(box);
        b.getStyleClass().add("monitor-tile");
        b.setAccessibleText("모니터 " + (i + 1));
        b.setOnAction(e -> pick(i));
        return b;
    }

    private void tick() {
        now++;
        ticks++;
        if (state.getDifficulty() == GameState.Difficulty.HARD && ticks % 30 == 0 && !done) {
            int old = frozen;
            frozen = (frozen + 1 + random.nextInt(ROOMS.length - 1)) % ROOMS.length;
            frozenTime = now - frozen - 1;
            if (old != frozen) {
                result.setText("지지직- 화면들이 한 번 흔들렸다.");
            }
        }
        updateClocks();
    }

    private void updateClocks() {
        for (int i = 0; i < clocks.length; i++) {
            int t = i == frozen ? frozenTime : now - i * 1;
            clocks[i].setText(String.format("● REC  %02d:%02d:%02d", (t / 3600) % 24, (t / 60) % 60, t % 60));
        }
    }

    private void pick(int i) {
        if (done) {
            return;
        }
        if (i == frozen) {
            done = true;
            timer.stop();
            result.setText("이 화면만 시간이 흐르지 않는다. 녹화된 영상이었다! 모니터가 덜컥 움직인다.");
            javafx.animation.PauseTransition wait = new javafx.animation.PauseTransition(Duration.millis(900));
            wait.setOnFinished(e -> solve());
            wait.play();
        } else {
            result.setText("CAM " + (i + 1) + "의 시계는 제대로 흐르고 있다.");
            state.penalize(GameState.PENALTY_SMALL, "감시 화면 오답");
        }
    }

    @Override
    protected void stop() {
        if (timer != null) {
            timer.stop();
        }
    }
}

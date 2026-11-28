package escape;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

/**
 * 에피소드 2 등대 3층 기관실.
 * - 공구함 → 큰 톱니바퀴 + 기록 5
 * - 축 3개에 톱니 끼우기 [F] (위 32 / 왼쪽 8 / 오른쪽 16) → 손잡이로 시계 장치 가동
 * - 큰 시계 바늘을 11:40 에 맞추기 [G] → 금 인장
 * - 인장 철문: 인장 3개를 끼우면 꼭대기 등실로
 */
public class Ep2EngineController extends Ep2Room {

    @FXML private ImageView gearLarge;
    @FXML private ImageView gearSmall;
    @FXML private ImageView gearMedium;
    @FXML private Pane clockHands;
    @FXML private Button crankButton;
    @FXML private Button clockButton;
    @FXML private Button chestButton;
    @FXML private Button doorButton;

    private final List<RotateTransition> spinning = new ArrayList<>();

    @Override
    protected String backgroundFile() {
        return "ep2_gear_bg.png";
    }

    @Override
    public void setHost(GameHost host) {
        gearLarge.setImage(Images.load("ep2_gear_large.png"));
        gearSmall.setImage(Images.load("ep2_gear_small.png"));
        gearMedium.setImage(Images.load("ep2_gear_medium.png"));
        drawClockHands();
        super.setHost(host);
    }

    @Override
    protected void refresh() {
        gearLarge.setVisible(state.hasFlag(Episode2.F_GEAR_L_PLACED));
        gearSmall.setVisible(state.hasFlag(Episode2.F_GEAR_S_PLACED));
        gearMedium.setVisible(state.hasFlag(Episode2.F_GEAR_M_PLACED));
        boolean running = state.isCleared(Episode2.PUZZLE_GEARS);
        if (running && spinning.isEmpty()) {
            startSpinning();
        }
        clockHands.setVisible(state.isCleared(Episode2.PUZZLE_CLOCK));
        crankButton.setText(running ? "손잡이 ✔" : "손잡이");
        clockButton.setText(state.isCleared(Episode2.PUZZLE_CLOCK) ? "큰 시계 ✔" : "큰 시계");
        chestButton.setText(state.hasFlag(Episode2.F_GEAR_L_TAKEN) ? "공구함 (열림)" : "공구함");
        doorButton.setText(state.hasFlag(Episode2.F_SEALS_IN) ? "꼭대기로" : "인장 철문");
    }

    // ================= 공구함 =================
    @FXML
    private void onChest() {
        if (rejectHeldItem()) {
            return;
        }
        boolean first = !state.hasFlag(Episode2.F_GEAR_L_TAKEN);
        Story.readDoc(host, 5);
        if (first) {
            takeOnce(Episode2.F_GEAR_L_TAKEN, Episode2.GEAR_LARGE, "공구함 안에 큰 톱니바퀴와 정비 일지가 들어 있다.");
        }
    }

    // ================= 톱니바퀴 [F] =================
    @FXML
    private void onSpindleTop() {
        placeGear(Episode2.GEAR_LARGE, Episode2.F_GEAR_L_PLACED, "위쪽 큰 축", 32);
    }

    @FXML
    private void onSpindleLeft() {
        placeGear(Episode2.GEAR_SMALL, Episode2.F_GEAR_S_PLACED, "왼쪽 축", 8);
    }

    @FXML
    private void onSpindleRight() {
        placeGear(Episode2.GEAR_MEDIUM, Episode2.F_GEAR_M_PLACED, "오른쪽 축", 16);
    }

    private void placeGear(String rightGear, String placedFlag, String spindleName, int teeth) {
        String held = state.getSelectedItem();
        if (state.hasFlag(placedFlag)) {
            if (held != null) {
                rejectHeldItem();
            } else {
                host.showMessage(spindleName + "에는 이미 톱니바퀴가 끼워져 있다.");
            }
            return;
        }
        if (held == null) {
            host.showMessage(spindleName + "이 비어 있다. 축 옆에 '" + teeth + "'이라는 숫자가 새겨져 있다.");
            state.addClue(Episode2.SPINDLE_NOTE);
            return;
        }
        boolean isGear = held.equals(Episode2.GEAR_SMALL) || held.equals(Episode2.GEAR_MEDIUM)
                || held.equals(Episode2.GEAR_LARGE);
        if (!isGear) {
            rejectHeldItem();
            return;
        }
        if (!held.equals(rightGear)) {
            state.clearSelection();
            SoundManager.sfx(SoundManager.first("sfx_gear_place", "sfx_wrong"));
            state.penalize(GameState.PENALTY_SMALL, "톱니바퀴 크기 오류");
            host.showMessage("[" + GameState.displayName(held) + "]을(를) 끼우려 했지만 맞물리지 않고 튕겨 나왔다. "
                    + "축 옆 숫자는 '" + teeth + "'이다.");
            return;
        }
        state.removeItem(held);
        state.setFlag(placedFlag);
        SoundManager.sfx(SoundManager.first("sfx_gear_place", "sfx_item"));
        host.showMessage("[" + GameState.displayName(held) + "]을(를) " + spindleName + "에 끼웠다. 딱 맞는다."
                + (Episode2.gearsDone(state) ? " 톱니 세 개가 모두 맞물렸다. 손잡이를 돌려 보자." : ""));
    }

    @FXML
    private void onCrank() {
        if (rejectHeldItem()) {
            return;
        }
        if (state.isCleared(Episode2.PUZZLE_GEARS)) {
            host.showMessage("톱니바퀴들이 맞물려 돌아가고 있다.");
            return;
        }
        if (!Episode2.gearsDone(state)) {
            host.showMessage("손잡이를 돌려도 헛돈다. 빈 축에 톱니바퀴를 끼워야 한다.");
            return;
        }
        SoundManager.sfx(SoundManager.first("sfx_clockwork", "sfx_unlock"));
        state.markCleared(Episode2.PUZZLE_GEARS);
        host.showMessage("드르륵- 손잡이를 돌리자 톱니바퀴가 맞물려 돈다. 큰 시계 안쪽에서 째깍거리는 소리가 나기 시작했다.");
    }

    private void startSpinning() {
        spin(gearLarge, 32, 1);
        spin(gearSmall, 8, -1);
        spin(gearMedium, 16, -1);
    }

    private void spin(ImageView gear, int teeth, int dir) {
        RotateTransition rt = new RotateTransition(Duration.seconds(teeth * 0.5), gear);
        rt.setByAngle(360 * dir);
        rt.setInterpolator(Interpolator.LINEAR);
        rt.setCycleCount(Animation.INDEFINITE);
        rt.play();
        spinning.add(rt);
    }

    // ================= 시계 [G] =================
    @FXML
    private void onClock() {
        if (rejectHeldItem()) {
            return;
        }
        if (state.isCleared(Episode2.PUZZLE_CLOCK)) {
            host.showMessage("시계가 11시 40분을 가리키고 있다. 등불이 켜지는 시각.");
            return;
        }
        if (!state.isCleared(Episode2.PUZZLE_GEARS)) {
            host.showMessage("바늘 없는 커다란 시계. 시계 장치가 멈춰 있다. 왼쪽 기계를 움직여야 할 것 같다.");
            return;
        }
        host.openPuzzle("기관실 시계", "Ep2ClockView.fxml", () -> {
            state.markCleared(Episode2.PUZZLE_CLOCK);
            state.addItem(Episode2.SEAL_GOLD);
            host.closeOverlay();
            host.showMessage("바늘이 11시 40분에 맞춰지자 시계 가운데가 열리며 [금 인장]이 굴러 나왔다. 금 인장 획득!");
        });
    }

    /** 방 그림 위 시계에 11:40 바늘을 그린다. 시계 중심 (533, 228). */
    private void drawClockHands() {
        double cx = 533;
        double cy = 228;
        Line hour = hand(cx, cy, (11 + 40 / 60.0) * 30, 62, 7);
        Line minute = hand(cx, cy, 40 * 6, 98, 4.5);
        Circle pin = new Circle(cx, cy, 7, Color.web("#6b4a1f"));
        clockHands.getChildren().setAll(hour, minute, pin);
    }

    private static Line hand(double cx, double cy, double angleDeg, double length, double width) {
        double rad = Math.toRadians(angleDeg);
        Line l = new Line(cx, cy, cx + Math.sin(rad) * length, cy - Math.cos(rad) * length);
        l.setStroke(Color.web("#2b2118"));
        l.setStrokeWidth(width);
        l.setStrokeLineCap(StrokeLineCap.ROUND);
        return l;
    }

    // ================= 인장 철문 =================
    @FXML
    private void onDoor() {
        if (state.hasFlag(Episode2.F_SEALS_IN)) {
            move(Episode2.LANTERN, "계단 끝 철문을 지나 꼭대기 등실로 올라왔다. 사방이 유리창이다.");
            return;
        }
        if (Episode2.hasAllSeals(state)) {
            for (String seal : Episode2.SEALS) {
                state.removeItem(seal);
            }
            state.setFlag(Episode2.F_SEALS_IN);
            host.showMessage("청동, 은, 금 인장을 문의 구멍에 차례로 끼웠다. 철컥- 철컥- 철컥. 위로 가는 문이 열렸다!");
            return;
        }
        if (rejectHeldItem()) {
            return;
        }
        int have = 0;
        for (String seal : Episode2.SEALS) {
            if (state.hasItem(seal)) {
                have++;
            }
        }
        host.showMessage("계단 끝 철문에 둥근 구멍이 세 개 있다. 등불 인장이 들어갈 자리다. (가진 인장 " + have + "/3)");
    }

    @FXML
    private void goDown() {
        move(Episode2.OBSERVATION, "계단을 내려가 2층 관찰실로 왔다.");
    }
}

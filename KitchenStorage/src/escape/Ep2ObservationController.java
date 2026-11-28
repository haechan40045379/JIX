package escape;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.shape.Rectangle;

/**
 * 에피소드 2 등대 2층 관찰실.
 * - 배전반: 회로 타일 [D] → 전원 (배경이 켜진 그림으로 바뀜)
 * - 모니터: 멈춘 감시 화면 찾기 [E] → 은 인장
 * - 콘솔: 점등 시각 23:40 (기관실 시계 단서), 녹음기: 기록 4
 */
public class Ep2ObservationController extends Ep2Room {

    @FXML private Rectangle darkness;
    @FXML private Button boardButton;
    @FXML private Button monitorButton;

    private Boolean shownPowered;

    @Override
    protected String backgroundFile() {
        return powered() ? "ep2_observation_bg_on.png" : "ep2_observation_bg.png";
    }

    private boolean powered() {
        return state.hasFlag(Episode2.F_POWER);
    }

    @Override
    protected void refresh() {
        boolean on = powered();
        if (shownPowered == null || shownPowered != on) {
            shownPowered = on;
            Images.apply(background, backgroundFile());
            darkness.setVisible(!on);
        }
        boardButton.setText(on ? "배전반 ✔" : "배전반");
        monitorButton.setText(state.isCleared(Episode2.PUZZLE_MONITOR) ? "모니터 ✔" : "모니터");
    }

    @FXML
    private void onBoard() {
        if (rejectHeldItem()) {
            return;
        }
        if (powered()) {
            host.showMessage("배전반의 회로가 모두 이어져 있다. 낮은 전기 소리가 난다.");
            return;
        }
        host.openPuzzle("배전반 - 회로 잇기", "Ep2CircuitView.fxml", () -> {
            state.setFlag(Episode2.F_POWER);
            state.markCleared(Episode2.PUZZLE_CIRCUIT);
            host.closeOverlay();
            host.showMessage("위잉- 전원이 들어왔다! 모니터 여섯 대가 하나씩 켜지고 콘솔에 초록 불이 들어온다.");
        });
    }

    @FXML
    private void onMonitors() {
        if (rejectHeldItem()) {
            return;
        }
        if (!powered()) {
            host.showMessage("모니터 여섯 대가 모두 꺼져 있다. 전원이 필요하다.");
            return;
        }
        if (state.isCleared(Episode2.PUZZLE_MONITOR)) {
            host.showMessage("멈춰 있던 화면은 꺼졌다. 그 뒤의 작은 칸은 비어 있다.");
            return;
        }
        host.openPuzzle("감시 모니터", "Ep2MonitorView.fxml", () -> {
            state.markCleared(Episode2.PUZZLE_MONITOR);
            state.addItem(Episode2.SEAL_SILVER);
            host.closeOverlay();
            host.showMessage("멈춘 화면을 누르자 모니터가 앞으로 열리며 뒤쪽 칸이 드러났다. [은 인장] 획득!");
        });
    }

    @FXML
    private void onConsole() {
        if (rejectHeldItem()) {
            return;
        }
        if (!powered()) {
            host.showMessage("콘솔의 스위치를 눌러도 반응이 없다. 전원이 없다.");
            return;
        }
        boolean known = state.hasClue(Episode2.CONSOLE_LOG);
        state.addClue(Episode2.CONSOLE_LOG);
        host.showMessage("콘솔에서 종이 기록지가 밀려 나온다. '자동 점등 예정 23:40. 수동 조작 금지.'"
                + (known ? "" : " (단서 기록에 추가됨)"));
    }

    @FXML
    private void onMemo() {
        if (rejectHeldItem()) {
            return;
        }
        Story.flavor(host, Episode2.MONITOR_NOTE);
    }

    @FXML
    private void goUp() {
        move(Episode2.ENGINE, "계단을 올라 3층 기관실로 왔다. 기름 냄새와 쇠 냄새가 난다.");
    }

    @FXML
    private void goDown() {
        move(Episode2.ARCHIVE, "계단을 내려가 1층 기록 보관소로 왔다.");
    }
}

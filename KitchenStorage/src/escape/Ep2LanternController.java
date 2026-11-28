package escape;

import javafx.fxml.FXML;

/**
 * 에피소드 2 꼭대기 등실.
 * - 거대한 렌즈: 거울 바닥 퍼즐 [H] = 마지막 선택 (등불을 켠다 / 끈다) → 엔딩
 * - 의자 위 편지: 기록 6
 */
public class Ep2LanternController extends Ep2Room {

    @Override
    protected String backgroundFile() {
        return "ep2_lantern_bg.png";
    }

    @FXML
    private void onLens() {
        if (rejectHeldItem()) {
            return;
        }
        host.openPuzzle("등불과 거울 바닥", "Ep2MirrorView.fxml", () -> { });
        host.showMessage("렌즈 아래 바닥에 거울이 박힌 격자가 있다. 빛을 어디로 보낼지는 내가 정한다.");
    }

    @FXML
    private void goDown() {
        move(Episode2.ENGINE, "계단을 내려가 3층 기관실로 왔다.");
    }
}

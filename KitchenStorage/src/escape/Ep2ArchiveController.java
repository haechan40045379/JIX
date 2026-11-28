package escape;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * 에피소드 2 등대 1층 기록 보관소.
 * - 회벽 / 책상: UV 손전등을 들고 비추면 숨은 글씨 [B]
 * - 서랍장: 13칸 서랍 순서 퍼즐 [C] → 청동 인장 + 기록 3
 * - 서랍장 아래 → 중간 톱니바퀴, 책상 서류 → 기록 2
 */
public class Ep2ArchiveController extends Ep2Room {

    @FXML private Label wallWriting;
    @FXML private Label deskWriting;
    @FXML private Button catalogButton;
    @FXML private Button underButton;

    @Override
    protected String backgroundFile() {
        return "ep2_archive_bg.png";
    }

    @Override
    protected void refresh() {
        wallWriting.setVisible(state.hasClue(Episode2.UV_WALL_NOTE));
        deskWriting.setVisible(state.hasClue(Episode2.UV_DESK_NOTE));
        catalogButton.setText(state.isCleared(Episode2.PUZZLE_CATALOG) ? "서랍장 ✔" : "서랍장");
        underButton.setText(state.hasFlag(Episode2.F_GEAR_M_TAKEN) ? "서랍장 아래 (빔)" : "서랍장 아래");
    }

    private boolean holdingUv() {
        return state.isSelected(Episode2.UV_FLASHLIGHT);
    }

    @FXML
    private void onWall() {
        if (holdingUv()) {
            SoundManager.sfx(SoundManager.first("sfx_uv_on", "sfx_click"));
            Story.flavor(host, Episode2.UV_WALL_NOTE);
            return;
        }
        if (rejectHeldItem()) {
            return;
        }
        host.showMessage(state.hasClue(Episode2.UV_WALL_NOTE)
                ? "보랏빛에 드러났던 글씨: '실패한 자들의 순서로, 마지막은 처음으로.'"
                : "얼룩진 회벽. 유난히 깨끗하게 비어 있다. 맨눈으로는 아무것도 보이지 않는다.");
    }

    @FXML
    private void onDesk() {
        if (holdingUv()) {
            SoundManager.sfx(SoundManager.first("sfx_uv_on", "sfx_click"));
            Story.flavor(host, Episode2.UV_DESK_NOTE);
            return;
        }
        if (rejectHeldItem()) {
            return;
        }
        Story.readDoc(host, 2);
    }

    @FXML
    private void onCatalog() {
        if (rejectHeldItem()) {
            return;
        }
        if (state.isCleared(Episode2.PUZZLE_CATALOG)) {
            host.showMessage("서랍 1이 열린 채로 있다. 안은 비어 있다.");
            return;
        }
        host.openPuzzle("참가자 서랍장", "Ep2CatalogView.fxml", () -> {
            state.markCleared(Episode2.PUZZLE_CATALOG);
            state.addItem(Episode2.SEAL_BRONZE);
            host.closeOverlay();
            Story.readDoc(host, 3);
            host.showMessage("딸깍. 1번 서랍이 열렸다. 파일 위에 [청동 인장]이 놓여 있다. 청동 인장 획득!");
        });
    }

    @FXML
    private void onUnder() {
        if (rejectHeldItem()) {
            return;
        }
        if (!takeOnce(Episode2.F_GEAR_M_TAKEN, Episode2.GEAR_MEDIUM, "서랍장 밑 틈으로 손을 넣자 차가운 쇠붙이가 잡힌다.")) {
            host.showMessage("틈에는 먼지뿐이다.");
        }
    }

    @FXML
    private void goUp() {
        move(Episode2.OBSERVATION, "삐걱이는 나선 계단을 올라 2층으로 왔다. 어둡다.");
    }

    @FXML
    private void goPier() {
        move(Episode2.PIER, "철문을 지나 선착장으로 나왔다. 안개가 더 짙어졌다.");
    }
}

package escape;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * 에피소드 2 선착장.
 * - 신호등: 모스 신호 재생 [A]   - 선원 수첩: 숫자 신호표
 * - 등대 철문: 4자리 다이얼 (0113)
 * - 어구 상자 → UV 손전등, 닻 밑 → 작은 톱니바퀴
 */
public class Ep2PierController extends Ep2Room {

    @FXML private Button doorButton;
    @FXML private Button crateButton;
    @FXML private Button anchorButton;

    @Override
    protected String backgroundFile() {
        return "ep2_pier_bg.png";
    }

    @Override
    protected void refresh() {
        doorButton.setText(state.isCleared(Episode2.PUZZLE_DIAL) ? "등대 안으로" : "등대 철문");
        crateButton.setText(state.hasFlag(Episode2.F_UV_TAKEN) ? "어구 상자 (빔)" : "어구 상자");
        anchorButton.setText(state.hasFlag(Episode2.F_GEAR_S_TAKEN) ? "닻 (빔)" : "닻");
    }

    @FXML
    private void onSignal() {
        if (rejectHeldItem()) {
            return;
        }
        host.openPuzzle("장대 위 신호등", "Ep2MorseView.fxml", () -> { });
        host.showMessage("신호등을 올려다본다. 유리 안에서 무언가 깜빡일 준비를 하고 있다.");
    }

    @FXML
    private void onLake(javafx.event.ActionEvent event) {
        if (state.getSelectedItem() == null) {
            SoundManager.sfx("sfx_waves", 0.6);
        }
        onFlavor(event);
    }

    @FXML
    private void onBoat(javafx.event.ActionEvent event) {
        if (state.getSelectedItem() == null) {
            SoundManager.sfx("sfx_boat_creak", 0.6);
        }
        onFlavor(event);
    }

    @FXML
    private void onPost() {
        Story.flavor(host, Episode2.POST_NOTE);
    }

    @FXML
    private void onNotebook() {
        if (rejectHeldItem()) {
            return;
        }
        state.addClue(Episode2.MORSE_TABLE);
        Label paper = new Label(Episode2.MORSE_TABLE);
        paper.getStyleClass().addAll("note-paper", "story-doc", "morse-table");
        host.showOverlay("선원 수첩", paper);
        host.showMessage("못에 걸린 선원 수첩. 숫자를 빛으로 보내는 신호표가 적혀 있다. (단서 기록에 추가됨)");
    }

    @FXML
    private void onCrate() {
        if (rejectHeldItem()) {
            return;
        }
        if (!takeOnce(Episode2.F_UV_TAKEN, Episode2.UV_FLASHLIGHT, "그물 밑에서 보랏빛 렌즈가 달린 손전등을 찾았다.")) {
            host.showMessage("젖은 그물과 낚싯줄뿐이다.");
        }
    }

    @FXML
    private void onAnchor() {
        if (rejectHeldItem()) {
            return;
        }
        if (!takeOnce(Episode2.F_GEAR_S_TAKEN, Episode2.GEAR_SMALL, "무거운 닻을 밀어내자 밑에 작은 톱니바퀴가 깔려 있었다.")) {
            host.showMessage("녹슨 닻. 밑에는 더 이상 아무것도 없다.");
        }
    }

    @FXML
    private void onIronDoor() {
        if (state.isCleared(Episode2.PUZZLE_DIAL)) {
            move(Episode2.ARCHIVE, "철문을 밀고 등대 1층으로 들어왔다. 먼지와 종이 냄새가 난다.");
            return;
        }
        if (rejectHeldItem()) {
            return;
        }
        host.openPuzzle("등대 철문 - 숫자 다이얼", "Ep2DialView.fxml", () -> {
            state.markCleared(Episode2.PUZZLE_DIAL);
            host.closeOverlay();
            host.showMessage("철컥. 무거운 철문이 안쪽으로 열렸다.");
        });
    }
}

package escape;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

/**
 * 지하 저장고 컨트롤러.
 * - 스톱워치 장치: 퍼즐 1
 * - 순서 패널: 퍼즐 2 (전원이 없어 퓨즈를 손에 들고 끼워야 켜진다)
 * - 작업대 아래 상자: 퍼즐 1·2 모두 클리어 시 열리며 냉장고 열쇠 지급
 * - 선반 / 천 / 큰 상자 / 전구 / 술통: 분위기용 (FXML userData 문장 표시)
 * - 주방 계단: 방 전환
 */
public class StorageController implements RoomController {

    @FXML private ImageView background;
    @FXML private Button stopwatchButton;
    @FXML private Button sequencePanelButton;
    @FXML private Button keyBoxButton;

    private final GameState state = GameState.get();
    private GameHost host;

    @Override
    public void setHost(GameHost host) {
        this.host = host;
        Images.apply(background, "storage_bg.png");
        state.addListener(this::refresh);
        refresh();
    }

    private void refresh() {
        stopwatchButton.setText(state.isCleared(GameState.PUZZLE_STOPWATCH) ? "스톱워치\n✔" : "스톱워치");
        sequencePanelButton.setText(state.isCleared(GameState.PUZZLE_SEQUENCE) ? "순서 패널 ✔"
                : state.isPanelPowered() ? "순서 패널" : "순서 패널 (전원 꺼짐)");
        boolean opened = state.isCleared(GameState.PUZZLE_STOPWATCH) && state.isCleared(GameState.PUZZLE_SEQUENCE);
        keyBoxButton.setText(opened ? "상자\n(열림)" : "작업대 아래 상자");
    }

    /** 아이템을 든 채 쓸 수 없는 곳을 누르면 안내 후 내려놓는다. */
    private boolean rejectHeldItem() {
        String held = state.getSelectedItem();
        if (held == null) {
            return false;
        }
        host.showMessage("[" + GameState.displayName(held) + "]은(는) 여기에 쓸 수 없다.");
        state.clearSelection();
        return true;
    }

    @FXML
    private void onStopwatch() {
        if (rejectHeldItem()) {
            return;
        }
        if (state.isCleared(GameState.PUZZLE_STOPWATCH)) {
            host.showMessage("스톱워치는 이미 정확히 멈춰 있다.");
            return;
        }
        host.openPuzzle("스톱워치 장치", "StopwatchView.fxml",
                () -> onStoragePuzzleSolved(GameState.PUZZLE_STOPWATCH, "딸깍! 스톱워치 장치가 풀렸다."));
    }

    @FXML
    private void onSequencePanel() {
        boolean holdingFuse = state.isSelected(GameState.FUSE);
        if (!(holdingFuse && !state.isPanelPowered()) && rejectHeldItem()) {
            return;
        }
        if (state.isCleared(GameState.PUZZLE_SEQUENCE)) {
            host.showMessage("순서 패널의 불빛이 모두 켜져 있다.");
            return;
        }
        if (!state.isPanelPowered()) {
            if (holdingFuse) {
                state.insertFuse();
                host.showMessage("패널 뒤 빈 퓨즈 칸에 퓨즈를 끼웠다. 위잉- 패널 표시등에 불이 들어왔다!");
            } else if (state.hasItem(GameState.FUSE)) {
                host.showMessage("전원이 들어오지 않는다. 패널 뒤 퓨즈 칸이 비어 있다. 인벤토리에서 퓨즈를 손에 들고 눌러보자.");
                return;
            } else {
                host.showMessage("버튼을 눌러도 반응이 없다. 패널 뒤를 보니 퓨즈 칸이 비어 있다.");
                return;
            }
        }
        host.openPuzzle("순서 패널 - 숫자/색깔 기억하기", "SequenceView.fxml",
                () -> onStoragePuzzleSolved(GameState.PUZZLE_SEQUENCE, "철컥! 순서 패널이 풀렸다."));
    }

    @FXML
    private void onKeyBox() {
        if (rejectHeldItem()) {
            return;
        }
        boolean stopwatch = state.isCleared(GameState.PUZZLE_STOPWATCH);
        boolean sequence = state.isCleared(GameState.PUZZLE_SEQUENCE);
        if (stopwatch && sequence) {
            host.showMessage(state.hasItem(GameState.FRIDGE_KEY) || state.isFridgeUnlocked()
                    ? "상자는 비어 있다." : "상자 안에는 아무것도 없다.");
            return;
        }
        int locked = (stopwatch ? 0 : 1) + (sequence ? 0 : 1);
        host.showMessage("자물쇠가 " + locked + "개 남아 있다. 선이 스톱워치 장치와 순서 패널로 이어져 있다.");
    }

    private void onStoragePuzzleSolved(String puzzleId, String solvedMessage) {
        state.markCleared(puzzleId);
        host.closeOverlay();
        if (state.tryGiveFridgeKey()) {
            host.showMessage(solvedMessage + " 작업대 아래 상자의 자물쇠가 모두 풀렸다. ["
                    + GameState.displayName(GameState.FRIDGE_KEY) + "] 획득!");
        } else {
            host.showMessage(solvedMessage + " 작업대 아래 상자의 자물쇠 하나가 풀렸다.");
        }
    }

    /** 분위기용 오브젝트: FXML의 userData 문장을 그대로 보여준다. */
    @FXML
    private void onFlavor(ActionEvent event) {
        if (rejectHeldItem()) {
            return;
        }
        Object text = ((Node) event.getSource()).getUserData();
        if (text == null) {
            host.showMessage("특별한 것은 없다.");
        } else if (GameState.isParticipantNote(text.toString())) {
            state.addClue(text.toString());
            host.showMessage(text + " (단서 기록에 추가됨)");
        } else {
            host.showMessage(text.toString());
        }
    }

    @FXML
    private void goToKitchen() {
        host.goTo(GameHost.KITCHEN);
        host.showMessage("계단을 올라 주방으로 돌아왔다.");
    }
}

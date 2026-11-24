package escape;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * 주방 컨트롤러.
 * - 냉장고: 잠김 → (냉장고 열쇠) → 도형 맞추기 → 열쇠 조각
 * - 쪽지: "냉장고 열쇠는 창고에" 단서
 * - 싱크대: 물방울 다섯 번 → 스톱워치 목표 시간(5초) 단서
 * - 싱크대 아래 수납장: 퓨즈 획득 (창고 순서 패널 전원에 필요)
 * - 아이템을 손에 든 채 클릭하면 사용 시도 (냉장고 + 냉장고 열쇠), 안 맞으면 "쓸 수 없다"
 * - 선반 / 나무 상자 / 조리도구: 분위기용 (FXML userData 문장 표시)
 * - 지하 저장고 문 / 식당: 방 전환
 */
public class KitchenController implements RoomController {

    @FXML private ImageView background;
    @FXML private Button fridgeButton;
    @FXML private Button cabinetButton;

    private final GameState state = GameState.get();
    private GameHost host;
    /** 퓨즈는 패널에 끼우면 인벤토리에서 사라지므로, 수납장 상태는 따로 기억한다. */
    private boolean fuseTaken;

    @Override
    public void setHost(GameHost host) {
        this.host = host;
        Images.apply(background, "kitchen_bg.png");
        state.addListener(this::refresh);
        refresh();
    }

    private void refresh() {
        if (state.isCleared(GameState.PUZZLE_SHAPE)) {
            fridgeButton.setText("냉장고\n(비었다)");
        } else if (state.isFridgeUnlocked()) {
            fridgeButton.setText("냉장고\n(열림)");
        } else {
            fridgeButton.setText("냉장고\n(잠김)");
        }
        cabinetButton.setText(fuseTaken ? "수납장\n(비었다)" : "수납장");
    }

    @FXML
    private void onFridge() {
        boolean holdingKey = state.isSelected(GameState.FRIDGE_KEY);
        if (!(holdingKey && !state.isFridgeUnlocked()) && rejectHeldItem()) {
            return;
        }
        if (state.isCleared(GameState.PUZZLE_SHAPE)) {
            host.showMessage("냉장고 안은 이제 텅 비어 있다.");
        } else if (state.isFridgeUnlocked()) {
            openShapePuzzle();
        } else if (holdingKey) {
            state.unlockFridge();
            host.showMessage("냉장고 열쇠로 자물쇠를 풀었다! 안쪽에 도형 장치가 있다.");
            openShapePuzzle();
        } else if (state.hasItem(GameState.FRIDGE_KEY)) {
            host.showMessage("자물쇠로 잠겨 있다. 인벤토리에서 냉장고 열쇠를 클릭해 손에 든 뒤 다시 눌러보자.");
        } else {
            host.showMessage("자물쇠로 잠겨 있다. 창고에서 열쇠를 찾아야 할 것 같다.");
        }
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
    private void onNote() {
        if (rejectHeldItem()) {
            return;
        }
        state.addClue(GameState.CLUE_NOTE);
        showPaper("단서 쪽지", "조리대 위에 접힌 쪽지가 있다. (단서 기록에 추가됨)", GameState.CLUE_NOTE);
        host.showMessage("단서: " + GameState.CLUE_NOTE);
    }

    @FXML
    private void onSink() {
        if (rejectHeldItem()) {
            return;
        }
        state.addClue(GameState.CLUE_DRIP);
        host.showMessage("똑... 똑... 똑... 똑... 똑. 물방울이 1초 간격으로 다섯 번 떨어지고 멈췄다. (단서 기록에 추가됨)");
    }

    @FXML
    private void onCabinet() {
        if (rejectHeldItem()) {
            return;
        }
        if (fuseTaken) {
            host.showMessage("수납장에는 녹슨 배관과 거미줄뿐이다.");
            return;
        }
        fuseTaken = true;
        state.addItem(GameState.FUSE);
        host.showMessage("녹슨 배관 뒤에 무언가 끼어 있다. [" + GameState.displayName(GameState.FUSE)
                + "] 획득! (인벤토리에서 더블클릭하면 살펴볼 수 있다)");
        refresh();
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
    private void goToStorage() {
        host.goTo(GameHost.STORAGE);
        host.showMessage("삐걱이는 계단을 내려와 지하 저장고에 들어왔다.");
    }

    @FXML
    private void goToDining() {
        host.goTo(GameHost.DINING);
    }

    private void showPaper(String title, String captionText, String body) {
        Label paper = new Label("\"" + body + "\"");
        paper.getStyleClass().add("note-paper");
        paper.setWrapText(true);
        paper.setMaxWidth(460);

        Label caption = new Label(captionText);
        caption.getStyleClass().add("puzzle-text");

        VBox box = new VBox(14, caption, paper);
        box.setAlignment(Pos.CENTER);
        host.showOverlay(title, box);
    }

    private void openShapePuzzle() {
        host.openPuzzle("냉장고 안 - 도형 맞추기", "ShapePuzzleView.fxml", () -> {
            state.clearShapePuzzle();
            host.closeOverlay();
            host.showMessage("잠금장치가 풀리며 열쇠 조각이 떨어졌다! [" + GameState.displayName(GameState.KEY_FRAGMENT_1)
                    + "] 획득 (" + state.getKeyFragments() + "/" + GameState.TOTAL_KEY_FRAGMENTS + ")"
                    + "  ...어디선가 낮은 남자 목소리가 들린다. \"주방 시험, 통과.\"");
        });
    }
}

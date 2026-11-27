package escape;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * 현관 홀 컨트롤러.
 * - 왼쪽 출입구: 식당 이동
 * - 중앙 목재 현관문: 최종 탈출문 (다른 방으로 연결하지 않음)
 * - 오른쪽 벽면 거꾸로 걸린 액자: 액자 회전 퍼즐 → 찢어진 그림 조각 획득
 * - 오른쪽 협탁 위 메모: 쪽지 확대 오버레이 + 단서 기록 저장
 * - 왼쪽 아래 화분 / 오른쪽 협탁: 분위기용(FXML userData 문장 표시)
 */
public class EntranceController implements RoomController {

    @FXML private ImageView background;
    @FXML private Button frameButton;

    private final GameState state = GameState.get();
    private GameHost host;

    @Override
    public void setHost(GameHost host) {
        this.host = host;
        state.addListener(this::refresh);
        refresh();
    }

    private void refresh() {
        boolean frameSolved = state.isCleared(GameState.PUZZLE_FRAME);
        String bgFile = frameSolved ? "entrance_bg_solved.png" : "entrance_bg.png";
        if (!Images.apply(background, bgFile)) {
            Images.apply(background, "entrance_bg.png");
        }
        if (frameButton != null) {
            frameButton.setText(frameSolved ? "풍경화 액자" : "거꾸로 걸린 액자");
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
    private void onFrontDoor() {
        switch (state.getKeyFragments()) {
            case 0 -> host.showMessage("현관문이 굳게 잠겨 있다. 이곳에서 나가려면 열쇠를 찾아야 할 것 같다.");
            case 1 -> host.showMessage("열쇠 조각 하나를 찾았다. 아직 두 개의 조각이 부족하다.");
            case 2 -> host.showMessage("마지막 열쇠 조각을 찾아야 한다.");
            case GameState.TOTAL_KEY_FRAGMENTS -> state.clearGame();
        }
    }

    @FXML
    private void onUpsideDownFrame() {
        if (rejectHeldItem()) {
            return;
        }
        if (state.isCleared(GameState.PUZZLE_FRAME)) {
            showSolvedFrame();
            host.showMessage("액자는 이미 정상 방향으로 맞춰져 있다.");
            return;
        }
        host.openPuzzle("현관 홀 - 액자 회전", "FramePuzzleView.fxml", () -> {
            state.clearFramePuzzle();
            host.closeOverlay();
            host.showMessage("액자를 바로잡자 뒤쪽에서 오래된 그림 조각이 떨어졌다. ["
                    + GameState.displayName(GameState.PAINTING_FRAGMENT) + "] 획득!");
        });
    }

    @FXML
    private void onMemo() {
        if (rejectHeldItem()) {
            return;
        }
        state.addClue(GameState.CLUE_ENTRANCE_MEMO);
        showPaper("협탁 위 메모", "협탁 위에 놓인 낡은 메모다. (단서 기록에 추가됨)", GameState.CLUE_ENTRANCE_MEMO);
        host.showMessage("협탁 위의 메모를 읽었다. (단서 기록에 추가됨)");
    }

    /** 분위기용 오브젝트: FXML의 userData 문장을 보여준다. */
    @FXML
    private void onFlavor(ActionEvent event) {
        if (rejectHeldItem()) {
            return;
        }
        Object text = ((Node) event.getSource()).getUserData();
        host.showMessage(text == null ? "특별한 것은 없다." : text.toString());
    }

    @FXML
    private void goToDining() {
        host.goTo(GameHost.DINING);
        host.showMessage("왼쪽 문을 통해 식당으로 이동했다.");
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

    private void showSolvedFrame() {
        Label caption = new Label("정상 방향으로 걸린 풍경화 액자다. 뒤쪽에는 더 이상 아무것도 없다.");
        caption.getStyleClass().add("puzzle-text");

        VBox box = new VBox(14, caption);
        box.setAlignment(Pos.CENTER);

        Image img = Images.load("entrance_frame.png");
        if (img != null) {
            ImageView view = new ImageView(img);
            view.setFitWidth(320);
            view.setFitHeight(224);
            view.setPreserveRatio(true);
            view.setSmooth(true);
            box.getChildren().add(view);
        }
        host.showOverlay("풍경화 액자", box);
    }
}

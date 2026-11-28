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
 * 식당 컨트롤러.
 * - 왼쪽 출입구: 현관 홀 이동
 * - 오른쪽 출입구: 주방 이동
 * - 오른쪽 벽면 작은 액자: 찢어진 그림 확대 / 그림 조각 사용 시 복원
 * - 중앙 식탁: 식기 배열 퍼즐 → 탈출 열쇠 조각 2 획득
 * - 왼쪽 식기 수납장 / 중앙 풍경화 액자 / 오른쪽 목재 수납장 / 벽면 촛대 조명: 조사 및 분위기용(FXML userData 문장 표시)
 */
public class DiningController implements RoomController {

    @FXML private ImageView background;
    @FXML private Button paintingButton;
    @FXML private Button tableButton;

    private final GameState state = GameState.get();
    private GameHost host;

    @Override
    public void setHost(GameHost host) {
        this.host = host;
        state.addListener(this::refresh);
        refresh();
    }

    private void refresh() {
        boolean restored = state.isPaintingRestored();
        String bgFile = restored ? "dining_bg_restored.png" : "dining_bg.png";
        if (!Images.apply(background, bgFile)) {
            Images.apply(background, "dining_bg.png");
        }
        if (paintingButton != null) {
            paintingButton.setText(restored ? "복원된 그림" : "작은 액자");
        }
        if (tableButton != null) {
            tableButton.setText(state.isCleared(GameState.PUZZLE_DINING_TABLE) ? "식탁 ✔" : "식탁");
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
    private void onTornPainting() {
        boolean holdingFragment = state.isSelected(GameState.PAINTING_FRAGMENT);
        if (!(holdingFragment && !state.isPaintingRestored()) && rejectHeldItem()) {
            return;
        }
        if (state.isPaintingRestored()) {
            showPaintingOverlay("완성된 식기 정물화", "조각을 맞춰 복원한 그림이다. 식기들이 일정한 위치에 놓여 있다.",
                    "dining_painting_complete.png");
            host.showMessage("복원된 정물화다. 식기들의 배치를 자세히 살펴보자.");
        } else if (holdingFragment) {
            state.restorePainting();
            state.addClue(GameState.CLUE_DINING_PAINTING);
            showPaintingOverlay("그림 복원 완료", "찢어진 자리에 그림 조각이 정확히 들어맞았다. (단서 기록에 추가됨)",
                    "dining_painting_complete.png");
            host.showMessage("그림 조각이 찢어진 부분에 정확히 들어맞는다. 완성된 그림에는 식기들이 일정한 위치에 놓여 있다.");
        } else {
            showPaintingOverlay("찢어진 그림", "그림의 절반이 찢겨 나가 있다. 사라진 부분을 찾아야 할 것 같다.",
                    "dining_painting_torn.png");
            host.showMessage("그림의 절반이 찢겨 나가 있다. 사라진 부분을 찾아야 할 것 같다.");
        }
    }

    @FXML
    private void onDiningTable() {
        if (rejectHeldItem()) {
            return;
        }
        if (state.isCleared(GameState.PUZZLE_DINING_TABLE)) {
            host.showMessage("식기들은 모두 제자리에 놓여 있고, 식탁 아래 숨겨진 수납 장치도 이미 열려 있다.");
            return;
        }
        host.openPuzzle("식탁 - 식기 배열", "DiningTablePuzzleView.fxml", () -> {
            state.clearDiningTablePuzzle();
            host.closeOverlay();
            host.showMessage("식기들이 제자리에 놓이자 식탁 아래 숨겨진 수납 장치가 열렸다. ["
                    + GameState.displayName(GameState.KEY_FRAGMENT_2) + "] 획득!");
        });
    }

    /** 분위기 및 일반 조사 오브젝트: FXML의 userData 문장을 보여준다. */
    @FXML
    private void onFlavor(ActionEvent event) {
        if (rejectHeldItem()) {
            return;
        }
        Story.flavor(host, ((Node) event.getSource()).getUserData());
    }

    @FXML
    private void goToEntrance() {
        host.goTo(GameHost.ENTRANCE);
        host.showMessage("식당 왼쪽 문을 지나 현관 홀에 들어섰다.");
    }

    @FXML
    private void goToKitchen() {
        host.goTo(GameHost.KITCHEN);
        host.showMessage("식당 오른쪽 문을 통해 주방으로 돌아왔다.");
    }

    private void showPaintingOverlay(String title, String captionText, String imageFile) {
        Label caption = new Label(captionText);
        caption.getStyleClass().add("puzzle-text");
        caption.setWrapText(true);
        caption.setMaxWidth(480);

        VBox box = new VBox(14, caption);
        box.setAlignment(Pos.CENTER);

        Image img = Images.load(imageFile);
        if (img != null) {
            ImageView view = new ImageView(img);
            view.setFitWidth(460);
            view.setFitHeight(345);
            view.setPreserveRatio(true);
            view.setSmooth(true);
            box.getChildren().add(view);
        }
        host.showOverlay(title, box);
    }
}

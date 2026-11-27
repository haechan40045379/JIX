package escape;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * 식당 식기 배열 퍼즐 컨트롤러.
 * - 슬롯 순서: [0]=왼쪽, [1]=위쪽, [2]=오른쪽, [3]=오른쪽 위
 * - 정답 배치: 왼쪽=나이프, 위쪽=가로 포크(-90°), 오른쪽=숟가락, 오른쪽 위=와인잔
 * - 식기 클릭 시 선택, 다른 슬롯 클릭 시 위치 교체, 같은 슬롯 재클릭 시 선택 해제.
 * - 그림이 복원된 상태에서만 클리어 시 탈출 열쇠 조각 2를 지급한다.
 */
public class DiningTablePuzzleController implements PuzzleController {

    private enum Utensil {
        KNIFE("나이프", "knife.png"),
        FORK("포크", "fork.png"),
        SPOON("숟가락", "spoon.png"),
        WINE_GLASS("와인잔", "wine_glass.png");

        final String label;
        final String imageFile;

        Utensil(String label, String imageFile) {
            this.label = label;
            this.imageFile = imageFile;
        }
    }

    private static final int SLOT_LEFT = 0;
    private static final int SLOT_TOP = 1;
    private static final int SLOT_RIGHT = 2;
    private static final int SLOT_TOP_RIGHT = 3;

    private static final Utensil[] INITIAL_ORDER = {
            Utensil.SPOON, Utensil.WINE_GLASS, Utensil.FORK, Utensil.KNIFE
    };
    private static final Utensil[] ANSWER_ORDER = {
            Utensil.KNIFE, Utensil.FORK, Utensil.SPOON, Utensil.WINE_GLASS
    };

    private static final String STYLE_NORMAL =
            "-fx-background-color: rgba(255, 255, 255, 0.04);"
            + "-fx-border-color: rgba(240, 224, 176, 0.28);"
            + "-fx-border-width: 1.5;"
            + "-fx-border-style: segments(6, 4);"
            + "-fx-cursor: hand;";
    private static final String STYLE_HOVER =
            "-fx-background-color: rgba(240, 224, 176, 0.10);"
            + "-fx-border-color: rgba(240, 224, 176, 0.65);"
            + "-fx-border-width: 1.5;"
            + "-fx-border-style: segments(6, 4);"
            + "-fx-cursor: hand;";
    private static final String STYLE_SELECTED =
            "-fx-background-color: rgba(240, 192, 96, 0.16);"
            + "-fx-border-color: #f0c060;"
            + "-fx-border-width: 2.5;"
            + "-fx-cursor: hand;";

    @FXML private ImageView background;
    @FXML private StackPane leftSlot;
    @FXML private StackPane topSlot;
    @FXML private StackPane rightSlot;
    @FXML private StackPane topRightSlot;
    @FXML private Label statusLabel;
    @FXML private Button viewPaintingButton;
    @FXML private Button resetButton;
    @FXML private Button checkButton;
    @FXML private VBox paintingOverlay;
    @FXML private ImageView completePaintingView;

    private StackPane[] slotPanes;
    private final Utensil[] placed = new Utensil[4];
    private int selectedSlot = -1;
    private boolean solved;
    private PauseTransition solveWait;
    private Runnable onSolved = () -> { };

    @FXML
    private void initialize() {
        Images.apply(background, "dining_table_closeup.png");

        slotPanes = new StackPane[] {leftSlot, topSlot, rightSlot, topRightSlot};
        for (int i = 0; i < slotPanes.length; i++) {
            final int idx = i;
            StackPane slot = slotPanes[i];
            slot.setPickOnBounds(true);
            slot.setOnMouseClicked(e -> onSlotClick(idx));
            slot.setOnMouseEntered(e -> {
                if (!solved && selectedSlot != idx) {
                    slot.setStyle(STYLE_HOVER);
                }
            });
            slot.setOnMouseExited(e -> {
                if (selectedSlot != idx) {
                    slot.setStyle(STYLE_NORMAL);
                }
            });
        }

        boolean restored = GameState.get().isPaintingRestored();
        viewPaintingButton.setVisible(restored);
        viewPaintingButton.setManaged(restored);
        if (restored) {
            Image paintingImg = Images.load("dining_painting_complete.png");
            if (paintingImg != null) {
                completePaintingView.setImage(paintingImg);
            }
        }

        resetArrangement();
    }

    @Override
    public void setOnSolved(Runnable onSolved) {
        this.onSolved = onSolved;
    }

    @Override
    public void onClose() {
        if (solveWait != null) {
            solveWait.stop();
            solveWait = null;
        }
    }

    private void onSlotClick(int idx) {
        if (solved || paintingOverlay.isVisible()) {
            return;
        }
        if (selectedSlot == -1) {
            selectedSlot = idx;
            statusLabel.setText("[" + placed[idx].label + "] 선택됨 — 바꿀 위치의 식기를 클릭하세요.");
        } else if (selectedSlot == idx) {
            selectedSlot = -1;
            statusLabel.setText("");
        } else {
            Utensil temp = placed[selectedSlot];
            placed[selectedSlot] = placed[idx];
            placed[idx] = temp;
            selectedSlot = -1;
            statusLabel.setText("");
        }
        renderSlots();
    }

    @FXML
    private void onReset() {
        if (solved) {
            return;
        }
        resetArrangement();
        statusLabel.setText("식기 배치를 처음 상태로 되돌렸다.");
    }

    @FXML
    private void onCheck() {
        if (solved) {
            return;
        }
        if (!isAnswerMatch()) {
            statusLabel.setText("아무런 반응이 없다. 식기 배열이 잘못된 것 같다.");
            return;
        }
        if (!GameState.get().isPaintingRestored()) {
            statusLabel.setText("식탁의 장치가 잠겨 있다. 사라진 그림부터 확인해야 할 것 같다.");
            return;
        }
        solved = true;
        selectedSlot = -1;
        renderSlots();
        viewPaintingButton.setDisable(true);
        resetButton.setDisable(true);
        checkButton.setDisable(true);
        statusLabel.setText("식기들이 제자리에 맞춰졌다! 식탁 아래 장치가 열린다...");

        solveWait = new PauseTransition(Duration.seconds(0.65));
        solveWait.setOnFinished(e -> {
            solveWait = null;
            onSolved.run();
        });
        solveWait.play();
    }

    @FXML
    private void onShowPainting() {
        if (GameState.get().isPaintingRestored()) {
            paintingOverlay.setVisible(true);
        }
    }

    @FXML
    private void onClosePainting() {
        paintingOverlay.setVisible(false);
    }

    private void resetArrangement() {
        System.arraycopy(INITIAL_ORDER, 0, placed, 0, placed.length);
        selectedSlot = -1;
        renderSlots();
    }

    private boolean isAnswerMatch() {
        for (int i = 0; i < ANSWER_ORDER.length; i++) {
            if (placed[i] != ANSWER_ORDER[i]) {
                return false;
            }
        }
        return true;
    }

    private void renderSlots() {
        for (int i = 0; i < slotPanes.length; i++) {
            StackPane slot = slotPanes[i];
            Utensil utensil = placed[i];
            slot.getChildren().setAll(createUtensilNode(utensil, i));

            boolean isSelected = (selectedSlot == i);
            slot.setStyle(isSelected ? STYLE_SELECTED : STYLE_NORMAL);
            slot.setEffect(isSelected ? new DropShadow(10, Color.web("#f0c060")) : null);
        }
    }

    private javafx.scene.Node createUtensilNode(Utensil utensil, int slotIndex) {
        Image img = Images.load(utensil.imageFile);
        if (img == null) {
            Label fallback = new Label(utensil.label);
            fallback.getStyleClass().add("puzzle-text");
            fallback.setMouseTransparent(true);
            return fallback;
        }

        ImageView view = new ImageView(img);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setMouseTransparent(true);

        if (utensil == Utensil.WINE_GLASS) {
            double fitSize = slotIndex == SLOT_TOP ? 120 : (slotIndex == SLOT_TOP_RIGHT ? 150 : 135);
            view.setFitWidth(fitSize);
            view.setFitHeight(fitSize);
            view.setRotate(0);
        } else {
            double fitHeight = slotIndex == SLOT_TOP_RIGHT ? 122 : 205;
            view.setFitWidth(140);
            view.setFitHeight(fitHeight);
            view.setRotate(slotIndex == SLOT_TOP ? -90 : 0);
        }

        return new Group(view);
    }
}

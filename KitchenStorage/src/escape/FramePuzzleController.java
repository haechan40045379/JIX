package escape;

import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/**
 * 현관 홀 퍼즐: 거꾸로 걸린 액자 바로잡기.
 * - 원본 이미지(entrance_frame.png)는 정상 방향이므로 초기 각도를 180°로 설정한다.
 * - 좌/우 회전 버튼으로 90°씩 부드럽게 회전시키며, 회전 중에는 중복 입력을 막는다.
 * - 최종 각도가 0°(360°와 동등)에 도달하면 클리어 처리한다.
 */
public class FramePuzzleController implements PuzzleController {

    private static final double ROTATE_MILLIS = 220;

    @FXML private Label infoLabel;
    @FXML private ImageView frameImage;
    @FXML private Label fallbackLabel;
    @FXML private Button leftButton;
    @FXML private Button rightButton;

    private Runnable onSolved = () -> { };
    private RotateTransition activeTransition;
    private PauseTransition solveWait;
    private int angle = 180;
    private boolean rotating;
    private boolean solved;

    @FXML
    private void initialize() {
        Image img = Images.load("entrance_frame.png");
        if (img != null) {
            frameImage.setImage(img);
            frameImage.setRotate(angle);
        } else {
            fallbackLabel.setVisible(true);
            fallbackLabel.setRotate(angle);
        }
    }

    @Override
    public void setOnSolved(Runnable onSolved) {
        this.onSolved = onSolved;
    }

    @Override
    public void onClose() {
        if (activeTransition != null) {
            activeTransition.stop();
            activeTransition = null;
        }
        if (solveWait != null) {
            solveWait.stop();
            solveWait = null;
        }
        rotating = false;
    }

    @FXML
    private void onRotateLeft() {
        rotateBy(-90);
    }

    @FXML
    private void onRotateRight() {
        rotateBy(90);
    }

    private void rotateBy(int delta) {
        if (rotating || solved) {
            return;
        }
        rotating = true;
        setButtonsDisabled(true);

        javafx.scene.Node target = frameImage.getImage() != null ? frameImage : fallbackLabel;
        int nextAngle = Math.floorMod(angle + delta, 360);

        RotateTransition transition = new RotateTransition(Duration.millis(ROTATE_MILLIS), target);
        transition.setByAngle(delta);
        transition.setOnFinished(e -> {
            activeTransition = null;
            angle = nextAngle;
            target.setRotate(angle);
            rotating = false;
            if (angle == 0) {
                onFrameAligned();
            } else {
                setButtonsDisabled(false);
            }
        });
        activeTransition = transition;
        transition.play();
    }

    private void onFrameAligned() {
        solved = true;
        setButtonsDisabled(true);
        infoLabel.setText("액자가 정상 방향으로 돌아왔다! 뒤쪽에서 무언가 떨어진다...");
        solveWait = new PauseTransition(Duration.seconds(0.65));
        solveWait.setOnFinished(e -> {
            solveWait = null;
            onSolved.run();
        });
        solveWait.play();
    }

    private void setButtonsDisabled(boolean disabled) {
        leftButton.setDisable(disabled);
        rightButton.setDisable(disabled);
    }
}

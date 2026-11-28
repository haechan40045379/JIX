package escape;

import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SafePuzzleController implements PuzzleController {

    @FXML private ImageView safeImage;
    @FXML private Button firstDial;
    @FXML private Button secondDial;
    @FXML private Button thirdDial;
    @FXML private Button fourthDial;
    @FXML private Label result;

    private List<Button> dials;
    private Runnable onSolved;

    @FXML
    private void initialize() {
        Image image = Images.load("safe_closeup.png");
        if (image == null || image.isError()) {
            result.setText("금고 확대 이미지를 불러올 수 없다.");
        } else {
            safeImage.setImage(image);
        }
        dials = List.of(firstDial, secondDial, thirdDial, fourthDial);
    }

    @FXML
    private void onTurnDial(ActionEvent event) {
        Button dial = (Button) event.getSource();
        int next = (Integer.parseInt(dial.getText()) + 1) % 10;
        dial.setText(Integer.toString(next));
        dial.setAccessibleText(dial.getTooltip().getText() + ", 현재 " + next);
        if ("비밀번호가 틀렸다.".equals(result.getText())) {
            result.setText("");
        }
    }

    @FXML
    private void onTryHandle() {
        if (onSolved == null) {
            return;
        }
        StringBuilder code = new StringBuilder(dials.size());
        dials.forEach(dial -> code.append(dial.getText()));
        if (StudyController.SAFE_CODE.contentEquals(code)) {
            Runnable solve = onSolved;
            onSolved = null;
            solve.run();
        } else {
            result.setText("비밀번호가 틀렸다.");
            GameState.get().penalize(GameState.PENALTY_BIG, "금고 비밀번호 오답");
        }
    }

    @Override
    public void setOnSolved(Runnable onSolved) {
        this.onSolved = onSolved;
    }

    @Override
    public void onClose() {
        onSolved = null;
    }
}

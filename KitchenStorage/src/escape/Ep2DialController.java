package escape;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/** [A] 등대 철문의 4자리 숫자 다이얼. 정답은 Episode2.DIAL_CODE (0113). */
public class Ep2DialController extends Ep2Puzzle {

    private static final double W = 720;
    private static final double H = 405;
    /** 숫자 칸 왼쪽 x (이미지 720x405 기준). */
    private static final double[] SLOT_X = {220, 298.5, 377, 455};

    private final List<Button> dials = new ArrayList<>();
    private Label result;

    @Override
    protected void build() {
        Pane board = new Pane();
        board.setPrefSize(W, H);
        board.setMinSize(W, H);
        board.setMaxSize(W, H);
        board.getChildren().add(picture("ep2_dial_lock_closeup.png", W, H));
        for (int i = 0; i < SLOT_X.length; i++) {
            Button dial = new Button("0");
            dial.getStyleClass().addAll("safe-dial", "ep2-dial");
            dial.relocate(SLOT_X[i], 204);
            dial.setPrefSize(51, 38);
            dial.setMinSize(51, 38);
            dial.setAccessibleText("숫자 칸 " + (i + 1));
            dial.setOnAction(e -> {
                dial.setText(Integer.toString((Integer.parseInt(dial.getText()) + 1) % 10));
                SoundManager.sfx(SoundManager.first("sfx_dial", "sfx_click"), 0.5);
                result.setText("");
            });
            dials.add(dial);
            board.getChildren().add(dial);
        }
        result = text("");
        result.setMinHeight(20);
        root.getChildren().addAll(board,
                text("숫자 칸을 눌러 번호를 맞춘 뒤 손잡이를 당기자."),
                button("손잡이 당기기", this::tryOpen), result);
    }

    private void tryOpen() {
        StringBuilder code = new StringBuilder();
        dials.forEach(d -> code.append(d.getText()));
        if (Episode2.DIAL_CODE.contentEquals(code)) {
            solve();
        } else {
            result.setText("덜컹. 손잡이가 걸려 움직이지 않는다.");
            state.penalize(GameState.PENALTY_BIG, "철문 다이얼 오답");
        }
    }
}

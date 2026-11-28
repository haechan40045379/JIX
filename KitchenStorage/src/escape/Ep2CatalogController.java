package escape;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

/**
 * [C] 참가자 서랍장 (13칸, 4-4-4-1).
 * 서랍 13은 언제든 열 수 있다 (내 파일 + 실패한 참가자 7·9·12).
 * 나머지는 7 → 9 → 12 → 1 순서로 눌러야 한다. 틀리면 시간 차감 후 처음부터.
 */
public class Ep2CatalogController extends Ep2Puzzle {

    private static final double W = 720;
    private static final double H = 405;
    private static final double[][] COLS = {{166.5, 260}, {268, 357.5}, {365.5, 455}, {463, 556.5}};
    private static final double[][] ROWS = {{43.5, 111}, {120, 185}, {193.5, 260}};

    private final List<Button> pressed = new ArrayList<>();
    private int progress;
    private Label result;
    private boolean done;

    @Override
    protected void build() {
        Pane board = new Pane();
        board.setPrefSize(W, H);
        board.setMinSize(W, H);
        board.setMaxSize(W, H);
        board.getChildren().add(picture("ep2_catalog_closeup.png", W, H));
        for (int r = 0; r < ROWS.length; r++) {
            for (int c = 0; c < COLS.length; c++) {
                int number = r * 4 + c + 1;
                addDrawer(board, number, COLS[c][0], ROWS[r][0], COLS[c][1] - COLS[c][0], ROWS[r][1] - ROWS[r][0]);
            }
        }
        addDrawer(board, 13, 166.5, 270, 390, 73.5);

        result = text("");
        result.setMinHeight(22);
        result.setMaxWidth(700);
        root.getChildren().addAll(board,
                text("각 서랍의 명패에 참가자 번호가 적혀 있다. 잠긴 서랍은 정해진 순서로만 열린다."), result);
    }

    private void addDrawer(Pane board, int number, double x, double y, double w, double h) {
        Button drawer = new Button();
        drawer.getStyleClass().add("drawer");
        drawer.relocate(x, y);
        drawer.setPrefSize(w, h);
        drawer.setMinSize(w, h);
        drawer.setAccessibleText("서랍 " + number);
        drawer.setOnAction(e -> onDrawer(number, drawer));

        // 명패 번호 (명패는 서랍 위쪽 가운데)
        Label plate = new Label(Integer.toString(number));
        plate.getStyleClass().add("drawer-number");
        plate.setMouseTransparent(true);
        plate.setPrefWidth(w);
        plate.setAlignment(javafx.geometry.Pos.CENTER);
        plate.relocate(x, y + (number == 13 ? 13 : 8));
        board.getChildren().addAll(drawer, plate);
    }

    private void onDrawer(int number, Button drawer) {
        if (done) {
            return;
        }
        SoundManager.sfx(SoundManager.first("sfx_drawer", "sfx_click"), 0.7);
        if (number == 13) {
            boolean known = state.hasClue(Episode2.DRAWER13_NOTE);
            state.addClue(Episode2.DRAWER13_NOTE);
            result.setText(Episode2.DRAWER13_NOTE + (known ? "" : "\n(단서 기록에 추가됨)"));
            return;
        }
        if (number == Episode2.DRAWER_ORDER[progress]) {
            progress++;
            drawer.getStyleClass().add("drawer-open");
            drawer.setDisable(true);
            pressed.add(drawer);
            if (progress == Episode2.DRAWER_ORDER.length) {
                done = true;
                result.setText("마지막 서랍이 덜컥 열린다!");
                PauseTransition wait = new PauseTransition(Duration.millis(600));
                wait.setOnFinished(e -> solve());
                wait.play();
            } else {
                result.setText("서랍 " + number + "이(가) 조금 빠져나온 채 걸렸다. (" + progress + "/4)");
            }
            return;
        }
        for (Button b : pressed) {
            b.getStyleClass().remove("drawer-open");
            b.setDisable(false);
        }
        pressed.clear();
        progress = 0;
        result.setText("서랍 " + number + "을(를) 당기자 쿵- 열려 있던 서랍들이 모두 도로 닫혔다.");
        state.penalize(GameState.PENALTY_SMALL, "서랍 순서 오답");
    }
}

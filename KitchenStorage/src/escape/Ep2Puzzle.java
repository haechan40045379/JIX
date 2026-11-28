package escape;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/** 에피소드 2 퍼즐 화면 공통 부분. 내용은 각 퍼즐이 build() 에서 코드로 그린다. */
abstract class Ep2Puzzle implements PuzzleController {

    @FXML protected VBox root;

    protected final GameState state = GameState.get();
    private Runnable onSolved;
    protected boolean closed;

    @FXML
    private void initialize() {
        build();
    }

    protected abstract void build();

    @Override
    public void setOnSolved(Runnable onSolved) {
        this.onSolved = onSolved;
    }

    /** 한 번만 클리어 콜백을 부른다. */
    protected void solve() {
        Runnable r = onSolved;
        onSolved = null;
        if (r != null && !closed) {
            r.run();
        }
    }

    @Override
    public void onClose() {
        closed = true;
        onSolved = null;
        stop();
    }

    /** 애니메이션 정리. */
    protected void stop() {
    }

    protected static Label text(String s) {
        Label l = new Label(s);
        l.getStyleClass().add("puzzle-text");
        l.setWrapText(true);
        l.setMaxWidth(640);
        return l;
    }

    protected static Button button(String s, Runnable action) {
        Button b = new Button(s);
        b.getStyleClass().add("clue-button");
        b.setOnAction(e -> action.run());
        return b;
    }

    protected static ImageView picture(String file, double w, double h) {
        ImageView v = new ImageView(Images.load(file));
        v.setFitWidth(w);
        v.setFitHeight(h);
        v.setPreserveRatio(false);
        v.setMouseTransparent(true);
        return v;
    }
}

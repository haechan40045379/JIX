package escape;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class StudyController implements RoomController {

    static final String SAFE_CODE = "4961";
    static final String TAROT_BOOK = """
            메이저 아르카나

            죽음
            끝과 새로운 시작, 변화와 전환

            광대
            새로운 출발, 순수함과 가능성

            은둔자
            고독, 성찰, 지혜를 찾는 여정

            마법사
            지식과 능력의 활용, 가능성을 현실로 만드는 힘

            세계
            완성, 성취, 여정의 끝""";

    @FXML private ImageView background;

    private final GameState state = GameState.get();
    private GameHost host;

    @Override
    public void setHost(GameHost host) {
        this.host = host;
        Images.apply(background, "study.png");
    }

    @FXML
    private void onBookshelf() {
        state.addClue(TAROT_BOOK);
        Label book = new Label(TAROT_BOOK);
        book.getStyleClass().add("note-paper");
        book.setWrapText(true);
        host.showOverlay("메이저 아르카나 설명서", book);
        host.showMessage("메이저 아르카나에 대한 설명이 적힌 책을 발견했다.");
    }

    @FXML
    private void onSafe() {
        if (state.isCleared(GameState.PUZZLE_SAFE)) {
            host.showMessage("금고는 이미 열려 있다.");
            return;
        }
        host.openPuzzle("금고", "SafePuzzleView.fxml", () -> {
            state.clearSafePuzzle();
            host.closeOverlay();
            host.showMessage("철컥! 금고가 열렸다. 탈출 열쇠 조각 3을 획득했다.");
        });
    }

    @FXML
    private void goToBedroom() {
        host.goTo(GameHost.BEDROOM);
        host.showMessage("서재에서 침실로 돌아왔다.");
    }
}

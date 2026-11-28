package escape;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.shape.Rectangle;
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

    static final String GLOW_NOTE =
            "불을 끄자 벽에 야광 글씨가 떠오른다. '금고 번호는 그림이 알려준다. 그들은 우리를 보고 있다. - 참가자 9'";

    @FXML private ImageView background;
    @FXML private Rectangle darkness;
    @FXML private Button glowButton;
    @FXML private Button lampButton;

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

    /** 스탠드 켜기/끄기: 끄면 방이 어두워지고 벽의 야광 글씨가 보인다. */
    @FXML
    private void onLamp() {
        boolean dark = !darkness.isVisible();
        darkness.setVisible(dark);
        glowButton.setVisible(dark);
        lampButton.setText(dark ? "스탠드 (꺼짐)" : "스탠드");
        host.showMessage(dark ? "딸깍. 스탠드를 끄자 서재가 어둠에 잠긴다. 벽 한쪽이 희미하게 빛난다."
                : "딸깍. 스탠드를 다시 켰다.");
    }

    @FXML
    private void onGlow() {
        Story.flavor(host, GLOW_NOTE);
    }

    /** 분위기 / 스토리 오브젝트 (userData). */
    @FXML
    private void onFlavor(ActionEvent event) {
        Story.flavor(host, ((javafx.scene.Node) event.getSource()).getUserData());
    }

    @FXML
    private void goToBedroom() {
        host.goTo(GameHost.BEDROOM);
        host.showMessage("서재에서 침실로 돌아왔다.");
    }
}

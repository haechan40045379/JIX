package escape;

import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class BedroomController implements RoomController {

    private enum Card {
        FOOL("광대", "tarot_fool.png"),
        MAGICIAN("마법사", "tarot_magician.png"),
        HERMIT("은둔자", "tarot_hermit.png"),
        DEATH("죽음", "tarot_death.png"),
        WORLD("세계", "tarot_world.png");

        private final String name;
        private final String imageFile;

        Card(String name, String imageFile) {
            this.name = name;
            this.imageFile = imageFile;
        }
    }

    private static final List<Card> CORRECT_ORDER =
            List.of(Card.DEATH, Card.FOOL, Card.HERMIT, Card.MAGICIAN, Card.WORLD);
    private static final String ORDER_NOTE = """
            모든 것을 잃고 다시 눈을 뜬 자여,
            고독 속에서 지혜를 갈구하고,
            미지를 기지로 바꾸어
            여정의 끝에 도달하라.""";
    private static final String PASSWORD_NOTE = "서재 금고의 비밀번호\n\n" + StudyController.SAFE_CODE;

    @FXML private ImageView background;

    private final GameState state = GameState.get();
    private GameHost host;

    @Override
    public void setHost(GameHost host) {
        this.host = host;
        Images.apply(background, "bedroom.png");
    }

    @FXML
    private void onTable() {
        boolean read = state.hasClue(ORDER_NOTE);
        state.addClue(ORDER_NOTE);
        host.showMessage(read ? "탁자 위에는 이미 읽었던 쪽지가 놓여 있다."
                : "영문 모를 내용이 적혀 있다. 어떤 순서인가?");
        showNote("순서에 관한 쪽지", ORDER_NOTE);
    }

    @FXML
    private void onPainting() {
        if (state.isCleared(GameState.PUZZLE_TAROT)) {
            showNote("비밀번호 쪽지", PASSWORD_NOTE);
            return;
        }
        boolean noteRead = state.hasClue(ORDER_NOTE);
        boolean bookRead = state.hasClue(StudyController.TAROT_BOOK);
        if (noteRead && bookRead) {
            openTarotPuzzle();
        } else {
            String hint = noteRead ? "쪽지의 의미를 이해하려면 이 그림들에 관한 지식이 필요할 것 같다."
                    : bookRead ? "그림의 의미는 알겠지만, 어떤 순서로 선택해야 하는지 아직 모르겠다."
                    : "다섯 그림에는 어떤 의미가 숨겨져 있는 것 같다. 주변을 더 조사해 보자.";
            Label description = puzzleText(hint);
            HBox cardRow = cardRow();
            for (Card card : Card.values()) {
                Button button = cardButton(card, new Label());
                button.setOnAction(event -> description.setText(card.name + " 그림을 살펴보았다. " + hint));
                cardRow.getChildren().add(button);
            }
            host.showMessage(hint);
            VBox content = new VBox(12, description, cardRow);
            content.setAlignment(Pos.CENTER);
            host.showOverlay("벽에 걸린 다섯 장의 그림", content);
        }
    }

    private void openTarotPuzzle() {
        Label instructions = puzzleText("쪽지에 적힌 글귀를 따라 다섯 그림을 올바른 순서로 선택하라.");
        Label progress = puzzleText("선택한 그림: 0/5");
        Label result = puzzleText("");
        HBox cardRow = cardRow();
        List<Button> selectedButtons = new ArrayList<>();
        List<Label> badges = new ArrayList<>();
        Runnable reset = () -> {
            selectedButtons.forEach(button -> button.setDisable(false));
            selectedButtons.clear();
            badges.forEach(badge -> badge.setVisible(false));
            progress.setText("선택한 그림: 0/5");
        };

        Button retry = new Button("다시 선택하기");
        retry.getStyleClass().add("clue-button");
        retry.setOnAction(event -> {
            reset.run();
            result.setText("");
        });
        VBox puzzle = new VBox(12, instructions, cardRow, progress, result, retry);
        puzzle.setAlignment(Pos.CENTER);
        for (Card card : Card.values()) {
            Label badge = new Label();
            badges.add(badge);
            Button button = cardButton(card, badge);
            cardRow.getChildren().add(button);
            button.setOnAction(event -> {
                int index = selectedButtons.size();
                if (index >= CORRECT_ORDER.size() || button.isDisabled()) {
                    return;
                }
                if (CORRECT_ORDER.get(index) != card) {
                    reset.run();
                    result.setText("순서가 틀렸다. 처음부터 다시 선택해야 한다.");
                    state.penalize(GameState.PENALTY_SMALL, "타로 그림 순서 오답");
                    return;
                }
                selectedButtons.add(button);
                button.setDisable(true);
                badge.setText(Integer.toString(selectedButtons.size()));
                badge.setVisible(true);
                progress.setText("선택한 그림: " + selectedButtons.size() + "/5");
                result.setText("");
                if (selectedButtons.size() == CORRECT_ORDER.size()) {
                    state.markCleared(GameState.PUZZLE_TAROT);
                    state.addClue(PASSWORD_NOTE);
                    host.showMessage("다섯 그림의 순서를 맞추자 세계 그림의 액자 아래에서 쪽지가 떨어졌다.");
                    showNote("비밀번호 쪽지", PASSWORD_NOTE);
                }
            });
        }
        host.showOverlay("타로 그림 순서 맞추기", puzzle);
    }

    private static HBox cardRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER);
        row.setPrefWidth(750);
        return row;
    }

    private static Button cardButton(Card card, Label badge) {
        Image image = Images.load(card.imageFile);
        ImageView view = new ImageView(image);
        view.setFitWidth(130);
        view.setFitHeight(215);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        StackPane artwork = new StackPane();
        artwork.setPrefSize(130, 215);
        artwork.getChildren().add(image == null || image.isError() ? puzzleText("이미지 없음") : view);
        badge.getStyleClass().add("tarot-rank");
        badge.setVisible(false);
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        artwork.getChildren().add(badge);

        Button button = new Button(card.name, artwork);
        button.setContentDisplay(ContentDisplay.TOP);
        button.getStyleClass().add("tarot-card");
        button.setTooltip(new Tooltip(card.name));
        return button;
    }

    private void showNote(String title, String text) {
        Label note = new Label(text);
        note.getStyleClass().add("note-paper");
        note.setWrapText(true);
        host.showOverlay(title, note);
    }

    private static Label puzzleText(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("puzzle-text");
        return label;
    }

    /** 분위기 / 스토리 오브젝트 (userData). */
    @FXML
    private void onFlavor(ActionEvent event) {
        Story.flavor(host, ((javafx.scene.Node) event.getSource()).getUserData());
    }

    @FXML
    private void goToEntrance() {
        host.goTo(GameHost.ENTRANCE);
        host.showMessage("계단을 내려와 현관 홀로 돌아왔다.");
    }

    @FXML
    private void goToStudy() {
        host.goTo(GameHost.STUDY);
        host.showMessage("침실 오른쪽 문을 지나 서재에 들어섰다.");
    }
}

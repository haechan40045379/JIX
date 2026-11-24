package escape;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;

/** 메인 화면 컨트롤러: 방 전환, 오버레이, HUD(인벤토리/열쇠 조각) 관리. */
public class MainController implements GameHost {

    @FXML private StackPane roomContainer;
    @FXML private StackPane overlayLayer;
    @FXML private StackPane overlayContent;
    @FXML private Label overlayTitle;
    @FXML private Label roomLabel;
    @FXML private HBox inventoryBox;
    @FXML private Label fragmentLabel;
    @FXML private Label messageLabel;
    @FXML private Button clueButton;
    @FXML private Button fullScreenButton;

    private static final int MAX_LOG = 60;
    /** 하단 메시지 기록 (오래된 것이 앞). */
    private final List<String> messageLog = new ArrayList<>();
    /** 단서 기록 창을 마지막으로 열었을 때의 단서 개수 (새 단서 표시용). */
    private int seenClueCount;

    private final GameState state = GameState.get();
    private final Map<String, Parent> rooms = new HashMap<>();
    private final Map<String, String> roomNames = new HashMap<>();
    private PuzzleController activePuzzle;

    @FXML
    private void initialize() {
        rooms.put(KITCHEN, loadRoom("KitchenView.fxml"));
        rooms.put(STORAGE, loadRoom("StorageView.fxml"));
        roomNames.put(KITCHEN, "주방");
        roomNames.put(STORAGE, "지하 저장고");

        inventoryBox.getChildren().setAll(new InventoryBar(state, this));
        fullScreenButton.setTooltip(new Tooltip("전체화면 (F11)"));
        watchFullScreen();
        state.addListener(this::refreshHud);
        refreshHud();
        goTo(KITCHEN);
        showMessage("어두운 주방이다. 낡은 냉장고가 눈에 띈다.");
    }

    private Parent loadRoom(String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(MainController.class.getResource(fxmlName));
            Parent root = loader.load();
            RoomController controller = loader.getController();
            controller.setHost(this);
            return root;
        } catch (IOException e) {
            throw new UncheckedIOException("FXML 로드 실패: " + fxmlName, e);
        }
    }

    private void refreshHud() {
        fragmentLabel.setText("열쇠 조각 " + state.getKeyFragments() + "/" + GameState.TOTAL_KEY_FRAGMENTS);
        int clueCount = state.getClues().size();
        boolean hasNew = clueCount > seenClueCount;
        clueButton.setText("단서 기록 (" + clueCount + ")" + (hasNew ? " ●" : ""));
        clueButton.getStyleClass().remove("clue-new");
        if (hasNew) {
            clueButton.getStyleClass().add("clue-new");
        }
    }

    // ================= 단서 기록 창 =================
    @FXML
    private void onShowClues() {
        seenClueCount = state.getClues().size();
        refreshHud();

        // 탭 1: 단서
        VBox clueList = new VBox(10);
        clueList.setPadding(new Insets(10));
        List<String> clues = state.getClues();
        if (clues.isEmpty()) {
            clueList.getChildren().add(logText("아직 찾은 단서가 없다. 방을 구석구석 살펴보자."));
        }
        for (int i = 0; i < clues.size(); i++) {
            Label item = new Label((i + 1) + ".  " + clues.get(i));
            item.getStyleClass().add("clue-item");
            item.setWrapText(true);
            item.setMaxWidth(Double.MAX_VALUE);
            clueList.getChildren().add(item);
        }

        // 탭 2: 최근 메시지 기록 (최신이 위)
        VBox historyList = new VBox(6);
        historyList.setPadding(new Insets(10));
        if (messageLog.isEmpty()) {
            historyList.getChildren().add(logText("기록이 없다."));
        }
        for (int i = messageLog.size() - 1; i >= 0; i--) {
            Label line = logText("· " + messageLog.get(i));
            historyList.getChildren().add(line);
        }

        TabPane tabs = new TabPane(
                new Tab("단서 (" + clues.size() + ")", scroll(clueList)),
                new Tab("대화 기록 (" + messageLog.size() + ")", scroll(historyList)));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setPrefSize(580, 380);
        tabs.getStyleClass().add("log-tabs");
        showOverlay("단서 기록", tabs);
    }

    private static Label logText(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("puzzle-text");
        l.setWrapText(true);
        l.setMaxWidth(540);
        return l;
    }

    private static ScrollPane scroll(VBox content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("log-scroll");
        return sp;
    }

    // ================= GameHost =================
    @Override
    public void goTo(String roomId) {
        Parent room = rooms.get(roomId);
        if (room == null) {
            showMessage("아직 연결되지 않은 방입니다: " + roomId);
            return;
        }
        closeOverlay();
        roomContainer.getChildren().setAll(room);
        roomLabel.setText("현재 위치: " + roomNames.get(roomId));
    }

    @Override
    public void showOverlay(String title, Node content) {
        closeOverlay();
        overlayTitle.setText(title);
        overlayContent.getChildren().setAll(content);
        overlayLayer.setVisible(true);
    }

    @Override
    public void openPuzzle(String title, String fxmlName, Runnable onSolved) {
        try {
            FXMLLoader loader = new FXMLLoader(MainController.class.getResource(fxmlName));
            Parent puzzleRoot = loader.load();
            PuzzleController controller = loader.getController();
            controller.setOnSolved(onSolved);
            showOverlay(title, puzzleRoot);
            activePuzzle = controller;
        } catch (IOException e) {
            throw new UncheckedIOException("퍼즐 FXML 로드 실패: " + fxmlName, e);
        }
    }

    @Override
    public void closeOverlay() {
        if (activePuzzle != null) {
            activePuzzle.onClose();
            activePuzzle = null;
        }
        overlayContent.getChildren().clear();
        overlayLayer.setVisible(false);
    }

    @Override
    public void showMessage(String message) {
        messageLabel.setText(message);
        // 같은 문장 연속 중복은 한 번만 기록, 최대 MAX_LOG 개 유지
        if (messageLog.isEmpty() || !messageLog.get(messageLog.size() - 1).equals(message)) {
            messageLog.add(message);
            if (messageLog.size() > MAX_LOG) {
                messageLog.remove(0);
            }
        }
    }

    @FXML
    private void onCloseOverlay() {
        closeOverlay();
    }

    /** HUD 버튼: 전체화면 ↔ 창 모드 (키보드 F11 / Alt+Enter 와 같음). */
    @FXML
    private void onToggleFullScreen() {
        if (roomLabel.getScene() != null && roomLabel.getScene().getWindow() instanceof Stage) {
            Stage stage = (Stage) roomLabel.getScene().getWindow();
            Main.toggleFullScreen(stage);
        }
    }

    /** 전체화면 상태가 바뀌면(ESC/F11 포함) 버튼 툴팁 갱신. Scene이 붙은 뒤 한 번만 등록. */
    private void watchFullScreen() {
        roomLabel.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene == null) {
                return;
            }
            scene.windowProperty().addListener((o2, oldWin, win) -> {
                if (win instanceof Stage) {
                    ((Stage) win).fullScreenProperty().addListener((o3, was, full) ->
                            fullScreenButton.setTooltip(new Tooltip(full ? "창 모드로 (F11 / ESC)" : "전체화면 (F11)")));
                }
            });
        });
    }
}

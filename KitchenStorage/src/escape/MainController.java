package escape;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/** 메인 화면 컨트롤러: 방 전환, 오버레이, HUD(인벤토리/열쇠 조각) 관리. */
public class MainController implements GameHost {

    @FXML private BorderPane gameLayer;
    @FXML private StackPane clearLayer;
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
    @FXML private Label timerLabel;
    @FXML private ClearController clearViewController;
    @FXML private Button hintButton;
    @FXML private Button mapButton;
    @FXML private Label toastLabel;
    @FXML private Button menuButton;

    /** 일시정지 메뉴가 열려 있는 동안 타이머를 멈춘다. */
    private boolean paused;
    private static final String PAUSE_TITLE = "일시 정지";

    /** 자동 저장 주기(초). */
    private static final int AUTOSAVE_SECONDS = 20;
    private int ticksSinceSave;

    /** 1초마다 남은 시간을 줄이는 타이머. */
    private Timeline timer;

    private static final int MAX_LOG = 60;
    /** 하단 메시지 기록 (오래된 것이 앞). */
    private final List<String> messageLog = new ArrayList<>();
    /** 단서 기록 창을 마지막으로 열었을 때의 단서 개수 (새 단서 표시용). */
    private int seenClueCount;

    private final GameState state = GameState.get();
    private final Map<String, Parent> rooms = new HashMap<>();
    private final Map<String, String> roomNames = new HashMap<>();
    private PuzzleController activePuzzle;

    // ---- 효과음: 상태 변화 비교용 직전 값 ----
    private final java.util.Set<String> lastInventory = new java.util.HashSet<>();
    private final java.util.Set<String> lastCleared = new java.util.HashSet<>();
    private int lastClueCount;
    private int lastClearedCount;
    private boolean lastFridgeUnlocked;
    private boolean lastSafeOpened;
    private final java.util.Set<String> lastFlags = new java.util.HashSet<>();
    private boolean timeWarned;
    private final boolean ep2 = state.getEpisode() == 2;

    @FXML
    private void initialize() {
        if (ep2) {
            rooms.put(Episode2.PIER, loadRoom("Ep2PierView.fxml"));
            rooms.put(Episode2.ARCHIVE, loadRoom("Ep2ArchiveView.fxml"));
            rooms.put(Episode2.OBSERVATION, loadRoom("Ep2ObservationView.fxml"));
            rooms.put(Episode2.ENGINE, loadRoom("Ep2EngineView.fxml"));
            rooms.put(Episode2.LANTERN, loadRoom("Ep2LanternView.fxml"));
            for (String r : Episode2.ROOMS) {
                roomNames.put(r, Episode2.roomName(r));
            }
        } else {
            loadEpisode1Rooms();
        }
        initHud();
    }

    private void loadEpisode1Rooms() {
        rooms.put(KITCHEN, loadRoom("KitchenView.fxml"));
        rooms.put(STORAGE, loadRoom("StorageView.fxml"));
        rooms.put(DINING, loadRoom("DiningView.fxml"));
        rooms.put(ENTRANCE, loadRoom("EntranceView.fxml"));
        rooms.put(BEDROOM, loadRoom("BedroomView.fxml"));
        rooms.put(STUDY, loadRoom("StudyView.fxml"));
        roomNames.put(KITCHEN, "주방");
        roomNames.put(STORAGE, "지하 저장고");
        roomNames.put(DINING, "식당");
        roomNames.put(ENTRANCE, "현관 홀");
        roomNames.put(BEDROOM, "침실");
        roomNames.put(STUDY, "서재");
    }

    private void initHud() {
        inventoryBox.getChildren().setAll(new InventoryBar(state, this));
        fullScreenButton.setTooltip(new Tooltip("전체화면 (F11)"));
        watchFullScreen();
        state.addListener(this::refreshGameView);
        state.setPenaltyListener(this::onPenalty);
        hintButton.setTooltip(new Tooltip(state.getDifficulty().hintCost < 0
                ? "어려움 난이도에서는 힌트를 쓸 수 없다"
                : state.getDifficulty().hintCost == 0 ? "힌트 보기 (무료)"
                : "힌트 보기 (-" + state.getDifficulty().hintCost / 60 + "분)"));
        mapButton.setTooltip(new Tooltip(mapTitle() + " (M)"));
        menuButton.setTooltip(new Tooltip("일시 정지 / 저장 / 종료 (ESC)"));
        hintButton.setDisable(state.getDifficulty().hintCost < 0);

        // 이어하기라면 저장된 방에서, 새 게임이면 침실에서 시작
        boolean continuing = state.getCurrentRoom() != null;
        seenClueCount = state.getClues().size();
        rememberSoundState();
        timeWarned = state.getRemainingSeconds() <= 5 * 60;
        refreshGameView();
        startTimer();
        if (continuing) {
            goTo(state.getCurrentRoom());
            showMessage("저장된 지점에서 이어서 시작한다. (난이도: " + state.getDifficulty().label + ")");
        } else if (ep2) {
            goTo(Episode2.PIER);
            SoundManager.sfx("sfx_foghorn");
            showMessage("안개 낀 밤, 작은 보트가 섬의 선착장에 닿았다. 밧줄이 끊어져 보트가 떠내려간다... (난이도: "
                    + state.getDifficulty().label + ")");
            showIntro();
        } else {
            goTo(BEDROOM);
            showMessage("낯선 침실에서 눈을 떴다. 손에 초대장 한 장이 쥐어져 있다... (난이도: "
                    + state.getDifficulty().label + ")");
            showIntro();
        }
    }

    // ================= 힌트 =================
    @FXML
    private void onHint() {
        if (!state.isPlaying()) {
            return;
        }
        GameState.Difficulty d = state.getDifficulty();
        if (d.hintCost < 0) {
            showMessage("어려움 난이도에서는 힌트를 쓸 수 없다.");
            return;
        }
        if (d.hintCost == 0) {
            revealHint();
            return;
        }
        Label ask = logText("힌트를 보면 제한 시간이 " + d.hintCost / 60 + "분 줄어든다.\n"
                + "(지금까지 사용한 힌트: " + state.getHintsUsed() + "회)");
        Button yes = new Button("힌트 보기 (-" + d.hintCost / 60 + "분)");
        yes.getStyleClass().add("clue-button");
        yes.setOnAction(e -> revealHint());
        Button no = new Button("그만두기");
        no.getStyleClass().add("clue-button");
        no.setOnAction(e -> closeOverlay());
        HBox buttons = new HBox(12, yes, no);
        VBox box = new VBox(16, ask, buttons);
        box.setPadding(new Insets(8));
        showOverlay("힌트", box);
    }

    private void revealHint() {
        String hint = HintSystem.hintFor(state);
        if (!state.useHint()) {
            return;
        }
        refreshTimer();
        if (!state.isPlaying()) {
            return;
        }
        SoundManager.sfx("sfx_hint");
        Label text = logText(hint);
        text.getStyleClass().add("hint-text");
        VBox box = new VBox(10, text);
        box.setPadding(new Insets(8));
        box.setMaxWidth(560);
        showOverlay("힌트 (" + state.getHintsUsed() + "회째)", box);
        showMessage("힌트: " + hint);
    }

    // ================= 스토리: 시작 초대장 =================
    private void showIntro() {
        Label letter = new Label(ep2 ? Episode2.INTRO : Story.INTRO);
        letter.getStyleClass().addAll("note-paper", "story-doc");
        letter.setWrapText(true);
        letter.setMaxWidth(520);
        Button ok = new Button(ep2 ? "쪽지를 주머니에 넣는다" : "초대장을 내려놓는다");
        ok.getStyleClass().add("clue-button");
        ok.setOnAction(e -> {
            closeOverlay();
            showMessage(ep2 ? "등대 시계는 23시를 가리킨다. 등불이 켜지기 전에 꼭대기로 올라가야 한다."
                    : "저택 어딘가에 흑등회의 기록이 숨겨져 있을지도 모른다. 방을 구석구석 살펴보자.");
        });
        VBox box = new VBox(14, letter, ok);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        showOverlay(ep2 ? "주머니 속 쪽지" : "흑등회의 초대장", box);
    }

    // ================= 일시 정지 메뉴 (게임 종료) =================
    @FXML
    private void onPauseMenu() {
        if (!state.isPlaying()) {
            return;
        }
        if (overlayLayer.isVisible() && PAUSE_TITLE.equals(overlayTitle.getText())) {
            closeOverlay();
            return;
        }
        Label info = logText("타이머가 멈췄다. (남은 시간 " + GameState.formatTime(state.getRemainingSeconds())
                + ")\n진행 상황은 자동으로 저장되며, 타이틀의 '이어하기'로 다시 시작할 수 있다.");
        Button resume = pauseButton("계속하기", this::closeOverlay);
        Button title = pauseButton("저장하고 타이틀로", () -> {
            saveNow();
            Main.backToTitle();
        });
        Button quit = pauseButton("저장하고 게임 종료", () -> {
            saveNow();
            stopTimer();
            SoundManager.stopBgm();
            javafx.application.Platform.exit();
        });
        VBox box = new VBox(12, info, resume, title, quit);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        box.setPadding(new Insets(8, 40, 8, 40));
        showOverlay(PAUSE_TITLE, box);
        paused = true;
        if (timer != null) {
            timer.pause();
        }
    }

    private static Button pauseButton(String text, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().add("clue-button");
        b.setPrefWidth(240);
        b.setOnAction(e -> action.run());
        return b;
    }

    // ================= 미니맵 =================
    @FXML
    private void onShowMap() {
        if (!state.isPlaying()) {
            return;
        }
        showOverlay(mapTitle(), ep2 ? MapView.buildEpisode2(state) : MapView.build(state));
    }

    private String mapTitle() {
        return ep2 ? "등대 지도" : "저택 지도";
    }

    /** 키보드 단축키 (M: 지도, H: 힌트). Main 에서 호출. */
    public void handleKey(javafx.scene.input.KeyCode code) {
        if (code == javafx.scene.input.KeyCode.M) {
            if (overlayLayer.isVisible() && mapTitle().equals(overlayTitle.getText())) {
                closeOverlay();
            } else {
                onShowMap();
            }
        } else if (code == javafx.scene.input.KeyCode.H) {
            onHint();
        } else if (code == javafx.scene.input.KeyCode.ESCAPE) {
            if (overlayLayer.isVisible() && !PAUSE_TITLE.equals(overlayTitle.getText())) {
                closeOverlay(); // 열린 창 닫기
            } else {
                onPauseMenu();
            }
        }
    }

    // ================= 저장 / 업적 =================
    /** 진행 상황 저장 (방 이동 시, 주기적으로, 창 닫을 때). */
    public void saveNow() {
        if (state.isPlaying()) {
            SaveManager.save(state);
        }
        ticksSinceSave = 0;
    }

    private void checkAchievements() {
        for (Achievement a : Achievement.checkDuringPlay(state)) {
            showToast(a);
        }
    }

    private void showToast(Achievement a) {
        SoundManager.sfx("sfx_achievement");
        toastLabel.setText("🏆 업적 달성: " + a.title + "\n" + a.description);
        toastLabel.setVisible(true);
        toastLabel.setOpacity(0);
        FadeTransition in = new FadeTransition(Duration.millis(300), toastLabel);
        in.setToValue(1);
        FadeTransition out = new FadeTransition(Duration.millis(600), toastLabel);
        out.setDelay(Duration.seconds(3));
        out.setToValue(0);
        out.setOnFinished(e -> toastLabel.setVisible(false));
        new javafx.animation.SequentialTransition(in, out).play();
    }

    // ================= 제한 시간 =================
    private void startTimer() {
        refreshTimer();
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            state.tick();
            refreshTimer();
            if (++ticksSinceSave >= AUTOSAVE_SECONDS) {
                saveNow();
            }
        }));
        timer.setCycleCount(Animation.INDEFINITE);
        timer.play();
    }

    /** 게임 화면을 떠날 때(다시 하기 / 타이틀로) 호출. */
    public void stopTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    private void refreshTimer() {
        int remain = state.getRemainingSeconds();
        timerLabel.setText("⏳ " + GameState.formatTime(remain));
        timerLabel.getStyleClass().remove("hud-timer-warn");
        if (remain <= 5 * 60) {
            timerLabel.getStyleClass().add("hud-timer-warn");
            if (!timeWarned && state.isPlaying()) {
                timeWarned = true;
                SoundManager.sfx("sfx_clock_warning");
                SoundManager.bgm("bgm_hurry");
                showMessage(ep2 ? "머리 위에서 시계 장치가 빨라진다. 등불이 켜지기까지 5분도 남지 않았다!"
                        : "남은 시간이 5분도 안 된다! 서두르자.");
            }
        }
    }

    /** 잘못된 시도로 시간이 차감되면 메시지 + 타이머 깜빡임. */
    private void onPenalty(int seconds, String reason) {
        SoundManager.sfx("sfx_wrong");
        refreshTimer();
        if (seconds <= 0) {
            showMessage("잘못된 시도(" + reason + "). 쉬움 난이도라 시간은 줄지 않았다.");
            return;
        }
        showMessage("잘못된 시도(" + reason + ") - 제한 시간 " + seconds + "초 감소!");
        FadeTransition blink = new FadeTransition(Duration.millis(180), timerLabel);
        blink.setFromValue(1);
        blink.setToValue(0.2);
        blink.setAutoReverse(true);
        blink.setCycleCount(6);
        blink.play();
    }

    private void refreshGameView() {
        if (state.isPlaying()) {
            playChangeSound();
            refreshHud();
        } else if (!clearLayer.isVisible()) {
            playEndSound(state.isGameCleared());
            stopTimer();
            refreshTimer();
            SaveManager.deleteSave();
            clearViewController.show(state, state.isGameCleared(), Achievement.checkAtEnd(state));
            showEndingImage();
            closeOverlay();
            gameLayer.setDisable(true);
            gameLayer.setVisible(false);
            clearLayer.setOpacity(0);
            clearLayer.setVisible(true);
            clearLayer.requestFocus();

            FadeTransition fade = new FadeTransition(Duration.millis(650), clearLayer);
            fade.setToValue(1);
            fade.play();
        }
    }

    // ================= 효과음 / 배경음 =================
    private void rememberSoundState() {
        lastInventory.clear();
        lastInventory.addAll(state.getInventory());
        lastClueCount = state.getClues().size();
        lastClearedCount = state.getClearedCount();
        lastCleared.clear();
        lastCleared.addAll(state.getClearedPuzzles());
        lastFridgeUnlocked = state.isFridgeUnlocked();
        lastSafeOpened = state.isCleared(GameState.PUZZLE_SAFE);
        lastFlags.clear();
        lastFlags.addAll(state.getFlags());
    }

    /** 직전 상태와 비교해 가장 중요한 변화 하나에 맞는 효과음을 낸다. */
    private void playChangeSound() {
        boolean gotFragment = false;
        boolean gotItem = false;
        for (String item : state.getInventory()) {
            if (!lastInventory.contains(item)) {
                gotItem = true;
                if (item.startsWith("KEY_FRAGMENT") || Episode2.SEALS.contains(item)) {
                    gotFragment = true;
                }
            }
        }
        boolean unlocked = (state.isFridgeUnlocked() && !lastFridgeUnlocked)
                || (state.isCleared(GameState.PUZZLE_SAFE) && !lastSafeOpened)
                || (state.isCleared(Episode2.PUZZLE_DIAL) && !lastCleared.contains(Episode2.PUZZLE_DIAL))
                || (state.hasFlag(Episode2.F_SEALS_IN) && !lastFlags.contains(Episode2.F_SEALS_IN));
        boolean powered = state.hasFlag(Episode2.F_POWER) && !lastFlags.contains(Episode2.F_POWER);
        boolean solved = state.getClearedCount() > lastClearedCount;
        for (String id : state.getClearedPuzzles()) {
            if (!lastCleared.contains(id)) {
                String voice = Story.voiceFor(id, state);
                if (voice != null) {
                    javafx.animation.PauseTransition later = new javafx.animation.PauseTransition(Duration.seconds(1.6));
                    later.setOnFinished(e -> showMessage(voice));
                    later.play();
                }
            }
        }
        boolean newClue = state.getClues().size() > lastClueCount;
        rememberSoundState();

        if (gotFragment) {
            SoundManager.sfx("sfx_key_fragment");
        } else if (powered) {
            SoundManager.sfx(SoundManager.first("sfx_power_on", "sfx_unlock"));
        } else if (unlocked) {
            SoundManager.sfx("sfx_unlock");
        } else if (solved) {
            SoundManager.sfx("sfx_correct");
        } else if (gotItem) {
            SoundManager.sfx("sfx_item");
        } else if (newClue) {
            SoundManager.sfx("sfx_paper");
        }
    }

    private void playEndSound(boolean success) {
        SoundManager.stopSfx("sfx_tick");
        SoundManager.stopBgm();
        if (ep2) {
            boolean lit = !success || Episode2.ENDING_ON.equals(state.getEnding());
            SoundManager.sfx(lit ? SoundManager.first("sfx_lantern_on", "sfx_final_door")
                    : SoundManager.first("sfx_lantern_off", "sfx_final_door"));
            String music = lit ? SoundManager.first("bgm_ep2_bad_end", "bgm_gameover")
                    : SoundManager.first("bgm_ep2_true_end", "bgm_clear");
            javafx.animation.PauseTransition wait = new javafx.animation.PauseTransition(Duration.seconds(2.5));
            wait.setOnFinished(e -> {
                if (!state.isPlaying()) {
                    SoundManager.bgm(music);
                }
            });
            wait.play();
            return;
        }
        if (success) {
            SoundManager.sfx("sfx_final_door");
            javafx.animation.PauseTransition wait = new javafx.animation.PauseTransition(Duration.seconds(2.5));
            wait.setOnFinished(e -> {
                if (!state.isPlaying()) { // 그새 "다시 하기"를 눌렀으면 틀지 않는다
                    SoundManager.bgm("bgm_clear");
                }
            });
            wait.play();
        } else {
            SoundManager.bgm("bgm_gameover");
        }
    }

    private void playRoomSound(String from, String to) {
        if (ep2) {
            if (from != null && !from.equals(to)) {
                boolean door = Episode2.PIER.equals(from) || Episode2.PIER.equals(to)
                        || Episode2.LANTERN.equals(to);
                SoundManager.sfx(door ? SoundManager.first("sfx_iron_door", "sfx_door") : "sfx_stairs");
            }
            if (timeWarned) {
                SoundManager.bgm("bgm_hurry");
            } else if (Episode2.PIER.equals(to)) {
                SoundManager.bgm(SoundManager.first("bgm_ep2_pier", "bgm_manor"));
            } else if (Episode2.LANTERN.equals(to)) {
                SoundManager.bgm(SoundManager.first("bgm_ep2_top", "bgm_ep2_tower", "bgm_basement"));
            } else {
                SoundManager.bgm(SoundManager.first("bgm_ep2_tower", "bgm_basement"));
            }
            return;
        }
        if (from != null && !from.equals(to)) {
            boolean stairs = (KITCHEN.equals(from) && STORAGE.equals(to))
                    || (STORAGE.equals(from) && KITCHEN.equals(to));
            SoundManager.sfx(stairs ? "sfx_stairs" : "sfx_door");
        }
        if (timeWarned) {
            SoundManager.bgm("bgm_hurry");
        } else if (KITCHEN.equals(to) || STORAGE.equals(to)) {
            SoundManager.bgm("bgm_basement");
        } else {
            SoundManager.bgm("bgm_manor");
        }
    }

    /** 에피소드 2 결과 화면 뒤에 엔딩 그림을 깐다. */
    private void showEndingImage() {
        if (!ep2) {
            return;
        }
        String file = Episode2.endingImage(state, state.isGameCleared());
        javafx.scene.image.Image img = file == null ? null : Images.load(file);
        if (img == null) {
            return;
        }
        javafx.scene.image.ImageView view = new javafx.scene.image.ImageView(img);
        view.setPreserveRatio(false);
        view.fitWidthProperty().bind(clearLayer.widthProperty());
        view.fitHeightProperty().bind(clearLayer.heightProperty());
        view.setOpacity(0.55);
        view.setMouseTransparent(true);
        clearLayer.getChildren().add(0, view);
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
        fragmentLabel.setText(ep2 ? "등불 인장 " + Episode2.sealsFound(state) + "/3"
                : "열쇠 조각 " + state.getKeyFragments() + "/" + GameState.TOTAL_KEY_FRAGMENTS);
        int clueCount = state.getClues().size();
        boolean hasNew = clueCount > seenClueCount;
        clueButton.setText("단서 " + clueCount + (hasNew ? " ●" : ""));
        if (hasNew) {
            checkAchievements();
        }
        clueButton.getStyleClass().remove("clue-new");
        if (hasNew) {
            clueButton.getStyleClass().add("clue-new");
        }
    }

    // ================= 단서 기록 창 =================
    @FXML
    private void onShowClues() {
        if (!state.isPlaying()) {
            return;
        }
        seenClueCount = state.getClues().size();
        refreshHud();

        // 탭 1: 단서
        VBox clueList = new VBox(10);
        clueList.setPadding(new Insets(10));
        List<String> clues = new ArrayList<>();
        VBox docList = new VBox(10);
        docList.setPadding(new Insets(10));
        for (String c : state.getClues()) {
            if (Story.isDoc(c)) {
                Label doc = new Label(c);
                doc.getStyleClass().add("clue-item");
                doc.setWrapText(true);
                doc.setMaxWidth(Double.MAX_VALUE);
                docList.getChildren().add(doc);
            } else {
                clues.add(c);
            }
        }
        int docCount = docList.getChildren().size();
        if (docCount == 0) {
            docList.getChildren().add(logText(ep2 ? "아직 찾은 기록이 없다. 등대 곳곳에 흑등회의 기록이 남아 있을 것이다."
                    : "아직 찾은 기록이 없다. 흑등회는 기록을 저택 곳곳에 숨겨 두었다."));
        }
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
                new Tab("흑등회 기록 (" + docCount + "/" + Story.TOTAL_DOCS + ")", scroll(docList)),
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
        if (!state.isPlaying()) {
            return;
        }
        Parent room = rooms.get(roomId);
        if (room == null) {
            showMessage("아직 연결되지 않은 방입니다: " + roomId);
            return;
        }
        closeOverlay();
        boolean firstRoom = roomContainer.getChildren().isEmpty();
        playRoomSound(firstRoom ? null : state.getCurrentRoom(), roomId);
        roomContainer.getChildren().setAll(room);
        roomLabel.setText(roomNames.get(roomId));
        state.enterRoom(roomId);
        saveNow();
        checkAchievements();
    }

    @Override
    public void showOverlay(String title, Node content) {
        if (!state.isPlaying()) {
            return;
        }
        closeOverlay();
        overlayTitle.setText(title);
        overlayContent.getChildren().setAll(content);
        overlayLayer.setVisible(true);
    }

    @Override
    public void openPuzzle(String title, String fxmlName, Runnable onSolved) {
        if (!state.isPlaying()) {
            return;
        }
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
        if (paused) {
            paused = false;
            if (timer != null) {
                timer.play();
            }
        }
        if (activePuzzle != null) {
            activePuzzle.onClose();
            activePuzzle = null;
        }
        overlayContent.getChildren().clear();
        overlayLayer.setVisible(false);
    }

    @Override
    public void showMessage(String message) {
        if (!state.isPlaying()) {
            return;
        }
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
        if (!state.isPlaying()) {
            return;
        }
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

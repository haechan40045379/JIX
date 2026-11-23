package com.escapemanor.ui;

    import com.escapemanor.core.GameContext;
    import com.escapemanor.core.GameManager;
    import com.escapemanor.core.GameState;
    import com.escapemanor.core.ItemId;
    import com.escapemanor.core.RoomId;
    import com.escapemanor.core.RoomManager;
    import javafx.beans.binding.Bindings;
    import javafx.collections.ListChangeListener;
    import javafx.geometry.Insets;
    import javafx.geometry.Pos;
    import javafx.scene.Parent;
    import javafx.scene.control.Alert;
    import javafx.scene.control.Button;
    import javafx.scene.control.ButtonType;
    import javafx.scene.control.ChoiceDialog;
    import javafx.scene.control.Label;
    import javafx.scene.control.ScrollPane;
        import javafx.scene.control.Tooltip;
    import javafx.scene.layout.BorderPane;
    import javafx.scene.layout.HBox;
    import javafx.scene.layout.Priority;
    import javafx.scene.layout.Region;
    import javafx.scene.layout.StackPane;
    import javafx.scene.layout.VBox;
    import java.net.URL;
    import java.util.Arrays;
    import java.util.List;

    /** 두 번째 참고 이미지의 위치 체계: 상단 도구 / 중앙 방 / 오른쪽 인벤토리 / 하단 대사. */
    public final class GameView {
        private static final int INVENTORY_SLOTS = 8;
        private final GameManager game;
        private final RoomManager rooms;
        private final GameState state;
        private final GameContext context;
        private final Runnable onTitle;
        private final Runnable onRestart;
        private final BorderPane root = new BorderPane();
        private final StackPane roomHolder = new StackPane();
        private final VBox slots = new VBox(9);
    
        public GameView(GameManager game, RoomManager rooms, Runnable onTitle, Runnable onRestart) {
            this.game = game;
            this.rooms = rooms;
            this.state = game.state();
            this.context = new GameContext(game);
            this.onTitle = onTitle;
            this.onRestart = onRestart;
            root.getStyleClass().add("game-root");
            buildLayout();
            URL css = GameView.class.getResource("/css/game.css");
            if (css != null) root.getStylesheets().add(css.toExternalForm());
            state.currentRoomProperty().addListener((obs, oldRoom, newRoom) -> showRoom(newRoom));
            state.inventory().addListener((ListChangeListener<ItemId>) c -> refreshInventory());
            game.setOnVictory(() -> finish("탈출 성공", "열쇠 세 개를 사용해 저택을 탈출했습니다."));
            game.setOnTimeout(() -> finish("시간 초과", "제한 시간 안에 탈출하지 못했습니다."));
            refreshInventory();
        }

        public Parent getRoot() { return root; }
        public void loadInitialRoom() { showRoom(state.currentRoom()); }

        private void buildLayout() {
            HBox top = new HBox(11);
            top.getStyleClass().add("top-bar");
            top.setAlignment(Pos.CENTER_LEFT);

            Label time = new Label();
            time.getStyleClass().add("timer-text");
            time.textProperty().bind(Bindings.createStringBinding(
                    () -> "◷  남은 시간  " + String.format("%02d:%02d:%02d",
                            state.remainingSeconds() / 3600, (state.remainingSeconds() % 3600) / 60,
                            state.remainingSeconds() % 60), state.remainingSecondsProperty()));
            Region gap = new Region();
            HBox.setHgrow(gap, Priority.ALWAYS);
            Button journal = iconButton("▤", "기록 / 저널");
            journal.setOnAction(e -> showJournal());
            Button settings = iconButton("⚙", "설정 / 통합 테스트");
            settings.setOnAction(e -> showSettings());
            Region inventoryTopSpace = new Region();
            inventoryTopSpace.setMinWidth(156);
            inventoryTopSpace.setMaxWidth(156);
            top.getChildren().addAll(time, gap, journal, settings, inventoryTopSpace);
            root.setTop(top);

            // 중앙 플레이 영역: 사진 + 양쪽 이동 화살표
            StackPane visual = new StackPane();
            visual.getStyleClass().add("visual-area");
            roomHolder.getStyleClass().add("room-holder");
            visual.getChildren().add(roomHolder);
            StackPane.setAlignment(roomHolder, Pos.CENTER);

            Button previous = navButton("❮", "이전 방 (임시 테스트)");
            Button next = navButton("❯", "다음 방 (임시 테스트)");
            previous.setOnAction(e -> game.goTo(state.currentRoom().offset(-1)));
            next.setOnAction(e -> game.goTo(state.currentRoom().offset(1)));
            visual.getChildren().addAll(previous, next);
            StackPane.setAlignment(previous, Pos.CENTER_LEFT);
            StackPane.setAlignment(next, Pos.CENTER_RIGHT);
            StackPane.setMargin(previous, new Insets(0, 0, 0, 6));
            StackPane.setMargin(next, new Insets(0, 6, 0, 0));
            VBox.setVgrow(visual, Priority.ALWAYS);

            Label narration = new Label();
            narration.getStyleClass().add("narration");
            narration.setWrapText(true);
            narration.setAlignment(Pos.CENTER_LEFT);
            narration.setMaxWidth(Double.MAX_VALUE);
            narration.textProperty().bind(state.narrationProperty());
            HBox narrationBar = new HBox(narration);
            narrationBar.getStyleClass().add("narration-bar");
            narrationBar.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(narration, Priority.ALWAYS);

            VBox playArea = new VBox(visual, narrationBar);
            playArea.getStyleClass().add("play-area");
            HBox.setHgrow(playArea, Priority.ALWAYS);

            // 인벤토리는 기존 '선택지' 패널 자리에 고정한다.
            VBox inventory = new VBox(12);
            inventory.getStyleClass().add("inventory-panel");
            inventory.setPrefWidth(165);
            inventory.setMinWidth(145);
            inventory.setMaxWidth(195);
            Label title = new Label("✦ 인벤토리 ✦");
            title.getStyleClass().add("inventory-title");
            ScrollPane scroll = new ScrollPane(slots);
            scroll.getStyleClass().add("inventory-scroll");
            scroll.setFitToWidth(true);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            VBox.setVgrow(scroll, Priority.ALWAYS);
            inventory.getChildren().addAll(title, scroll);
            HBox gameBody = new HBox(playArea, inventory);
            root.setCenter(gameBody);
        }

        private void showRoom(RoomId room) {
                Parent roomView = rooms.createRoomView(room, context);
            roomHolder.getChildren().setAll(roomView);
            StackPane.setAlignment(roomView, Pos.CENTER);
        }

        private void refreshInventory() {
            slots.getChildren().clear();
            int count = Math.max(INVENTORY_SLOTS, state.inventory().size());
            for (int i = 0; i < count; i++) {
                if (i < state.inventory().size()) {
                    ItemId item = state.inventory().get(i);
                    Button slot = new Button("🔑");
                    slot.getStyleClass().addAll("inventory-slot", "inventory-slot-filled");
                    Tooltip.install(slot, new Tooltip(item.title()));
                    slot.setOnAction(e -> game.say(item.title() + " — " + item.detail()));
                    slots.getChildren().add(slot);
                } else {
                    Region empty = new Region();
                    empty.getStyleClass().add("inventory-slot");
                    slots.getChildren().add(empty);
                }
            }
        }

        private Button iconButton(String icon, String tooltip) {
            Button b = new Button(icon);
            b.getStyleClass().add("header-icon-button");
            Tooltip.install(b, new Tooltip(tooltip));
            return b;
        }
        private Button navButton(String icon, String tooltip) {
            Button b = new Button(icon);
            b.getStyleClass().add("nav-button");
            Tooltip.install(b, new Tooltip(tooltip));
            return b;
        }
        private void showJournal() {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("조사 기록");
            alert.setHeaderText("발견한 단서와 행동 기록");
            String text = String.join("\n\n", state.journal());
            alert.getDialogPane().setPrefWidth(530);
            alert.setContentText(text.isBlank() ? "아직 기록이 없다." : text);
            alert.showAndWait();
        }

        private void showSettings() {
            // 개발 단계에서만 제공하는 통합 테스트 메뉴. 릴리스에서 제거할 항목.
            List<String> actions = List.of("계속하기", "방 이동 테스트", "열쇠 지급 테스트", "게임 다시 시작", "타이틀로 돌아가기");
            ChoiceDialog<String> choice = new ChoiceDialog<>(actions.get(0), actions);
            choice.setTitle("설정 / 개발 테스트");
            choice.setHeaderText("임시 개발용 메뉴 (최종 디자인에서 교체)");
            choice.setContentText("기능 선택:");
            choice.showAndWait().ifPresent(action -> {
                switch (action) {
                    case "방 이동 테스트" -> chooseRoom();
                    case "열쇠 지급 테스트" -> chooseKey();
                    case "게임 다시 시작" -> confirmAction("처음부터 시작하시겠습니까?", onRestart);
                    case "타이틀로 돌아가기" -> confirmAction("타이틀로 이동하시겠습니까?", onTitle);
                    default -> { }
                }
            });
        }
        private void chooseRoom() {
            ChoiceDialog<RoomId> choice = new ChoiceDialog<>(state.currentRoom(), Arrays.asList(RoomId.values()));
            choice.setTitle("방 이동 테스트");
            choice.setHeaderText("실제 맵 연결 전 테스트 기능");
            choice.showAndWait().ifPresent(game::goTo);
        }
        private void chooseKey() {
            ChoiceDialog<ItemId> choice = new ChoiceDialog<>(ItemId.values()[0], Arrays.asList(ItemId.values()));
            choice.setTitle("열쇠 지급 테스트");
            choice.setHeaderText("퍼즐 완료 연동 전 확인용");
            choice.showAndWait().ifPresent(game::grantItem);
        }
        private void confirmAction(String message, Runnable action) {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
            a.setHeaderText(null);
            if (a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) action.run();
        }
        private void finish(String title, String text) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, text, ButtonType.OK);
            alert.setTitle(title);
            alert.setHeaderText(title);
            alert.showAndWait();
            onTitle.run();
        }
    }

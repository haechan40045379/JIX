package escape;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * 인벤토리 바 (칸 GameState.INVENTORY_SLOTS 개).
 * - 아이템 클릭: 손에 들기(선택) / 다시 클릭하면 내려놓기
 * - 더블클릭 또는 우클릭: 살펴보기(큰 그림 + 설명)
 * - 아이템을 든 채 방의 물건을 클릭하면 그 물건에 사용 (각 방 컨트롤러에서 처리)
 * 팀 통합 시 이 클래스를 공용 HUD에 그대로 붙이면 된다.
 */
public class InventoryBar extends HBox {

    private static final double SLOT_SIZE = 44;

    private final GameState state;
    private final GameHost host;
    private final Label heldLabel = new Label();

    public InventoryBar(GameState state, GameHost host) {
        super(6);
        this.state = state;
        this.host = host;
        setAlignment(Pos.CENTER_LEFT);
        heldLabel.getStyleClass().add("hud-label");
        heldLabel.setStyle("-fx-text-fill: #f0c060;");
        state.addListener(this::refresh);
        refresh();
    }

    private void refresh() {
        List<String> items = new ArrayList<>(state.getInventory());
        List<Node> slots = new ArrayList<>();
        for (int i = 0; i < GameState.INVENTORY_SLOTS; i++) {
            slots.add(i < items.size() ? itemSlot(items.get(i)) : emptySlot());
        }
        String held = state.getSelectedItem();
        heldLabel.setText(held == null ? "" : "손에 든 것: " + GameState.displayName(held));
        slots.add(heldLabel);
        getChildren().setAll(slots);
    }

    private StackPane emptySlot() {
        StackPane slot = new StackPane();
        slot.setPrefSize(SLOT_SIZE, SLOT_SIZE);
        slot.setMinSize(SLOT_SIZE, SLOT_SIZE);
        slot.getStyleClass().add("inv-slot");
        return slot;
    }

    private StackPane itemSlot(String itemId) {
        StackPane slot = emptySlot();
        slot.getChildren().add(icon(itemId, SLOT_SIZE - 8));
        slot.getStyleClass().add("inv-item");
        if (state.isSelected(itemId)) {
            slot.getStyleClass().add("inv-selected");
        }
        boolean canSelect = !GameState.KEY_FRAGMENT_3.equals(itemId);
        String interaction = canSelect ? "\n클릭: 들기 / 더블클릭·우클릭: 살펴보기"
                : "\n더블클릭·우클릭: 살펴보기";
        Tooltip.install(slot, new Tooltip(GameState.displayName(itemId) + interaction));

        slot.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY || e.getClickCount() == 2) {
                examine(itemId);
            } else if (canSelect && e.getButton() == MouseButton.PRIMARY) {
                state.toggleSelect(itemId);
                host.showMessage(state.isSelected(itemId)
                        ? "[" + GameState.displayName(itemId) + "]을(를) 손에 들었다. 사용할 곳을 클릭하자."
                        : "[" + GameState.displayName(itemId) + "]을(를) 내려놓았다.");
            }
        });
        return slot;
    }

    /** 살펴보기: 큰 그림과 설명을 오버레이로 띄운다. */
    public void examine(String itemId) {
        Node big = icon(itemId, 220);
        Label desc = new Label(GameState.description(itemId));
        desc.getStyleClass().add("puzzle-text");
        desc.setWrapText(true);
        desc.setMaxWidth(420);
        VBox box = new VBox(16, big, desc);
        box.setAlignment(Pos.CENTER);
        host.showOverlay(GameState.displayName(itemId), box);
    }

    /** 아이템 그림. 그림 파일이 없으면 이름 글자로 대체. */
    static Node icon(String itemId, double size) {
        Image img = Images.load(GameState.iconFile(itemId));
        if (img == null) {
            Label text = new Label(GameState.displayName(itemId));
            text.setWrapText(true);
            text.setMaxWidth(size);
            text.setAlignment(Pos.CENTER);
            text.setStyle("-fx-text-fill: #e8dcc0; -fx-font-size: " + (size > 100 ? 22 : 10) + "px;");
            return text;
        }
        ImageView view = new ImageView(img);
        if (GameState.KEY_FRAGMENT_3.equals(itemId)) {
            // 실제 알파 경계 (640, 199)–(1542, 569)에 여백을 둔다.
            view.setViewport(new Rectangle2D(620, 179, 943, 411));
        }
        view.setFitWidth(size);
        view.setFitHeight(size);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        return view;
    }
}

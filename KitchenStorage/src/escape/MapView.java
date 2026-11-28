package escape;

import java.util.LinkedHashMap;
import java.util.Map;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;

/**
 * 저택 미니맵: 층별 방 배치, 현재 위치, 방문 여부, 열쇠 조각 획득 여부를 그린다.
 * 방문하지 않은 방은 "???"로 가려진다.
 */
public final class MapView {

    private static final double W = 130;
    private static final double H = 72;
    private static final double GAP_X = 40;
    private static final double GAP_Y = 40;
    private static final double LEFT = 70;
    private static final double TOP = 14;

    /** 방 ID → {열, 행} (행 0 = 2층, 1 = 1층, 2 = 지하). */
    private static final Map<String, int[]> GRID = new LinkedHashMap<>();
    static {
        GRID.put(GameHost.BEDROOM, new int[] {2, 0});
        GRID.put(GameHost.STUDY, new int[] {3, 0});
        GRID.put(GameHost.KITCHEN, new int[] {0, 1});
        GRID.put(GameHost.DINING, new int[] {1, 1});
        GRID.put(GameHost.ENTRANCE, new int[] {2, 1});
        GRID.put(GameHost.STORAGE, new int[] {0, 2});
    }

    /** 이어진 방(문/계단). */
    private static final String[][] LINKS = {
        {GameHost.BEDROOM, GameHost.STUDY},
        {GameHost.BEDROOM, GameHost.ENTRANCE},
        {GameHost.ENTRANCE, GameHost.DINING},
        {GameHost.DINING, GameHost.KITCHEN},
        {GameHost.KITCHEN, GameHost.STORAGE},
    };

    private MapView() {
    }

    public static String roomName(String roomId) {
        if (roomId.startsWith("EP2_")) {
            return Episode2.roomName(roomId);
        }
        switch (roomId) {
            case GameHost.KITCHEN: return "주방";
            case GameHost.STORAGE: return "지하 저장고";
            case GameHost.DINING: return "식당";
            case GameHost.ENTRANCE: return "현관 홀";
            case GameHost.BEDROOM: return "침실";
            case GameHost.STUDY: return "서재";
            default: return roomId;
        }
    }

    public static Pane build(GameState s) {
        Pane pane = new Pane();
        pane.setPrefSize(LEFT + 4 * W + 3 * GAP_X + 20, TOP + 3 * H + 2 * GAP_Y + 50);
        pane.getStyleClass().add("map-pane");

        String[] floors = {"2층", "1층", "지하"};
        for (int r = 0; r < floors.length; r++) {
            Label f = new Label(floors[r]);
            f.getStyleClass().add("map-floor");
            f.relocate(8, TOP + r * (H + GAP_Y) + H / 2 - 10);
            pane.getChildren().add(f);
        }

        for (String[] link : LINKS) {
            boolean known = s.isVisited(link[0]) || s.isVisited(link[1]);
            if (!known) {
                continue;
            }
            Line line = new Line(cx(link[0]), cy(link[0]), cx(link[1]), cy(link[1]));
            line.getStyleClass().add("map-link");
            pane.getChildren().add(line);
        }

        for (Map.Entry<String, int[]> e : GRID.entrySet()) {
            pane.getChildren().add(roomBox(s, e.getKey(), e.getValue()));
        }

        Label legend = new Label("■ 현재 위치    ✔ 열쇠 조각 획득    ??? 아직 가 보지 않은 방");
        legend.getStyleClass().add("map-legend");
        legend.relocate(LEFT, TOP + 3 * H + 2 * GAP_Y + 18);
        pane.getChildren().add(legend);
        return pane;
    }

    // ================= 에피소드 2: 세로로 선 등대 =================
    private static final double T_W = 150;
    private static final double T_H = 56;
    private static final double T_GAP = 22;
    private static final double T_LEFT = 260;

    /** 에피소드 2 지도: 선착장 옆에 등대 1층~꼭대기가 위로 쌓인다. */
    public static Pane buildEpisode2(GameState s) {
        String[] tower = {Episode2.LANTERN, Episode2.ENGINE, Episode2.OBSERVATION, Episode2.ARCHIVE};
        String[] floors = {"꼭대기", "3층", "2층", "1층"};
        Pane pane = new Pane();
        pane.setPrefSize(T_LEFT + T_W + 150, TOP + 4 * T_H + 3 * T_GAP + 50);
        pane.getStyleClass().add("map-pane");

        double pierX = T_LEFT - T_W - 60;
        double bottomY = TOP + 3 * (T_H + T_GAP);
        // 연결선 (선착장-1층, 층 사이 계단)
        if (s.isVisited(Episode2.PIER) || s.isVisited(Episode2.ARCHIVE)) {
            Line l = new Line(pierX + T_W, bottomY + T_H / 2, T_LEFT, bottomY + T_H / 2);
            l.getStyleClass().add("map-link");
            pane.getChildren().add(l);
        }
        for (int i = 0; i < tower.length - 1; i++) {
            if (s.isVisited(tower[i]) || s.isVisited(tower[i + 1])) {
                double y1 = TOP + i * (T_H + T_GAP) + T_H;
                Line l = new Line(T_LEFT + T_W / 2, y1, T_LEFT + T_W / 2, y1 + T_GAP);
                l.getStyleClass().add("map-link");
                pane.getChildren().add(l);
            }
        }
        for (int i = 0; i < tower.length; i++) {
            double y = TOP + i * (T_H + T_GAP);
            Label f = new Label(floors[i]);
            f.getStyleClass().add("map-floor");
            f.relocate(T_LEFT + T_W + 14, y + T_H / 2 - 10);
            pane.getChildren().add(f);
            pane.getChildren().add(towerBox(s, tower[i], T_LEFT, y));
        }
        pane.getChildren().add(towerBox(s, Episode2.PIER, pierX, bottomY));

        Label legend = new Label("■ 현재 위치    ✔ 인장 획득    ??? 아직 가 보지 않은 곳");
        legend.getStyleClass().add("map-legend");
        legend.relocate(pierX, TOP + 4 * T_H + 3 * T_GAP + 18);
        pane.getChildren().add(legend);
        return pane;
    }

    private static StackPane towerBox(GameState s, String room, double x, double y) {
        boolean visited = s.isVisited(room);
        boolean here = room.equals(s.getCurrentRoom());
        Label name = new Label(visited ? Episode2.roomName(room) : "???");
        name.getStyleClass().add("map-room-name");
        VBox box = new VBox(1, name);
        box.setAlignment(Pos.CENTER);
        String seal = Episode2.ARCHIVE.equals(room) ? Episode2.PUZZLE_CATALOG
                : Episode2.OBSERVATION.equals(room) ? Episode2.PUZZLE_MONITOR
                : Episode2.ENGINE.equals(room) ? Episode2.PUZZLE_CLOCK : null;
        if (visited && seal != null) {
            Label status = new Label(s.isCleared(seal) ? "✔ 인장 획득" : "인장 ?");
            status.getStyleClass().add("map-room-status");
            box.getChildren().add(status);
        }
        if (here) {
            Label me = new Label("■ 현재 위치");
            me.getStyleClass().add("map-room-here");
            box.getChildren().add(me);
        }
        StackPane cell = new StackPane(box);
        cell.setPrefSize(T_W, T_H);
        cell.getStyleClass().add("map-room");
        if (!visited) {
            cell.getStyleClass().add("map-room-unknown");
        }
        if (here) {
            cell.getStyleClass().add("map-room-current");
        }
        cell.relocate(x, y);
        return cell;
    }

    private static StackPane roomBox(GameState s, String room, int[] cell) {
        boolean visited = s.isVisited(room);
        boolean here = room.equals(s.getCurrentRoom());

        Label name = new Label(visited ? roomName(room) : "???");
        name.getStyleClass().add("map-room-name");
        VBox box = new VBox(2, name);
        box.setAlignment(Pos.CENTER);
        String fragment = fragmentPuzzle(room);
        if (visited && fragment != null) {
            Label status = new Label(s.isCleared(fragment) ? "✔ 조각 획득" : "열쇠 조각 ?");
            status.getStyleClass().add("map-room-status");
            box.getChildren().add(status);
        }
        if (here) {
            Label me = new Label("■ 현재 위치");
            me.getStyleClass().add("map-room-here");
            box.getChildren().add(me);
        }

        StackPane cellPane = new StackPane(box);
        cellPane.setPrefSize(W, H);
        cellPane.getStyleClass().add("map-room");
        if (!visited) {
            cellPane.getStyleClass().add("map-room-unknown");
        }
        if (here) {
            cellPane.getStyleClass().add("map-room-current");
        }
        cellPane.relocate(x(cell[0]), y(cell[1]));
        return cellPane;
    }

    /** 열쇠 조각을 주는 퍼즐이 있는 방. */
    private static String fragmentPuzzle(String room) {
        switch (room) {
            case GameHost.KITCHEN: return GameState.PUZZLE_SHAPE;
            case GameHost.DINING: return GameState.PUZZLE_DINING_TABLE;
            case GameHost.STUDY: return GameState.PUZZLE_SAFE;
            default: return null;
        }
    }

    private static double x(int col) {
        return LEFT + col * (W + GAP_X);
    }

    private static double y(int row) {
        return TOP + row * (H + GAP_Y);
    }

    private static double cx(String room) {
        return x(GRID.get(room)[0]) + W / 2;
    }

    private static double cy(String room) {
        return y(GRID.get(room)[1]) + H / 2;
    }
}

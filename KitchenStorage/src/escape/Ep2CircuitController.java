package escape;

import javafx.animation.PauseTransition;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

/**
 * [D] 배전반 4x4 회로 타일. 타일을 누르면 시계 방향으로 90도 돈다.
 * 왼쪽 전원(2행 왼쪽)에서 오른쪽 출력(3행 오른쪽)까지 이어지면 전원이 들어온다.
 */
public class Ep2CircuitController extends Ep2Puzzle {

    private static final double SIZE = 400;
    private static final double[] COL_X = {130.2, 182.3, 233.8, 285.4};
    private static final double[] ROW_Y = {119, 172, 225.2, 279.2};
    private static final double TILE = 46;

    static final int N = 1;
    static final int E = 2;
    static final int S = 4;
    static final int W = 8;
    private static final int STRAIGHT = N | S;
    private static final int CORNER = N | E;
    private static final int TEE = N | E | S;

    static final int IN_ROW = 1;
    static final int OUT_ROW = 2;

    /** 타일 모양 (회전 전). */
    static final int[][] TYPES = {
        {CORNER, STRAIGHT, TEE, CORNER},
        {STRAIGHT, CORNER, STRAIGHT, TEE},
        {TEE, CORNER, STRAIGHT, STRAIGHT},
        {CORNER, TEE, STRAIGHT, CORNER},
    };
    /** 정답 경로 타일 (쉬움 난이도에서 작은 표시). */
    private static final int[][] PATH = {{1, 0}, {1, 1}, {2, 1}, {2, 2}, {2, 3}};

    /** 현재 회전 (0~3, 90도 단위). */
    final int[][] rot = {
        {1, 0, 2, 3},
        {0, 1, 1, 0},
        {3, 2, 0, 2},
        {2, 1, 1, 0},
    };

    private final Button[][] tiles = new Button[4][4];
    private final Group[][] graphics = new Group[4][4];
    private Polyline inWire;
    private Polyline outWire;
    private Label result;
    private boolean done;

    @Override
    protected void build() {
        Pane board = new Pane();
        board.setPrefSize(SIZE, SIZE);
        board.setMinSize(SIZE, SIZE);
        board.setMaxSize(SIZE, SIZE);
        board.getChildren().add(picture("ep2_circuit_panel_bg.png", SIZE, SIZE));
        inWire = new Polyline(80, 200, 95, 200, 95, ROW_Y[IN_ROW], COL_X[0] - TILE / 2, ROW_Y[IN_ROW]);
        outWire = new Polyline(COL_X[3] + TILE / 2, ROW_Y[OUT_ROW], 323, ROW_Y[OUT_ROW], 323, 200, 333, 200);
        for (Polyline p : new Polyline[] {inWire, outWire}) {
            p.setStrokeWidth(6);
            p.setStrokeLineCap(StrokeLineCap.ROUND);
            p.setMouseTransparent(true);
        }
        board.getChildren().addAll(inWire, outWire);

        boolean easy = state.getDifficulty() == GameState.Difficulty.EASY;
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                int rr = r;
                int cc = c;
                Group g = tileGraphic(TYPES[r][c]);
                graphics[r][c] = g;
                Button b = new Button();
                b.setGraphic(g);
                b.getStyleClass().add("circuit-tile");
                b.setPrefSize(TILE, TILE);
                b.setMinSize(TILE, TILE);
                b.setMaxSize(TILE, TILE);
                b.relocate(COL_X[c] - TILE / 2, ROW_Y[r] - TILE / 2);
                b.setAccessibleText("회로 타일 " + (r + 1) + "행 " + (c + 1) + "열");
                b.setOnAction(e -> rotate(rr, cc));
                tiles[r][c] = b;
                board.getChildren().add(b);
            }
        }
        if (easy) {
            for (int[] p : PATH) {
                Circle mark = new Circle(COL_X[p[1]] + TILE / 2 - 6, ROW_Y[p[0]] - TILE / 2 + 6, 2.5,
                        Color.rgb(255, 230, 150, 0.7));
                mark.setMouseTransparent(true);
                board.getChildren().add(mark);
            }
        }
        result = text("");
        result.setMinHeight(20);
        root.getChildren().addAll(board,
                text("타일을 눌러 돌리자. 왼쪽 빨간 전원 단자에서 오른쪽 초록 출력 단자까지 전선을 이어야 한다."),
                result);
        refresh();
    }

    private static Group tileGraphic(int mask) {
        Group g = new Group();
        double h = TILE / 2;
        Circle frame = new Circle(h, h, h, Color.TRANSPARENT); // 회전 중심을 타일 가운데로
        g.getChildren().add(frame);
        int[][] dirs = {{N, 0, -1}, {E, 1, 0}, {S, 0, 1}, {W, -1, 0}};
        for (int[] d : dirs) {
            if ((mask & d[0]) != 0) {
                Line l = new Line(h, h, h + d[1] * h, h + d[2] * h);
                l.setStrokeWidth(7);
                l.setStrokeLineCap(StrokeLineCap.BUTT);
                g.getChildren().add(l);
            }
        }
        g.getChildren().add(new Circle(h, h, 5));
        return g;
    }

    static int rotateMask(int mask, int times) {
        int m = mask;
        for (int i = 0; i < times % 4; i++) {
            m = ((m << 1) | (m >> 3)) & 15;
        }
        return m;
    }

    int mask(int r, int c) {
        return rotateMask(TYPES[r][c], rot[r][c]);
    }

    private void rotate(int r, int c) {
        if (done) {
            return;
        }
        rot[r][c] = (rot[r][c] + 1) % 4;
        SoundManager.sfx(SoundManager.first("sfx_tile_rotate", "sfx_click"), 0.5);
        refresh();
    }

    /** 전원에서 이어진 타일을 찾는다. [r][c] = 전기가 통하면 true. */
    boolean[][] powered() {
        boolean[][] on = new boolean[4][4];
        if ((mask(IN_ROW, 0) & W) == 0) {
            return on;
        }
        java.util.ArrayDeque<int[]> queue = new java.util.ArrayDeque<>();
        on[IN_ROW][0] = true;
        queue.add(new int[] {IN_ROW, 0});
        int[][] dirs = {{N, -1, 0, S}, {E, 0, 1, W}, {S, 1, 0, N}, {W, 0, -1, E}};
        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int m = mask(cur[0], cur[1]);
            for (int[] d : dirs) {
                int nr = cur[0] + d[1];
                int nc = cur[1] + d[2];
                if ((m & d[0]) == 0 || nr < 0 || nr > 3 || nc < 0 || nc > 3 || on[nr][nc]) {
                    continue;
                }
                if ((mask(nr, nc) & d[3]) != 0) {
                    on[nr][nc] = true;
                    queue.add(new int[] {nr, nc});
                }
            }
        }
        return on;
    }

    boolean isSolved() {
        return powered()[OUT_ROW][3] && (mask(OUT_ROW, 3) & E) != 0;
    }

    private void refresh() {
        boolean[][] on = powered();
        Color live = Color.web("#ffd66b");
        Color dead = Color.web("#8c6b45");
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                Group g = graphics[r][c];
                g.setRotate(rot[r][c] * 90);
                Color col = on[r][c] ? live : dead;
                for (javafx.scene.Node n : g.getChildren()) {
                    if (n instanceof Line l) {
                        l.setStroke(col);
                    } else if (n instanceof Circle ci && ci.getFill() != Color.TRANSPARENT) {
                        ci.setFill(col);
                    }
                }
            }
        }
        inWire.setStroke((mask(IN_ROW, 0) & W) != 0 ? live : Color.web("#c0503a"));
        boolean solved = isSolved();
        outWire.setStroke(solved ? live : dead);
        if (solved && !done) {
            done = true;
            result.setText("전선이 모두 이어졌다! 배전반에서 불꽃이 튄다.");
            PauseTransition wait = new PauseTransition(Duration.millis(900));
            wait.setOnFinished(e -> solve());
            wait.play();
        }
    }
}

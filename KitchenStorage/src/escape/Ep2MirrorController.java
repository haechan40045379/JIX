package escape;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;

/**
 * [H] 등불과 거울 바닥 = 마지막 선택.
 * 6x6 바닥의 거울을 눌러 45도씩 돌리면 등불의 빛이 거울을 따라 꺾인다.
 * 빛이 왼쪽 아래 점화 렌즈에 닿으면 "등불을 켠다", 오른쪽 아래 차단 장치에 닿으면 "등불을 끈다".
 */
public class Ep2MirrorController extends Ep2Puzzle {

    private static final double SIZE = 390;
    private static final double GX0 = SIZE * 0.2156;
    private static final double GY0 = SIZE * 0.2135;
    private static final double CELL_W = SIZE * (0.7833 - 0.2156) / 6;
    private static final double CELL_H = SIZE * (0.786 - 0.2135) / 6;
    private static final int ENTRY_COL = 3;

    /** 방향: 0 '-' 가로, 1 '/', 2 '|' 세로, 3 '\'. */
    static final int[][] MIRRORS = {
        // {행, 열, 처음 방향}
        {1, 3, 0}, {1, 0, 3}, {1, 5, 1}, {3, 5, 0}, {3, 0, 0}, {3, 3, 1},
    };

    enum Target { NONE, BACK, LENS, SHUTTER }

    final int[] dir = new int[MIRRORS.length];
    private final Line[] mirrorLines = new Line[MIRRORS.length];
    private Polyline beam;
    private Label status;
    private HBox actions;
    private Target target = Target.NONE;

    @Override
    protected void build() {
        Pane board = new Pane();
        board.setPrefSize(SIZE, SIZE);
        board.setMinSize(SIZE, SIZE);
        board.setMaxSize(SIZE, SIZE);
        board.getChildren().add(picture("ep2_mirror_floor_bg.png", SIZE, SIZE));

        beam = new Polyline();
        beam.setStroke(Color.web("#fff1a8"));
        beam.setStrokeWidth(5);
        beam.setStrokeLineCap(StrokeLineCap.ROUND);
        beam.setEffect(new DropShadow(14, Color.web("#ffd34d")));
        beam.setMouseTransparent(true);
        board.getChildren().add(beam);

        for (int i = 0; i < MIRRORS.length; i++) {
            int idx = i;
            dir[i] = MIRRORS[i][2];
            double cx = GX0 + (MIRRORS[i][1] + 0.5) * CELL_W;
            double cy = GY0 + (MIRRORS[i][0] + 0.5) * CELL_H;
            Line m = new Line(-CELL_W * 0.42, 0, CELL_W * 0.42, 0);
            m.setStroke(Color.web("#d8f1ff"));
            m.setStrokeWidth(5);
            m.setStrokeLineCap(StrokeLineCap.ROUND);
            m.setEffect(new DropShadow(4, Color.BLACK));
            mirrorLines[i] = m;
            Button cell = new Button();
            cell.setGraphic(m);
            cell.getStyleClass().add("mirror-cell");
            cell.setPrefSize(CELL_W, CELL_H);
            cell.setMinSize(CELL_W, CELL_H);
            cell.setMaxSize(CELL_W, CELL_H);
            cell.relocate(cx - CELL_W / 2, cy - CELL_H / 2);
            cell.setAccessibleText("거울 " + (i + 1));
            cell.setOnAction(e -> turn(idx));
            board.getChildren().add(cell);
        }

        status = text("");
        status.setMinHeight(22);
        actions = new HBox(12);
        actions.setAlignment(Pos.CENTER);
        actions.setMinHeight(34);
        root.getChildren().addAll(board,
                text("거울을 눌러 돌리자. 왼쪽 아래 점화 렌즈 = 등불을 켠다, 오른쪽 아래 차단 장치 = 등불을 끈다."),
                status, actions);
        trace();
    }

    void turn(int i) {
        dir[i] = (dir[i] + 1) % 4;
        SoundManager.sfx(SoundManager.first("sfx_mirror_rotate", "sfx_click"), 0.5);
        trace();
    }

    private int mirrorAt(int r, int c) {
        for (int i = 0; i < MIRRORS.length; i++) {
            if (MIRRORS[i][0] == r && MIRRORS[i][1] == c) {
                return i;
            }
        }
        return -1;
    }

    /** 빛의 길을 따라가 목적지를 구하고 선을 그린다. */
    Target trace() {
        for (int i = 0; i < MIRRORS.length; i++) {
            mirrorLines[i].setRotate(switch (dir[i]) {
                case 1 -> -45;
                case 2 -> 90;
                case 3 -> 45;
                default -> 0;
            });
        }
        List<Double> pts = new ArrayList<>();
        double entryX = GX0 + (ENTRY_COL + 0.5) * CELL_W;
        pts.add(entryX);
        pts.add(SIZE * 0.20);
        int r = 0;
        int c = ENTRY_COL;
        int dr = 1;
        int dc = 0;
        Target result = Target.NONE;
        for (int step = 0; step < 80; step++) {
            if (r < 0 || r > 5 || c < 0 || c > 5) {
                // 격자 밖으로 나간 지점
                if (r < 0 || r > 5) {
                    pts.add(GX0 + (c + 0.5) * CELL_W);
                    pts.add(r < 0 ? GY0 : GY0 + 6 * CELL_H);
                } else {
                    pts.add(c < 0 ? GX0 : GX0 + 6 * CELL_W);
                    pts.add(GY0 + (r + 0.5) * CELL_H);
                }
                if (r > 5 && c == 0) {
                    result = Target.LENS;
                    pts.add(SIZE * 0.205);
                    pts.add(SIZE * 0.865);
                } else if (r > 5 && c == 5) {
                    result = Target.SHUTTER;
                    pts.add(SIZE * 0.79);
                    pts.add(SIZE * 0.865);
                } else if (r < 0 && c == ENTRY_COL) {
                    result = Target.BACK;
                    pts.add(entryX);
                    pts.add(SIZE * 0.20);
                }
                break;
            }
            int m = mirrorAt(r, c);
            if (m >= 0) {
                pts.add(GX0 + (c + 0.5) * CELL_W);
                pts.add(GY0 + (r + 0.5) * CELL_H);
                int ndr = dr;
                int ndc = dc;
                switch (dir[m]) {
                    case 0: // '-' 가로
                        if (dr != 0) {
                            ndr = -dr;
                        }
                        break;
                    case 2: // '|' 세로
                        if (dc != 0) {
                            ndc = -dc;
                        }
                        break;
                    case 1: // '/'
                        ndr = -dc;
                        ndc = -dr;
                        break;
                    default: // '\'
                        ndr = dc;
                        ndc = dr;
                        break;
                }
                dr = ndr;
                dc = ndc;
            }
            r += dr;
            c += dc;
        }
        beam.getPoints().setAll(pts);
        target = result;
        showTarget();
        return result;
    }

    private void showTarget() {
        actions.getChildren().clear();
        switch (target) {
            case LENS:
                status.setText("빛이 점화 렌즈를 향한다. 이대로 보내면 등불이 다시 켜진다.");
                actions.getChildren().add(button("빛을 보낸다 (등불을 켠다)", () -> confirm(Episode2.ENDING_ON)));
                break;
            case SHUTTER:
                status.setText("빛이 차단 장치를 향한다. 이대로 보내면 등불이 영원히 꺼진다.");
                actions.getChildren().add(button("빛을 보낸다 (등불을 끈다)", () -> confirm(Episode2.ENDING_OFF)));
                break;
            case BACK:
                status.setText("빛이 거울에 맞고 등불 쪽으로 되돌아간다.");
                break;
            default:
                status.setText("빛이 바닥 어딘가에서 흩어진다.");
                break;
        }
    }

    private void confirm(String choice) {
        boolean on = Episode2.ENDING_ON.equals(choice);
        status.setText(on ? "정말 등불을 켜겠는가? 되돌릴 수 없다." : "정말 등불을 끄겠는가? 되돌릴 수 없다.");
        actions.getChildren().setAll(
                button(on ? "켠다" : "끈다", () -> choose(choice)),
                button("다시 생각한다", this::showTarget));
    }

    void choose(String choice) {
        if (closed) {
            return;
        }
        state.finishWithEnding(choice);
        solve();
    }
}

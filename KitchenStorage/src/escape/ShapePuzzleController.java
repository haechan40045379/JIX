package escape;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

/**
 * 퍼즐 3: 도형 맞추기.
 * 조각을 드래그해 같은 모양의 홈 근처에 놓으면 스냅된다. 단, 방향(회전)까지 맞아야 끼워진다.
 * 조각을 클릭(드래그 없이)하거나 우클릭하면 90° 회전한다.
 */
public class ShapePuzzleController implements PuzzleController {

    /** 도형 종류. symmetry = 이 각도만큼 돌려도 같은 모양(회전 정답 판정용). */
    private enum Kind {
        TRIANGLE("삼각형", 360, "#c0694e"),
        SQUARE("사각형", 90, "#6f8fa8"),
        CIRCLE("원", 90, "#d0b060"),
        DIAMOND("마름모", 180, "#7f9e6a");

        final String label;
        final int symmetry;
        final Color color;

        Kind(String label, int symmetry, String color) {
            this.label = label;
            this.symmetry = symmetry;
            this.color = Color.web(color);
        }
    }

    private static final double SNAP_DISTANCE = 28;
    private static final double CLICK_TOLERANCE = 4;
    /** 도형 크기 배율 (그림 속 어두운 홈 높이 약 55px 에 맞춤) */
    private static final double SHAPE_SCALE = 0.5;
    private static final double DRAG_MARGIN = 28;

    // 홈(슬롯) 위치 - 720x432 판에서 그림 속 어두운 가로 홈(y 136~191) 안
    private static final Kind[] SLOT_ORDER = {Kind.DIAMOND, Kind.TRIANGLE, Kind.CIRCLE, Kind.SQUARE};
    private static final double[][] SLOT_POS = {{204, 164}, {306, 164}, {407, 164}, {509, 164}};

    // 조각 시작 위치/회전 - 홈 아래 금속판 (슬롯과 다른 순서, 일부는 돌아간 상태)
    private static final Kind[] PIECE_ORDER = {Kind.SQUARE, Kind.TRIANGLE, Kind.DIAMOND, Kind.CIRCLE};
    private static final double[][] PIECE_HOME = {{204, 262}, {306, 264}, {407, 260}, {509, 262}};
    private static final int[] PIECE_START_ROTATION = {0, 180, 90, 0};

    // 클리어 시 열쇠 조각이 나타나는 위치 (그림 속 걸쇠 오른쪽)
    private static final double[] FRAGMENT_POS = {592, 236};
    private static final double FRAGMENT_SIZE = 56;

    @FXML private Pane board;
    @FXML private ImageView background;
    @FXML private javafx.scene.shape.Rectangle fallbackPlate;
    @FXML private javafx.scene.shape.Rectangle fallbackStrip;
    @FXML private Label infoLabel;
    @FXML private Label progressLabel;
    @FXML private Button resetButton;

    private final List<Piece> pieces = new ArrayList<>();
    private final List<Slot> slots = new ArrayList<>();
    private Runnable onSolved = () -> { };
    private boolean solved;

    /** 홈(슬롯). */
    private static final class Slot {
        final Kind kind;
        final Shape node;
        Piece filledBy;

        Slot(Kind kind, Shape node) {
            this.kind = kind;
            this.node = node;
        }
    }

    /** 움직일 수 있는 조각. */
    private final class Piece {
        final Kind kind;
        final Shape node;
        final double homeX;
        final double homeY;
        final int startRotation;
        int rotation;
        boolean locked;

        private double pressSceneX;
        private double pressSceneY;
        private double pressLayoutX;
        private double pressLayoutY;
        private boolean dragged;

        Piece(Kind kind, double homeX, double homeY, int startRotation) {
            this.kind = kind;
            this.homeX = homeX;
            this.homeY = homeY;
            this.startRotation = startRotation;
            this.node = createShape(kind);
            node.setFill(kind.color);
            node.setStroke(Color.web("#141210"));
            node.setStrokeWidth(3);
            node.setStyle("-fx-cursor: hand;");
            installHandlers();
            resetPosition();
        }

        void resetPosition() {
            locked = false;
            rotation = startRotation;
            node.setRotate(rotation);
            node.setLayoutX(homeX);
            node.setLayoutY(homeY);
            node.setOpacity(1.0);
            node.setStyle("-fx-cursor: hand;");
        }

        boolean rotationMatches() {
            return rotation % kind.symmetry == 0;
        }

        void rotate() {
            rotation = (rotation + 90) % 360;
            node.setRotate(rotation);
        }

        private void installHandlers() {
            node.setOnMousePressed(e -> {
                if (locked || solved) {
                    return;
                }
                node.toFront();
                pressSceneX = e.getSceneX();
                pressSceneY = e.getSceneY();
                pressLayoutX = node.getLayoutX();
                pressLayoutY = node.getLayoutY();
                dragged = false;
                e.consume();
            });

            node.setOnMouseDragged(e -> {
                if (locked || solved || e.getButton() != MouseButton.PRIMARY) {
                    return;
                }
                double dx = e.getSceneX() - pressSceneX;
                double dy = e.getSceneY() - pressSceneY;
                if (Math.abs(dx) > CLICK_TOLERANCE || Math.abs(dy) > CLICK_TOLERANCE) {
                    dragged = true;
                }
                double x = clamp(pressLayoutX + dx, DRAG_MARGIN, board.getPrefWidth() - DRAG_MARGIN);
                double y = clamp(pressLayoutY + dy, DRAG_MARGIN, board.getPrefHeight() - DRAG_MARGIN);
                node.setLayoutX(x);
                node.setLayoutY(y);
                e.consume();
            });

            node.setOnMouseReleased(e -> {
                if (locked || solved) {
                    return;
                }
                if (e.getButton() == MouseButton.SECONDARY || !dragged) {
                    rotate();
                    infoLabel.setText(kind.label + " 조각을 회전했다.");
                } else {
                    tryDrop(this);
                }
                e.consume();
            });
        }
    }

    @FXML
    private void initialize() {
        if (Images.apply(background, "fridge_puzzle_bg.png")) {
            fallbackPlate.setVisible(false);
            fallbackStrip.setVisible(false);
        }

        for (int i = 0; i < SLOT_ORDER.length; i++) {
            Shape s = createShape(SLOT_ORDER[i]);
            s.setFill(Color.web("#14120f"));
            s.setStroke(Color.web("#a8987a"));
            s.setStrokeWidth(2);
            s.getStrokeDashArray().setAll(5.0, 4.0);
            s.setLayoutX(SLOT_POS[i][0]);
            s.setLayoutY(SLOT_POS[i][1]);
            s.setMouseTransparent(true);
            board.getChildren().add(s);
            slots.add(new Slot(SLOT_ORDER[i], s));
        }

        for (int i = 0; i < PIECE_ORDER.length; i++) {
            Piece p = new Piece(PIECE_ORDER[i], PIECE_HOME[i][0], PIECE_HOME[i][1], PIECE_START_ROTATION[i]);
            board.getChildren().add(p.node);
            pieces.add(p);
        }
        updateProgress();
    }

    @Override
    public void setOnSolved(Runnable onSolved) {
        this.onSolved = onSolved;
    }

    private void tryDrop(Piece piece) {
        Slot nearest = null;
        double best = Double.MAX_VALUE;
        for (Slot slot : slots) {
            double d = Math.hypot(piece.node.getLayoutX() - slot.node.getLayoutX(),
                    piece.node.getLayoutY() - slot.node.getLayoutY());
            if (d < best) {
                best = d;
                nearest = slot;
            }
        }

        if (nearest == null || best > SNAP_DISTANCE) {
            infoLabel.setText("홈 근처에 놓아야 끼워진다.");
            return;
        }
        if (nearest.filledBy != null) {
            infoLabel.setText("그 홈은 이미 채워져 있다.");
            returnHome(piece);
            return;
        }
        if (nearest.kind != piece.kind) {
            infoLabel.setText(piece.kind.label + " 조각은 " + nearest.kind.label + " 홈에 맞지 않는다.");
            returnHome(piece);
            return;
        }
        if (!piece.rotationMatches()) {
            infoLabel.setText("모양은 맞는데 방향이 다르다. 클릭해서 돌려보자.");
            returnHome(piece);
            return;
        }

        // 스냅 & 고정
        piece.node.setLayoutX(nearest.node.getLayoutX());
        piece.node.setLayoutY(nearest.node.getLayoutY());
        piece.locked = true;
        piece.node.setStyle("-fx-cursor: default;");
        nearest.filledBy = piece;
        infoLabel.setText(piece.kind.label + " 조각이 딸깍 끼워졌다!");
        updateProgress();
        checkSolved();
    }

    private void returnHome(Piece piece) {
        piece.node.setLayoutX(piece.homeX);
        piece.node.setLayoutY(piece.homeY);
    }

    private void checkSolved() {
        for (Slot slot : slots) {
            if (slot.filledBy == null) {
                return;
            }
        }
        solved = true;
        resetButton.setDisable(true);
        infoLabel.setText("모든 도형이 맞춰졌다! 걸쇠가 풀리며 무언가 떨어진다...");
        showFragment();
        PauseTransition wait = new PauseTransition(Duration.seconds(1.6));
        wait.setOnFinished(e -> onSolved.run());
        wait.play();
    }

    private void updateProgress() {
        long filled = slots.stream().filter(s -> s.filledBy != null).count();
        progressLabel.setText("맞춘 도형: " + filled + " / " + slots.size());
    }

    @FXML
    private void onReset() {
        if (solved) {
            return;
        }
        for (Slot slot : slots) {
            slot.filledBy = null;
        }
        for (Piece p : pieces) {
            p.resetPosition();
        }
        infoLabel.setText("조각을 처음 위치로 되돌렸다.");
        updateProgress();
    }

    /** 걸쇠 옆에 열쇠 조각 그림이 서서히 나타난다 (그림이 없으면 생략). */
    private void showFragment() {
        javafx.scene.image.Image img = Images.load("key_fragment_1.png");
        if (img == null) {
            return;
        }
        ImageView frag = new ImageView(img);
        frag.setFitWidth(FRAGMENT_SIZE);
        frag.setFitHeight(FRAGMENT_SIZE);
        frag.setPreserveRatio(true);
        frag.setLayoutX(FRAGMENT_POS[0]);
        frag.setLayoutY(FRAGMENT_POS[1]);
        frag.setOpacity(0);
        frag.setEffect(new javafx.scene.effect.DropShadow(14, Color.web("#f0c060")));
        board.getChildren().add(frag);
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(Duration.seconds(0.8), frag);
        fade.setToValue(1);
        fade.play();
    }

    /** 원점(0,0)을 중심으로 한 도형(SHAPE_SCALE 배율). layoutX/Y가 곧 중심 좌표가 된다. */
    private static Shape createShape(Kind kind) {
        double k = SHAPE_SCALE;
        switch (kind) {
            case TRIANGLE:
                return new Polygon(0, -42 * k, 46 * k, 34 * k, -46 * k, 34 * k);
            case SQUARE:
                return new Polygon(-36 * k, -36 * k, 36 * k, -36 * k, 36 * k, 36 * k, -36 * k, 36 * k);
            case CIRCLE:
                return new Circle(0, 0, 38 * k);
            case DIAMOND:
            default:
                return new Polygon(0, -50 * k, 32 * k, 0, 0, 50 * k, -32 * k, 0);
        }
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}

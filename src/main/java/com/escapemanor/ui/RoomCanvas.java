package com.escapemanor.ui;

    import java.net.URL;
    import javafx.geometry.Pos;
    import javafx.scene.Cursor;
    import javafx.scene.control.Label;
    import javafx.scene.control.Tooltip;
    import javafx.scene.image.Image;
    import javafx.scene.image.ImageView;
    import javafx.scene.layout.Pane;
    import javafx.scene.layout.Region;
    import javafx.scene.layout.StackPane;

    /** 방 이미지 + 백분율 좌표 클릭 영역. 팀원은 이미지만 바꿔도 UI 크기 변화에 대응 가능. */
    public final class RoomCanvas extends StackPane {
        private final Pane hotspots = new Pane();

        public RoomCanvas(String imagePath, String roomName) {
            getStyleClass().add("room-canvas");
            URL file = RoomCanvas.class.getResource(imagePath);
            if (file != null) {
                Image image = new Image(file.toExternalForm());
                ImageView background = new ImageView(image);
                background.setPreserveRatio(false);
                background.fitWidthProperty().bind(widthProperty());
                background.fitHeightProperty().bind(heightProperty());
                background.setMouseTransparent(true);
                getChildren().add(background);
            } else {
                Label placeholder = new Label(roomName + "\n배경 이미지 연결 대기\n" + imagePath);
                placeholder.getStyleClass().add("room-placeholder");
                placeholder.setAlignment(Pos.CENTER);
                placeholder.setMouseTransparent(true);
                getChildren().add(placeholder);
            }
            hotspots.setPickOnBounds(false);
            hotspots.prefWidthProperty().bind(widthProperty());
            hotspots.prefHeightProperty().bind(heightProperty());
            getChildren().add(hotspots);
        }

        /** 좌표는 0~1.0 비율. 예: 0.4,0.4,0.2,0.3은 중앙 20% 폭 구역. */
        public void addHotspot(double x, double y, double w, double h,
                               String tooltip, Runnable action) {
            Region region = new Region();
            region.getStyleClass().add("room-hotspot");
            region.setCursor(Cursor.HAND);
            region.layoutXProperty().bind(hotspots.widthProperty().multiply(x));
            region.layoutYProperty().bind(hotspots.heightProperty().multiply(y));
            region.prefWidthProperty().bind(hotspots.widthProperty().multiply(w));
            region.prefHeightProperty().bind(hotspots.heightProperty().multiply(h));
            Tooltip.install(region, new Tooltip(tooltip));
            region.setOnMouseClicked(event -> action.run());
            hotspots.getChildren().add(region);
        }
    }

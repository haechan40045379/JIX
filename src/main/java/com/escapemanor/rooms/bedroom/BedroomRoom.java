
package com.escapemanor.rooms.bedroom;
import javafx.scene.control.Button;
import com.escapemanor.core.GameContext;
import com.escapemanor.core.RoomId;
import com.escapemanor.core.RoomModule;
import com.escapemanor.ui.RoomCanvas;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public final class BedroomRoom implements RoomModule {

    private static final String[] CORRECT_ORDER = {
        "죽음", "광대", "은둔자", "마법사", "세계"
    };

    // 그림이 화면에 배치되는 순서
    private static final String[] CARDS = {
        "광대", "마법사", "은둔자", "죽음", "세계"
    };

    @Override
    public RoomId id() {
        return RoomId.BEDROOM;
    }

    


@Override
public Parent createView(GameContext context) {

    RoomCanvas room = new RoomCanvas(id().imagePath(), "침실");

    // 1. 타로 그림 클릭 영역
    room.addHotspot(
        0.28, 0.25, 0.46, 0.33,
        "타로 그림 조사",
        () -> {

            // 퍼즐을 이미 풀었다면 비밀번호 쪽지 표시
            if (context.flag("bedroom.tarot_solved")) {

                openPasswordNote();

            } else {

                boolean noteRead = context.flag("bedroom.note_found");
                boolean bookRead = context.flag("study.tarot_book_read");

                if (noteRead && bookRead) {

                    openTarotPuzzle(context, room);

                } else {

                    context.say("메이저 아르카나로 추정되는 그림이 놓여 있다.");
                    openPaintingWindow();

                }
            }
        }
    );

    // 2. 원형 탁자 클릭 영역
    room.addHotspot(
        0.57, 0.62, 0.10, 0.15,
        "원형 탁자 조사",
        () -> {

            if (!context.flag("bedroom.note_found")) {

                context.setFlag("bedroom.note_found");

                context.say(
                    "영문 모를 내용이 적혀 있다. 어떤 순서인가?"
                );

            } else {

                context.say("탁자 위에는 이미 읽었던 쪽지가 놓여 있다.");

            }

            openHintNote();
        }
    );
    

    return room;
}


    // 처음 그림을 조사했을 때 보여주는 감상용 창
    private void openPaintingWindow() {

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("메이저 아르카나");

        Label title = new Label("벽에 걸린 다섯 장의 그림");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        VBox cardList = new VBox(12);

        for (String card : CARDS) {
            Label label = new Label(card);
            label.setStyle(
                "-fx-font-size: 18px; " +
                "-fx-background-color: #e8dfcf; " +
                "-fx-padding: 12px; " +
                "-fx-background-radius: 8px;"
            );
            label.setMaxWidth(Double.MAX_VALUE);
            label.setAlignment(Pos.CENTER);
            cardList.getChildren().add(label);
        }

        Button closeButton = new Button("닫기");
        closeButton.setOnAction(event -> stage.close());

        VBox layout = new VBox(18, title, cardList, closeButton);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(layout, 350, 450));
        stage.showAndWait();
    }

    // 원형 탁자에서 발견한 힌트 쪽지
    private void openHintNote() {

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("순서에 관한 쪽지");

        Label note = new Label(
            "모든 것을 잃고 다시 눈을 뜬 자여,\n" +
            "고독 속에서 지혜를 갈구하고,\n" +
            "미지를 기지로 바꾸어\n" +
            "여정의 끝에 도달하라."
        );

        note.setWrapText(true);
        note.setAlignment(Pos.CENTER);
        note.setStyle("-fx-font-size: 17px; -fx-line-spacing: 8px;");

        Button closeButton = new Button("쪽지 덮기");
        closeButton.setOnAction(event -> stage.close());

        VBox layout = new VBox(25, note, closeButton);
        layout.setPadding(new Insets(35));
        layout.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(layout, 430, 280));
        stage.showAndWait();
    }

    // 타로 그림 순서 맞추기 퍼즐
    private void openTarotPuzzle(GameContext context, RoomCanvas room) {

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("타로 그림 순서 맞추기");

        Label title = new Label("그림을 쪽지에 적힌 글귀대로 선택해 보자.");
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");

        Label progressLabel = new Label("선택한 그림: 0 / 5");
        Label resultLabel = new Label("올바른 순서대로 선택해 보자.");

        VBox cardButtons = new VBox(10);
        cardButtons.setAlignment(Pos.CENTER);

        int[] progress = {0};

        for (String card : CARDS) {

            Button cardButton = new Button(card);
            cardButton.setPrefWidth(200);
            cardButton.setStyle("-fx-font-size: 16px;");

            cardButton.setOnAction(event -> {

                if (card.equals(CORRECT_ORDER[progress[0]])) {

                    progress[0]++;
                    progressLabel.setText(
                        "선택한 그림: " + progress[0] + " / 5"
                    );

                    resultLabel.setText(card + " 그림을 올바르게 선택했다.");

                    cardButton.setDisable(true);

                    if (progress[0] == CORRECT_ORDER.length) {

                        context.setFlag("bedroom.tarot_solved");

                        resultLabel.setText(
                            "다섯 그림의 순서를 모두 맞췄다!"
                        );

                        cardButtons.setDisable(true);

                        Button finishButton = new Button("완료");
                        finishButton.setOnAction(e -> stage.close());

                        cardButtons.getChildren().add(finishButton);
                    }

                } else {

                    progress[0] = 0;
                    progressLabel.setText("선택한 그림: 0 / 5");

                    resultLabel.setText(
                        "순서가 틀렸다. 처음부터 다시 선택해야 한다."
                    );

                    // 틀리면 모든 그림 버튼 다시 활성화
                    for (var node : cardButtons.getChildren()) {
                        if (node instanceof Button) {
                            node.setDisable(false);
                        }
                    }
                }
            });

            cardButtons.getChildren().add(cardButton);
        }

        VBox layout = new VBox(
            18,
            title,
            progressLabel,
            cardButtons,
            resultLabel
        );

        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(layout, 380, 480));
        stage.showAndWait();

        if (context.flag("bedroom.tarot_solved")) {
            context.say(
                "다섯 그림의 순서가 올바르게 맞춰졌다. " +
                "세계 그림의 액자 아래에서 무언가 떨어졌다."
            );
        }
        
    }

    // 퍼즐 성공 후 발견하는 비밀번호 쪽지
    private void openPasswordNote() {

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("비밀번호 쪽지");

        Label note = new Label(
            "서재 금고의 비밀번호\n\n4961"
        );

        note.setAlignment(Pos.CENTER);
        note.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Button closeButton = new Button("닫기");
        closeButton.setOnAction(event -> stage.close());

        VBox layout = new VBox(25, note, closeButton);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(layout, 300, 200));
        stage.showAndWait();
    }
 // 화면에 보이는 상호작용 버튼 생성

private void addVisibleButton(
        RoomCanvas room,
        double x,
        double y,
        double w,
        double h,
        String text,
        Runnable action) {

    Button button = new Button(text);

    button.setWrapText(true);
    button.setStyle(
        "-fx-font-size: 15px;" +
        "-fx-font-weight: bold;" +
        "-fx-background-color: #e8dfcf;" +
        "-fx-text-fill: #30271f;" +
        "-fx-background-radius: 10px;" +
        "-fx-border-color: #8b7355;" +
        "-fx-border-radius: 10px;"
    );

    button.setCursor(javafx.scene.Cursor.HAND);
    button.setManaged(false);

    // 화면 크기에 맞춰 버튼 위치와 크기 갱신
    Runnable update = () -> {
        button.resize(
            room.getWidth() * w,
            room.getHeight() * h
        );

        button.relocate(
            room.getWidth() * x,
            room.getHeight() * y
        );
    };

    room.widthProperty().addListener(
        (obs, oldValue, newValue) -> update.run()
    );

    room.heightProperty().addListener(
        (obs, oldValue, newValue) -> update.run()
    );

    button.setOnAction(event -> action.run());

    room.getChildren().add(button);

    update.run();
}


}
	
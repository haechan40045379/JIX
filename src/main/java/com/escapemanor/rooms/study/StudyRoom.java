
package com.escapemanor.rooms.study;

import com.escapemanor.core.GameContext;
import com.escapemanor.core.ItemId;
import com.escapemanor.core.RoomId;
import com.escapemanor.core.RoomModule;
import com.escapemanor.ui.RoomCanvas;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public final class StudyRoom implements RoomModule {

    @Override
    public RoomId id() {
        return RoomId.STUDY;
    }


@Override
public Parent createView(GameContext context) {

    RoomCanvas room = new RoomCanvas(id().imagePath(), "서재");

    // 책장 클릭 영역
    room.addHotspot(
        0.20, 0.25, 0.10, 0.18,
        "책장 조사",
        () -> {

            context.say("메이저 아르카나에 대한 설명이 적힌 책을 발견했다.");

            // 책을 읽었다는 진행 상태 저장
            context.setFlag("study.tarot_book_read");

            openTarotBook();
        }
    );

    // 금고 클릭 영역
    room.addHotspot(
    		0.75, 0.45, 0.14, 0.20,
        "금고 조사",
        () -> openSafe(context)
    );

    return room;
}


    // 책장에 있는 타로 설명 책
    private void openTarotBook() {

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("메이저 아르카나 설명서");

        Label title = new Label("메이저 아르카나");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label death = new Label(
            "죽음\n끝과 새로운 시작, 변화와 전환"
        );

        Label fool = new Label(
            "광대\n새로운 출발, 순수함과 가능성"
        );

        Label hermit = new Label(
            "은둔자\n고독, 성찰, 지혜를 찾는 여정"
        );

        Label magician = new Label(
            "마법사\n지식과 능력의 활용, 가능성을 현실로 만드는 힘"
        );

        Label world = new Label(
            "세계\n완성, 성취, 여정의 끝"
        );

        VBox descriptions = new VBox(
            18, death, fool, hermit, magician, world
        );

        for (var node : descriptions.getChildren()) {
            if (node instanceof Label label) {
                label.setStyle(
                    "-fx-font-size: 15px;" +
                    "-fx-background-color: #e8dfcf;" +
                    "-fx-padding: 12px;" +
                    "-fx-background-radius: 8px;"
                );
                label.setMaxWidth(Double.MAX_VALUE);
            }
        }

        Button closeButton = new Button("책 덮기");
        closeButton.setOnAction(event -> stage.close());

        VBox layout = new VBox(
            20, title, descriptions, closeButton
        );

        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(layout, 480, 560));
        stage.showAndWait();
    }

    // 금고 비밀번호 입력
    private void openSafe(GameContext context) {

        if (context.hasItem(ItemId.BEDROOM_STUDY_KEY)) {
            context.say("금고는 이미 열려 있다.");
            return;
        }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("금고");

        Label title = new Label("4자리 비밀번호를 입력하세요.");
        title.setStyle("-fx-font-size: 18px;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("비밀번호 4자리");

        Label result = new Label("");

        Button confirmButton = new Button("확인");

        confirmButton.setOnAction(event -> {

            if ("4961".equals(passwordField.getText())) {

                context.grantItem(ItemId.BEDROOM_STUDY_KEY);

                result.setText("금고가 열렸다! 열쇠를 획득했다.");

                confirmButton.setDisable(true);
                passwordField.setDisable(true);

            } else {

                result.setText("비밀번호가 틀렸다. 다시 입력해 보자.");
                passwordField.clear();
            }
        });

        Button closeButton = new Button("닫기");
        closeButton.setOnAction(event -> stage.close());

        VBox layout = new VBox(
            18, title, passwordField, confirmButton, result, closeButton
        );

        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(layout, 350, 300));
        stage.showAndWait();
    }

}

package escape;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** 결과 화면(탈출 성공 / 게임 오버) 컨트롤러. */
public class ClearController {

    @FXML private Label titleLabel;
    @FXML private Label bodyLabel;
    @FXML private VBox statsBox;
    @FXML private Label gradeLabel;
    @FXML private Label episodeLabel;

    /** 결과 내용 채우기. */
    public void show(GameState state, boolean success, java.util.List<Achievement> newAchievements) {
        boolean ep2 = state.getEpisode() == 2;
        episodeLabel.setText(ep2 ? "EPISODE 02  THE LANTERN" : "THE INITIATE");
        titleLabel.setText(Story.endingTitle(state, success));
        boolean gold = success && !(ep2 && Episode2.ENDING_ON.equals(state.getEnding()));
        titleLabel.setStyle("-fx-text-fill: " + (gold ? "#f0c060" : "#d04a3a")
                + "; -fx-font-size: 36px; -fx-font-weight: bold;");
        bodyLabel.setText(Story.ending(state, success));

        Label[] stats = {
            stat("난이도", state.getDifficulty().label),
            stat("사용한 시간", GameState.formatTime(state.getUsedSeconds())
                    + " / " + GameState.formatTime(state.getTimeLimit())),
            stat("남은 시간", GameState.formatTime(state.getRemainingSeconds())),
            stat("잘못된 시도", state.getPenaltyCount() + "회 (-" + GameState.formatTime(state.getPenaltySeconds()) + ")"),
            stat("사용한 힌트", state.getHintsUsed() + "회"),
            stat("찾은 단서", state.getClues().size() + "개"),
            ep2 ? stat("등불 인장", Episode2.sealsFound(state) + "/3")
                    : stat("열쇠 조각", state.getKeyFragments() + "/" + GameState.TOTAL_KEY_FRAGMENTS),
            stat("방문한 방", state.getVisitedRooms().size() + "/"
                    + (ep2 ? Episode2.ROOMS.size() : Achievement.TOTAL_ROOMS)),
            stat("흑등회 기록", Story.docsFound(state) + "/" + Story.TOTAL_DOCS),
            stat("이전 참가자 쪽지", participantNotes(state) + "개"),
        };
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(36);
        grid.setVgap(4);
        grid.setAlignment(javafx.geometry.Pos.CENTER);
        for (int i = 0; i < stats.length; i++) {
            grid.add(stats[i], i % 2, i / 2);
        }
        statsBox.getChildren().setAll(grid);

        gradeLabel.setText(success ? "평가: " + grade(state) : "평가: FAIL");
        for (Achievement a : newAchievements) {
            Label l = new Label("🏆 새 업적: " + a.title + " - " + a.description);
            l.setStyle("-fx-text-fill: #f0c060; -fx-font-size: 14px;");
            statsBox.getChildren().add(l);
        }
    }

    private static int participantNotes(GameState state) {
        int n = 0;
        for (String c : state.getClues()) {
            if (GameState.isParticipantNote(c)) {
                n++;
            }
        }
        return n;
    }

    /** 남은 시간과 실수 횟수로 등급 계산. */
    static String grade(GameState state) {
        int remain = state.getRemainingSeconds();
        int mistakes = state.getPenaltyCount() + state.getHintsUsed();
        if (remain >= 30 * 60 && mistakes <= 2) {
            return "S";
        }
        if (remain >= 15 * 60 && mistakes <= 5) {
            return "A";
        }
        if (remain >= 5 * 60) {
            return "B";
        }
        return "C";
    }

    private static Label stat(String name, String value) {
        Label l = new Label(name + " : " + value);
        l.setStyle("-fx-text-fill: #d6b46b; -fx-font-size: 15px;");
        return l;
    }

    @FXML
    private void onRetry() {
        Main.restartGame();
    }

    @FXML
    private void onTitle() {
        Main.backToTitle();
    }

    @FXML
    private void onExit() {
        SoundManager.stopBgm();
        Platform.exit();
    }
}

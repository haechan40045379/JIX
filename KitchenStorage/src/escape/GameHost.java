package escape;

import javafx.scene.Node;

/**
 * 방 컨트롤러가 메인 화면에 요청하는 기능(방 전환 / 오버레이 / 메시지).
 * 팀 통합 시 공용 메인 컨트롤러가 이 인터페이스를 구현하면 주방·창고를 그대로 붙일 수 있다.
 */
public interface GameHost {
    String KITCHEN = "KITCHEN";
    String STORAGE = "STORAGE";
    String DINING = "DINING";
    String ENTRANCE = "ENTRANCE";
    String BEDROOM = "BEDROOM";
    String STUDY = "STUDY";

    /** 방 전환. */
    void goTo(String roomId);

    /** 쪽지 등 단순 내용을 오버레이로 띄운다. */
    void showOverlay(String title, Node content);

    /** 퍼즐 FXML을 불러와 오버레이로 띄우고, 클리어 시 onSolved를 호출한다. */
    void openPuzzle(String title, String fxmlName, Runnable onSolved);

    void closeOverlay();

    /** 하단 메시지 줄에 안내 문구 표시. */
    void showMessage(String message);
}

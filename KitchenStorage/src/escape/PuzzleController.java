package escape;

/** 퍼즐 FXML 컨트롤러 공통 인터페이스. */
public interface PuzzleController {

    /** 퍼즐을 다 풀었을 때 호출할 콜백. FXML 로드 직후 설정된다. */
    void setOnSolved(Runnable onSolved);

    /** 오버레이가 닫힐 때 호출(타이머/애니메이션 정리용). */
    default void onClose() {
    }
}

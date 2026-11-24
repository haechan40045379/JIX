package escape;

/** 방(주방/창고) FXML 컨트롤러 공통 인터페이스. FXML 로드 직후 setHost가 호출된다. */
public interface RoomController {
    void setHost(GameHost host);
}

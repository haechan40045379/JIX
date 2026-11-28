package escape;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.ImageView;

/**
 * 에피소드 2 방 컨트롤러 공통 부분.
 * - 배경 그림 붙이기, 상태 변화 시 refresh() 호출
 * - 쓸 수 없는 곳에 아이템을 들고 누르면 내려놓기
 * - 분위기 오브젝트(userData) 처리
 */
abstract class Ep2Room implements RoomController {

    @FXML protected ImageView background;

    protected final GameState state = GameState.get();
    protected GameHost host;

    /** 배경 그림 파일명. */
    protected abstract String backgroundFile();

    @Override
    public void setHost(GameHost host) {
        this.host = host;
        Images.apply(background, backgroundFile());
        state.addListener(this::refresh);
        refresh();
    }

    /** 상태가 바뀔 때마다 호출 (버튼 글자, 그림 바꾸기 등). */
    protected void refresh() {
    }

    /** 아이템을 든 채 쓸 수 없는 곳을 누르면 안내 후 내려놓는다. */
    protected boolean rejectHeldItem() {
        String held = state.getSelectedItem();
        if (held == null) {
            return false;
        }
        host.showMessage("[" + GameState.displayName(held) + "]은(는) 여기에 쓸 수 없다.");
        state.clearSelection();
        return true;
    }

    /** 아이템을 처음 한 번만 얻는다. */
    protected boolean takeOnce(String flag, String item, String message) {
        if (state.hasFlag(flag)) {
            return false;
        }
        state.setFlag(flag);
        state.addItem(item);
        host.showMessage(message + " [" + GameState.displayName(item) + "] 획득!");
        return true;
    }

    @FXML
    protected void onFlavor(ActionEvent event) {
        if (rejectHeldItem()) {
            return;
        }
        Story.flavor(host, ((Node) event.getSource()).getUserData());
    }

    protected void move(String room, String message) {
        host.goTo(room);
        host.showMessage(message);
    }
}

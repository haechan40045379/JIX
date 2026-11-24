package escape;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/** res/images 폴더의 이미지를 ImageView에 붙인다. 파일이 없으면 아무것도 하지 않는다(기본 도형으로 진행). */
public final class Images {
    private Images() {
    }

    private static final Map<String, Image> CACHE = new HashMap<>();

    /** 같은 그림은 한 번만 읽는다. 없으면 null. */
    public static Image load(String fileName) {
        if (CACHE.containsKey(fileName)) {
            return CACHE.get(fileName);
        }
        URL url = Images.class.getResource("/images/" + fileName);
        Image img = url == null ? null : new Image(url.toExternalForm());
        CACHE.put(fileName, img);
        return img;
    }

    /**
     * 이미지를 붙이고, 성공하면 부모(방) 노드에 "with-image" 스타일 클래스를 단다.
     * → game.css 에서 클릭 영역이 투명해지고 마우스를 올릴 때만 보이게 된다.
     */
    public static boolean apply(ImageView view, String fileName) {
        Image img = load(fileName);
        if (img == null) {
            return false;
        }
        view.setImage(img);
        Parent parent = view.getParent();
        if (parent != null && !parent.getStyleClass().contains("with-image")) {
            parent.getStyleClass().add("with-image");
        }
        return true;
    }
}

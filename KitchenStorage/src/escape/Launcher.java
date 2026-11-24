package escape;

/**
 * 이클립스에서 "Run As > Java Application"으로 실행할 진입점.
 * Application을 상속하지 않은 클래스에서 launch하면 VM 인자(--module-path) 없이도
 * 클래스패스의 JavaFX 라이브러리로 실행된다.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}

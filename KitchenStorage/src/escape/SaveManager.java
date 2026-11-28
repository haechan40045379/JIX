package escape;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * 파일 저장 담당 (DB 없이 사용자 폴더의 파일에 저장).
 * - 세이브:  ~/.mystery_story/save.properties   (이어하기)
 * - 업적:    ~/.mystery_story/achievements.txt  (한 줄에 업적 ID 하나)
 * 나중에 DB로 바꾸려면 이 클래스의 메서드 내용만 바꾸면 된다.
 */
public final class SaveManager {

    private static final Path DIR = Path.of(System.getProperty("user.home"), ".mystery_story");
    private static final Path SAVE_FILE = DIR.resolve("save.properties");
    private static final Path ACHIEVEMENT_FILE = DIR.resolve("achievements.txt");

    private SaveManager() {
    }

    // ================= 이어하기 =================
    public static boolean hasSave() {
        return Files.isRegularFile(SAVE_FILE);
    }

    /** 저장된 게임의 에피소드 번호 (없거나 읽을 수 없으면 0). */
    public static int savedEpisode() {
        if (!hasSave()) {
            return 0;
        }
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(SAVE_FILE, StandardCharsets.UTF_8)) {
            p.load(r);
            return Integer.parseInt(p.getProperty("episode", "1"));
        } catch (IOException | RuntimeException e) {
            return 0;
        }
    }

    /** 현재 진행 상황 저장. 실패해도 게임은 계속된다. */
    public static void save(GameState state) {
        if (!state.isPlaying()) {
            return;
        }
        Properties p = new Properties();
        state.writeTo(p);
        try {
            Files.createDirectories(DIR);
            try (Writer w = Files.newBufferedWriter(SAVE_FILE, StandardCharsets.UTF_8)) {
                p.store(w, "MYSTERY STORY save");
            }
        } catch (IOException e) {
            System.err.println("저장 실패: " + e.getMessage());
        }
    }

    /** 세이브 파일을 GameState에 불러온다. 성공하면 true. */
    public static boolean load(GameState state) {
        if (!hasSave()) {
            return false;
        }
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(SAVE_FILE, StandardCharsets.UTF_8)) {
            p.load(r);
            state.readFrom(p);
            return true;
        } catch (IOException | RuntimeException e) {
            System.err.println("불러오기 실패: " + e.getMessage());
            deleteSave();
            return false;
        }
    }

    /** 탈출/게임 오버로 끝난 게임은 이어할 수 없으므로 세이브 삭제. */
    public static void deleteSave() {
        try {
            Files.deleteIfExists(SAVE_FILE);
        } catch (IOException e) {
            System.err.println("세이브 삭제 실패: " + e.getMessage());
        }
    }

    // ================= 업적 =================
    public static Set<String> loadAchievements() {
        Set<String> ids = new LinkedHashSet<>();
        try {
            if (Files.isRegularFile(ACHIEVEMENT_FILE)) {
                for (String line : Files.readAllLines(ACHIEVEMENT_FILE, StandardCharsets.UTF_8)) {
                    if (!line.isBlank()) {
                        ids.add(line.trim());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("업적 불러오기 실패: " + e.getMessage());
        }
        return ids;
    }

    public static void saveAchievements(Set<String> ids) {
        try {
            Files.createDirectories(DIR);
            Files.write(ACHIEVEMENT_FILE, List.copyOf(ids), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("업적 저장 실패: " + e.getMessage());
        }
    }
}

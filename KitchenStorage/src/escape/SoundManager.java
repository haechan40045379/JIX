package escape;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * 효과음 / 배경음 재생. res/sounds 폴더에서 "이름.wav" 또는 "이름.mp3" 를 찾는다.
 * 파일이 없거나 재생에 실패하면 조용히 무시한다(게임은 소리 없이 그대로 진행).
 */
public final class SoundManager {
    private SoundManager() {
    }

    private static final double BGM_VOLUME = 0.35;
    private static final double SFX_VOLUME = 0.8;
    private static final Path SETTINGS = Paths.get(System.getProperty("user.home"),
            ".mystery_story", "settings.properties");

    private static final Map<String, AudioClip> CLIPS = new HashMap<>();
    private static MediaPlayer bgmPlayer;
    private static String bgmName;
    private static Timeline bgmFade;
    private static Boolean muted;

    // ================= 효과음 =================
    /** 효과음을 한 번 재생한다. */
    public static void sfx(String name) {
        sfx(name, 1.0);
    }

    /** 효과음을 한 번 재생한다. volume 은 기본 음량 대비 배율(0~1). */
    public static void sfx(String name, double volume) {
        if (isMuted()) {
            return;
        }
        AudioClip clip = clip(name);
        if (clip != null) {
            try {
                clip.setCycleCount(1);
                clip.play(SFX_VOLUME * volume);
            } catch (RuntimeException ignored) {
                // 재생 실패 → 무음
            }
        }
    }

    /** 효과음을 멈출 때까지 반복 재생한다 (예: 스톱워치 째깍 소리). */
    public static void loopSfx(String name) {
        if (isMuted()) {
            return;
        }
        AudioClip clip = clip(name);
        if (clip != null) {
            try {
                clip.stop();
                clip.setCycleCount(AudioClip.INDEFINITE);
                clip.play(SFX_VOLUME * 0.6);
            } catch (RuntimeException ignored) {
                // 무음
            }
        }
    }

    public static void stopSfx(String name) {
        AudioClip clip = CLIPS.get(name);
        if (clip != null) {
            try {
                clip.stop();
            } catch (RuntimeException ignored) {
                // 무시
            }
        }
    }

    private static AudioClip clip(String name) {
        if (CLIPS.containsKey(name)) {
            return CLIPS.get(name);
        }
        AudioClip clip = null;
        URL url = find(name);
        if (url != null) {
            try {
                clip = new AudioClip(url.toExternalForm());
            } catch (RuntimeException | LinkageError e) {
                clip = null; // 지원하지 않는 형식이거나 javafx.media 모듈이 없음
            }
        }
        CLIPS.put(name, clip);
        return clip;
    }

    // ================= 배경음 =================
    /** 배경음을 바꾼다. 이미 같은 곡이 나오고 있으면 그대로 둔다. 곡은 반복 재생된다. */
    public static void bgm(String name) {
        if (name.equals(bgmName)) {
            return;
        }
        stopBgm();
        bgmName = name;
        if (isMuted()) {
            return;
        }
        URL url = find(name);
        if (url == null) {
            return;
        }
        try {
            MediaPlayer player = new MediaPlayer(new Media(url.toExternalForm()));
            player.setCycleCount(MediaPlayer.INDEFINITE);
            player.setVolume(0);
            player.setOnError(() -> {
                if (bgmPlayer == player) {
                    bgmPlayer = null;
                }
            });
            player.play();
            bgmPlayer = player;
            bgmFade = new Timeline(new KeyFrame(Duration.seconds(1.2),
                    new KeyValue(player.volumeProperty(), BGM_VOLUME)));
            bgmFade.play();
        } catch (RuntimeException | LinkageError e) {
            bgmPlayer = null;
        }
    }

    public static void stopBgm() {
        if (bgmFade != null) {
            bgmFade.stop();
            bgmFade = null;
        }
        if (bgmPlayer != null) {
            try {
                bgmPlayer.stop();
                bgmPlayer.dispose();
            } catch (RuntimeException ignored) {
                // 무시
            }
            bgmPlayer = null;
        }
        bgmName = null;
    }

    // ================= 소리 켜기 / 끄기 (설정 파일에 기억) =================
    public static boolean isMuted() {
        if (muted == null) {
            muted = false;
            Properties p = new Properties();
            if (Files.exists(SETTINGS)) {
                try (InputStream in = Files.newInputStream(SETTINGS)) {
                    p.load(in);
                    muted = Boolean.parseBoolean(p.getProperty("muted", "false"));
                } catch (IOException | IllegalArgumentException ignored) {
                    // 기본값(소리 켜짐)
                }
            }
        }
        return muted;
    }

    public static void setMuted(boolean value) {
        muted = value;
        Properties p = new Properties();
        p.setProperty("muted", String.valueOf(value));
        try {
            Files.createDirectories(SETTINGS.getParent());
            try (OutputStream out = Files.newOutputStream(SETTINGS)) {
                p.store(out, "MYSTERY STORY settings");
            }
        } catch (IOException ignored) {
            // 저장 실패해도 이번 실행에는 적용
        }
        if (value) {
            String current = bgmName;
            stopBgm();
            bgmName = current; // 다시 켜면 같은 곡을 이어서 튼다
            for (AudioClip c : CLIPS.values()) {
                if (c != null) {
                    c.stop();
                }
            }
        } else if (bgmName != null) {
            String current = bgmName;
            bgmName = null;
            bgm(current);
        }
    }

    /** res/sounds 에 그 이름의 파일이 있는가. */
    public static boolean exists(String name) {
        return find(name) != null;
    }

    /** 이름들 중 실제로 있는 첫 파일 이름 (없으면 마지막 이름). 새 사운드가 아직 없을 때 기존 사운드로 대신한다. */
    public static String first(String... names) {
        for (String n : names) {
            if (exists(n)) {
                return n;
            }
        }
        return names[names.length - 1];
    }

    private static URL find(String name) {
        for (String ext : new String[] {".wav", ".mp3"}) {
            URL url = SoundManager.class.getResource("/sounds/" + name + ext);
            if (url != null) {
                return url;
            }
        }
        return null;
    }
}

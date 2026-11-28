package escape;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** 업적 목록과 달성 판정. 달성 기록은 SaveManager 가 파일에 저장한다. */
public enum Achievement {
    FIRST_ESCAPE("첫 탈출", "저택에서 처음으로 탈출했다."),
    NO_MISTAKE("완벽한 참가자", "잘못된 시도 없이 탈출했다."),
    NO_HINT("스스로의 힘으로", "힌트를 한 번도 쓰지 않고 탈출했다."),
    SPEED_RUN("시간의 지배자", "30분 안에 탈출했다."),
    HARD_ESCAPE("시험관도 놀랐다", "어려움 난이도로 탈출했다."),
    EXPLORER("저택 탐험가", "저택의 모든 방을 방문했다."),
    COLLECTOR("기록 수집가", "단서를 10개 이상 모았다."),
    FIRST_FAIL("다시 눈을 뜨다", "처음으로 시험에 실패했다."),
    ARCHIVIST("흑등회의 비밀", "숨겨진 흑등회 기록 6장을 모두 찾았다."),
    TRUE_ENDING("등불을 끈 자", "모든 기록을 읽고 탈출해 진실의 결말을 보았다."),
    EP2_CLEAR("등대에 오르다", "에피소드 2에서 꼭대기 등실의 선택을 마쳤다."),
    EP2_ARCHIVIST("등대의 기록", "등대에 숨겨진 흑등회 기록 6장을 모두 찾았다."),
    EP2_DAWN("새벽", "등불을 끄고 모든 기록을 읽어 진 엔딩을 보았다."),
    EP2_KEEPER("등불지기", "스스로 등불을 다시 켰다.");

    public final String title;
    public final String description;

    Achievement(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public static final int TOTAL_ROOMS = 6;

    /** 게임 도중 달성 가능한 업적 검사 (방 이동, 단서 획득 시). */
    public static List<Achievement> checkDuringPlay(GameState s) {
        List<Achievement> got = new ArrayList<>();
        boolean ep2 = s.getEpisode() == 2;
        if (!ep2 && s.getVisitedRooms().size() >= TOTAL_ROOMS) {
            got.add(EXPLORER);
        }
        if (s.getClues().size() >= 10) {
            got.add(COLLECTOR);
        }
        if (Story.docsFound(s) >= Story.TOTAL_DOCS) {
            got.add(ep2 ? EP2_ARCHIVIST : ARCHIVIST);
        }
        return unlock(got);
    }

    /** 게임이 끝났을 때(탈출/실패) 검사. 새로 달성한 업적만 돌려준다. */
    public static List<Achievement> checkAtEnd(GameState s) {
        List<Achievement> got = new ArrayList<>();
        boolean ep2 = s.getEpisode() == 2;
        if (s.isGameCleared()) {
            got.add(ep2 ? EP2_CLEAR : FIRST_ESCAPE);
            if (s.getPenaltyCount() == 0) {
                got.add(NO_MISTAKE);
            }
            if (s.getHintsUsed() == 0) {
                got.add(NO_HINT);
            }
            if (s.getUsedSeconds() <= 30 * 60) {
                got.add(SPEED_RUN);
            }
            if (s.getDifficulty() == GameState.Difficulty.HARD) {
                got.add(HARD_ESCAPE);
            }
            if (ep2) {
                if (Episode2.ENDING_ON.equals(s.getEnding())) {
                    got.add(EP2_KEEPER);
                } else if (Story.trueEnding(s)) {
                    got.add(EP2_DAWN);
                }
            } else if (Story.trueEnding(s)) {
                got.add(TRUE_ENDING);
            }
        } else if (s.isGameOver()) {
            got.add(FIRST_FAIL);
        }
        List<Achievement> fresh = unlock(got);
        fresh.addAll(0, checkDuringPlay(s));
        return fresh;
    }

    /** 처음 달성한 것만 골라 저장하고 돌려준다. */
    private static List<Achievement> unlock(List<Achievement> candidates) {
        Set<String> owned = SaveManager.loadAchievements();
        List<Achievement> fresh = new ArrayList<>();
        for (Achievement a : candidates) {
            if (owned.add(a.name())) {
                fresh.add(a);
            }
        }
        if (!fresh.isEmpty()) {
            SaveManager.saveAchievements(owned);
        }
        return fresh;
    }

    public static boolean isUnlocked(Achievement a) {
        return SaveManager.loadAchievements().contains(a.name());
    }
}

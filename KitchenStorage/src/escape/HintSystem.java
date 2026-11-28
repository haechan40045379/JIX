package escape;

import java.util.List;

/**
 * 진행 상황에 맞는 힌트를 고른다.
 * 1) 지금 있는 방에서 할 일이 있으면 그 방의 힌트
 * 2) 없으면 다른 방의 다음 할 일을 알려준다.
 */
public final class HintSystem {

    private HintSystem() {
    }

    /** 방 이동 순서 (다른 방 힌트를 찾을 때 이 순서대로 본다). */
    private static final List<String> ROOM_ORDER = List.of(
            GameHost.BEDROOM, GameHost.STUDY, GameHost.ENTRANCE,
            GameHost.DINING, GameHost.KITCHEN, GameHost.STORAGE);

    public static String hintFor(GameState s) {
        if (s.getEpisode() == 2) {
            return Episode2.hintFor(s);
        }
        String here = s.getCurrentRoom();
        if (here != null) {
            String h = roomHint(s, here);
            if (h != null) {
                return h;
            }
        }
        for (String room : ROOM_ORDER) {
            String h = roomHint(s, room);
            if (h != null) {
                return "[" + MapView.roomName(room) + "] " + h;
            }
        }
        return "모든 준비가 끝났다. 현관 홀의 문으로 가자.";
    }

    static String roomHint(GameState s, String room) {
        boolean tarot = s.isCleared(GameState.PUZZLE_TAROT);
        boolean safe = s.isCleared(GameState.PUZZLE_SAFE);
        boolean stopwatch = s.isCleared(GameState.PUZZLE_STOPWATCH);
        boolean sequence = s.isCleared(GameState.PUZZLE_SEQUENCE);
        switch (room) {
            case GameHost.BEDROOM:
                if (!tarot) {
                    return "탁자 위 쪽지의 시는 다섯 타로 그림의 순서를 뜻한다. 서재의 타로 책에서 카드의 의미를 확인해 보자.";
                }
                return null;
            case GameHost.STUDY:
                if (!safe && !tarot) {
                    return "금고 비밀번호는 침실의 타로 그림을 올바른 순서로 고르면 얻을 수 있다.";
                }
                if (!safe) {
                    return "단서 기록에 있는 비밀번호 쪽지를 보고 금고 다이얼을 맞춘 뒤 손잡이를 당기자.";
                }
                return null;
            case GameHost.ENTRANCE:
                if (!s.isCleared(GameState.PUZZLE_FRAME)) {
                    return "거꾸로 걸린 액자가 있다. 클릭해서 바로 세워 보자.";
                }
                if (s.getKeyFragments() >= GameState.TOTAL_KEY_FRAGMENTS) {
                    return "열쇠 조각이 모두 모였다. 현관문을 클릭하자.";
                }
                return null;
            case GameHost.DINING:
                if (!s.isPaintingRestored() && s.hasItem(GameState.PAINTING_FRAGMENT)) {
                    return "찢어진 그림 조각을 인벤토리에서 손에 든 뒤 식당의 찢어진 그림을 클릭하자.";
                }
                if (!s.isPaintingRestored()) {
                    return "식당 그림의 찢어진 부분은 현관 홀 어딘가에 있다.";
                }
                if (!s.isCleared(GameState.PUZZLE_DINING_TABLE)) {
                    return "복원된 그림 속 식기 배치와 똑같이 식탁의 식기를 바꿔 놓아 보자.";
                }
                return null;
            case GameHost.KITCHEN:
                if (!s.isFuseTaken()) {
                    return "주방 수납장 안쪽을 살펴보자.";
                }
                if (s.hasItem(GameState.FRIDGE_KEY)) {
                    return "냉장고 열쇠를 인벤토리에서 손에 든 뒤 냉장고를 클릭하자.";
                }
                if (s.isFridgeUnlocked() && !s.isCleared(GameState.PUZZLE_SHAPE)) {
                    return "도형을 클릭하면 회전한다. 홈과 모양·방향이 맞도록 끌어다 놓자.";
                }
                if (!stopwatch && !s.hasClue(GameState.CLUE_DRIP)) {
                    return "싱크대의 물방울 소리를 들어 보자. 저장고 스톱워치의 목표 시간이 된다.";
                }
                if (!s.isFridgeUnlocked() && !(stopwatch && sequence)) {
                    return "냉장고 열쇠는 지하 저장고의 두 장치를 모두 풀어야 얻을 수 있다.";
                }
                return null;
            case GameHost.STORAGE:
                if (!s.isPanelPowered() && s.hasItem(GameState.FUSE)) {
                    return "퓨즈를 인벤토리에서 손에 든 뒤 순서 패널을 클릭하자.";
                }
                if (!s.isPanelPowered() && !sequence) {
                    return "순서 패널에 전원이 없다. 주방에서 퓨즈를 찾아보자.";
                }
                if (!sequence) {
                    return "패널이 깜빡이는 순서를 기억했다가 같은 순서로 누르자.";
                }
                if (!stopwatch) {
                    return "스톱워치를 5.00초에 멈춰야 한다. 3초가 지나면 숫자가 사라지니 속으로 세자.";
                }
                return null;
            default:
                return null;
        }
    }
}

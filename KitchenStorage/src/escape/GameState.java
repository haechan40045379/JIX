package escape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 공통 게임 상태(싱글턴).
 * - 보유 아이템, 퍼즐 클리어 여부, 단서 기록, 최종 탈출 열쇠 조각 수를 관리한다.
 * - 팀 통합 시에는 keyFragments(열쇠 조각 수)와 방 전환(GameHost.goTo)만 맞추면 된다.
 */
public final class GameState {

    // ---- 아이템 ID ----
    public static final String FRIDGE_KEY = "FRIDGE_KEY";
    public static final String KEY_FRAGMENT_1 = "KEY_FRAGMENT_1";
    public static final String KEY_FRAGMENT_2 = "KEY_FRAGMENT_2";
    public static final String KEY_FRAGMENT_3 = "KEY_FRAGMENT_3";
    public static final String FUSE = "FUSE";
    public static final String PAINTING_FRAGMENT = "PAINTING_FRAGMENT";

    /** 인벤토리 칸 수 (화면에 항상 이만큼 칸이 보인다). */
    public static final int INVENTORY_SLOTS = 6;

    // ---- 단서 ----
    public static final String CLUE_NOTE = "냉장고 열쇠는 창고 깊숙한 곳의 시험을 통과해야 얻을 수 있다.";
    public static final String CLUE_DRIP = "싱크대 물방울은 1초 간격으로 다섯 번 떨어지고 멈춘다.";
    public static final String CLUE_ENTRANCE_MEMO =
            "이 저택에서는 당연한 것조차 의심해야 한다.\n"
            + "거꾸로 걸린 그림을 바로잡아라.\n"
            + "잃어버린 만찬의 일부를 찾을 수 있을 것이다.\n\n"
            + "- 참가자 09";
    public static final String CLUE_DINING_PAINTING =
            "완성된 정물화 속 식기 배치: 접시 왼쪽 나이프, 위쪽 가로 방향 포크, 오른쪽 숟가락, 오른쪽 위 와인잔.";

    // ---- 퍼즐 ID ----
    public static final String PUZZLE_STOPWATCH = "PUZZLE_STOPWATCH";
    public static final String PUZZLE_SEQUENCE = "PUZZLE_SEQUENCE";
    public static final String PUZZLE_SHAPE = "PUZZLE_SHAPE";
    public static final String PUZZLE_FRAME = "PUZZLE_FRAME";
    public static final String PUZZLE_DINING_TABLE = "PUZZLE_DINING_TABLE";
    public static final String PUZZLE_TAROT = "PUZZLE_TAROT";
    public static final String PUZZLE_SAFE = "PUZZLE_SAFE";

    public static final int TOTAL_KEY_FRAGMENTS = 3;

    private static final GameState INSTANCE = new GameState();

    public static GameState get() {
        return INSTANCE;
    }

    private final Set<String> inventory = new LinkedHashSet<>();
    private final Set<String> clearedPuzzles = new LinkedHashSet<>();
    private final List<String> clues = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private boolean fridgeUnlocked;
    private boolean panelPowered;
    private boolean paintingRestored;
    private boolean gameCleared;
    private int keyFragments;
    /** 플레이어가 손에 든(선택한) 아이템. 없으면 null. */
    private String selectedItem;

    private GameState() {
    }

    // ================= 변경 알림 =================
    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    private void fireChanged() {
        for (Runnable r : new ArrayList<>(listeners)) {
            r.run();
        }
    }

    // ================= 아이템 =================
    public boolean hasItem(String itemId) {
        return inventory.contains(itemId);
    }

    public void addItem(String itemId) {
        if (inventory.add(itemId)) {
            fireChanged();
        }
    }

    public boolean removeItem(String itemId) {
        boolean removed = inventory.remove(itemId);
        if (removed) {
            if (itemId.equals(selectedItem)) {
                selectedItem = null;
            }
            fireChanged();
        }
        return removed;
    }

    public Set<String> getInventory() {
        return Collections.unmodifiableSet(inventory);
    }

    // ================= 아이템 선택(손에 들기) =================
    public String getSelectedItem() {
        return selectedItem;
    }

    public boolean isSelected(String itemId) {
        return itemId != null && itemId.equals(selectedItem);
    }

    /** 같은 아이템을 다시 고르면 선택 해제. */
    public void toggleSelect(String itemId) {
        selectedItem = isSelected(itemId) || !hasItem(itemId) ? null : itemId;
        fireChanged();
    }

    public void selectItem(String itemId) {
        selectedItem = hasItem(itemId) ? itemId : null;
        fireChanged();
    }

    public void clearSelection() {
        if (selectedItem != null) {
            selectedItem = null;
            fireChanged();
        }
    }

    public static String displayName(String itemId) {
        switch (itemId) {
            case FRIDGE_KEY:
                return "냉장고 열쇠";
            case KEY_FRAGMENT_1:
                return "탈출 열쇠 조각 1";
            case KEY_FRAGMENT_2:
                return "탈출 열쇠 조각 2";
            case KEY_FRAGMENT_3:
                return "탈출 열쇠 조각 3";
            case FUSE:
                return "낡은 퓨즈";
            case PAINTING_FRAGMENT:
                return "찢어진 그림 조각";
            default:
                return Episode2.itemName(itemId);
        }
    }

    /** 아이템 살펴보기(더블클릭/우클릭) 설명. */
    public static String description(String itemId) {
        switch (itemId) {
            case FRIDGE_KEY:
                return "작업대 아래 상자에서 나온 작은 열쇠. 낡은 꼬리표가 달려 있다. 냉장고 자물쇠에 맞을 것 같다.";
            case KEY_FRAGMENT_1:
                return "화려한 장식이 달린 열쇠의 머리 부분. 날 쪽이 부러져 있다. 나머지 조각도 저택 어딘가에 있을 것이다.";
            case KEY_FRAGMENT_2:
                return "식당 식탁 아래 숨겨진 장치에서 나온 낡은 열쇠 기둥 조각. 양끝이 부러져 있으며 다른 조각과 맞물릴 것 같다.";
            case KEY_FRAGMENT_3:
                return "서재 금고에서 발견한 열쇠의 날 부분. 다른 조각과 맞물릴 것 같다.";
            case FUSE:
                return "도자기 몸통 양 끝에 금속 뚜껑이 달린 낡은 퓨즈. 먼지투성이지만 끊어지지는 않았다.";
            case PAINTING_FRAGMENT:
                return "현관 액자 뒤에서 떨어진 찢어진 그림 조각. 와인잔과 접시, 수저 일부가 그려져 있다.";
            default:
                return Episode2.itemDescription(itemId);
        }
    }

    /** 인벤토리 아이콘 그림 파일명 (res/images). */
    public static String iconFile(String itemId) {
        switch (itemId) {
            case FRIDGE_KEY:
                return "fridge_key.png";
            case KEY_FRAGMENT_1:
                return "key_fragment_1.png";
            case KEY_FRAGMENT_2:
                return "key_fragment_2.png";
            case KEY_FRAGMENT_3:
                return "key_fragment_3.png";
            case FUSE:
                return "fuse.png";
            case PAINTING_FRAGMENT:
                return "painting_fragment.png";
            default:
                return itemId.toLowerCase() + ".png";
        }
    }

    // ================= 퍼즐 =================
    public int getClearedCount() {
        return clearedPuzzles.size();
    }

    public Set<String> getClearedPuzzles() {
        return Collections.unmodifiableSet(clearedPuzzles);
    }

    public boolean isCleared(String puzzleId) {
        return clearedPuzzles.contains(puzzleId);
    }

    public void markCleared(String puzzleId) {
        if (clearedPuzzles.add(puzzleId)) {
            fireChanged();
        }
    }

    /**
     * 창고 퍼즐 두 개(스톱워치, 순서 기억)를 모두 클리어했으면 냉장고 열쇠를 지급한다.
     * @return 이번 호출에서 새로 지급했으면 true
     */
    public boolean tryGiveFridgeKey() {
        if (isCleared(PUZZLE_STOPWATCH) && isCleared(PUZZLE_SEQUENCE)
                && !fridgeUnlocked && !hasItem(FRIDGE_KEY)) {
            addItem(FRIDGE_KEY);
            return true;
        }
        return false;
    }

    // ================= 순서 패널 전원 =================
    public boolean isPanelPowered() {
        return panelPowered;
    }

    /** 퓨즈를 끼워 순서 패널에 전원을 넣는다(퓨즈 소모). */
    public boolean insertFuse() {
        if (panelPowered) {
            return true;
        }
        if (!hasItem(FUSE)) {
            return false;
        }
        inventory.remove(FUSE);
        if (FUSE.equals(selectedItem)) {
            selectedItem = null;
        }
        panelPowered = true;
        fireChanged();
        return true;
    }

    // ================= 냉장고 =================
    public boolean isFridgeUnlocked() {
        return fridgeUnlocked;
    }

    /** 냉장고 열쇠를 사용해 냉장고를 연다(열쇠는 소모). */
    public boolean unlockFridge() {
        if (fridgeUnlocked) {
            return true;
        }
        if (!hasItem(FRIDGE_KEY)) {
            return false;
        }
        inventory.remove(FRIDGE_KEY);
        if (FRIDGE_KEY.equals(selectedItem)) {
            selectedItem = null;
        }
        fridgeUnlocked = true;
        fireChanged();
        return true;
    }

    /** 도형 맞추기 클리어 처리 + 최종 탈출 열쇠 조각 1개 지급. */
    public void clearShapePuzzle() {
        if (isCleared(PUZZLE_SHAPE)) {
            return;
        }
        clearedPuzzles.add(PUZZLE_SHAPE);
        inventory.add(KEY_FRAGMENT_1);
        keyFragments = Math.min(TOTAL_KEY_FRAGMENTS, keyFragments + 1);
        fireChanged();
    }

    /** 현관 홀 액자 회전 퍼즐 클리어 처리 + 찢어진 그림 조각 지급(중복 지급 방지). */
    public void clearFramePuzzle() {
        if (isCleared(PUZZLE_FRAME)) {
            return;
        }
        clearedPuzzles.add(PUZZLE_FRAME);
        inventory.add(PAINTING_FRAGMENT);
        fireChanged();
    }

    // ================= 식당 그림 복원 & 식기 배열 퍼즐 =================
    public boolean isPaintingRestored() {
        return paintingRestored;
    }

    /** 찢어진 그림 조각을 사용해 식당의 작은 그림을 복원한다(조각 소모). */
    public boolean restorePainting() {
        if (paintingRestored) {
            return true;
        }
        if (!hasItem(PAINTING_FRAGMENT)) {
            return false;
        }
        inventory.remove(PAINTING_FRAGMENT);
        if (PAINTING_FRAGMENT.equals(selectedItem)) {
            selectedItem = null;
        }
        paintingRestored = true;
        fireChanged();
        return true;
    }

    /** 식당 식기 배열 퍼즐 클리어 처리 + 최종 탈출 열쇠 조각 2 지급(중복 지급 방지). */
    public void clearDiningTablePuzzle() {
        if (isCleared(PUZZLE_DINING_TABLE)) {
            return;
        }
        clearedPuzzles.add(PUZZLE_DINING_TABLE);
        inventory.add(KEY_FRAGMENT_2);
        keyFragments = Math.min(TOTAL_KEY_FRAGMENTS, keyFragments + 1);
        fireChanged();
    }

    /** 서재 금고를 열고 최종 탈출 열쇠 조각 3을 한 번만 지급한다. */
    public void clearSafePuzzle() {
        if (!clearedPuzzles.add(PUZZLE_SAFE)) {
            return;
        }
        if (inventory.add(KEY_FRAGMENT_3)) {
            keyFragments = Math.min(TOTAL_KEY_FRAGMENTS, keyFragments + 1);
        }
        fireChanged();
    }

    // ================= 단서 기록 =================
    public void addClue(String clue) {
        if (!clues.contains(clue)) {
            clues.add(clue);
            fireChanged();
        }
    }

    public List<String> getClues() {
        return Collections.unmodifiableList(clues);
    }

    public boolean hasClue(String clue) {
        return clues.contains(clue);
    }

    /** 분위기 문장 중 "- 참가자 N" 서명이 있는 것은 이전 참가자의 기록 → 단서로 저장. */
    public static boolean isParticipantNote(String text) {
        return text.contains("- 참가자");
    }

    // ================= 최종 탈출 열쇠 조각 =================
    public int getKeyFragments() {
        return keyFragments;
    }

    /** 팀 통합용: 다른 구역에서 얻은 조각을 더할 때 사용. */
    public void addKeyFragment() {
        keyFragments = Math.min(TOTAL_KEY_FRAGMENTS, keyFragments + 1);
        fireChanged();
    }

    /** 팀 통합용: 공용 카운터 값으로 덮어쓸 때 사용. */
    public void setKeyFragments(int count) {
        keyFragments = Math.max(0, Math.min(TOTAL_KEY_FRAGMENTS, count));
        fireChanged();
    }

    // ================= 제한 시간 / 시간 차감 / 게임 오버 =================
    /** 기본(보통) 제한 시간(초): 60분. 실제 제한 시간은 난이도에 따라 다르다 -> getTimeLimit(). */
    public static final int TIME_LIMIT_SECONDS = 60 * 60;

    /** 난이도: 제한 시간 / 오답 차감 배율 / 힌트 비용(초, -1이면 힌트 불가). */
    public enum Difficulty {
        EASY("쉬움", 90 * 60, 0, 0, "제한시간 90분 · 오답 차감 없음 · 힌트 무료"),
        NORMAL("보통", 60 * 60, 1, 120, "제한시간 60분 · 오답 시 30초~1분 차감 · 힌트 1회당 2분"),
        HARD("어려움", 40 * 60, 2, -1, "제한시간 40분 · 오답 차감 2배 · 힌트 사용 불가");

        public final String label;
        public final int timeLimit;
        public final int penaltyMultiplier;
        public final int hintCost;
        public final String description;

        Difficulty(String label, int timeLimit, int penaltyMultiplier, int hintCost, String description) {
            this.label = label;
            this.timeLimit = timeLimit;
            this.penaltyMultiplier = penaltyMultiplier;
            this.hintCost = hintCost;
            this.description = description;
        }
    }

    private Difficulty difficulty = Difficulty.NORMAL;
    /** 지금 플레이 중인 에피소드 (1: 저택, 2: 등대). */
    private int episode = 1;
    /** 에피소드 2 등에서 쓰는 단순 진행 표시(문 열림, 전원 등). */
    private final Set<String> flags = new LinkedHashSet<>();
    /** 에피소드 2 마지막 선택 (Episode2.ENDING_OFF / ENDING_ON). 없으면 null. */
    private String ending;
    private boolean fuseTaken;
    private String currentRoom;
    private final Set<String> visitedRooms = new LinkedHashSet<>();
    private int hintsUsed;

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getTimeLimit() {
        return difficulty.timeLimit;
    }

    // ---- 에피소드 / 진행 표시 / 엔딩 선택 ----
    public int getEpisode() {
        return episode;
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    public void setFlag(String flag) {
        if (flags.add(flag)) {
            fireChanged();
        }
    }

    public Set<String> getFlags() {
        return Collections.unmodifiableSet(flags);
    }

    public String getEnding() {
        return ending;
    }

    /** 에피소드 2: 마지막 선택으로 게임을 끝낸다. */
    public boolean finishWithEnding(String choice) {
        if (gameCleared || gameOver) {
            return false;
        }
        ending = choice;
        gameCleared = true;
        fireChanged();
        return true;
    }

    // ---- 주방 수납장 퓨즈(패널에 끼우면 인벤토리에서 사라지므로 따로 기억) ----
    public boolean isFuseTaken() {
        return fuseTaken;
    }

    public void takeFuse() {
        if (!fuseTaken) {
            fuseTaken = true;
            inventory.add(FUSE);
            fireChanged();
        }
    }

    // ---- 현재 방 / 방문한 방 (미니맵, 이어하기) ----
    public String getCurrentRoom() {
        return currentRoom;
    }

    public void enterRoom(String roomId) {
        currentRoom = roomId;
        visitedRooms.add(roomId);
    }

    public boolean isVisited(String roomId) {
        return visitedRooms.contains(roomId);
    }

    public Set<String> getVisitedRooms() {
        return Collections.unmodifiableSet(visitedRooms);
    }

    // ---- 힌트 ----
    public int getHintsUsed() {
        return hintsUsed;
    }

    /** 힌트 사용: 난이도에 따른 시간 차감. 사용할 수 없으면 false. */
    public boolean useHint() {
        if (!isPlaying() || difficulty.hintCost < 0) {
            return false;
        }
        hintsUsed++;
        int cut = Math.min(difficulty.hintCost, remainingSeconds);
        remainingSeconds -= cut;
        if (remainingSeconds == 0) {
            gameOver = true;
        }
        fireChanged();
        return true;
    }
    /** 행동별 시간 차감(초). */
    public static final int PENALTY_SMALL = 30;
    public static final int PENALTY_BIG = 60;

    private int remainingSeconds = TIME_LIMIT_SECONDS;
    private int penaltySeconds;
    private int penaltyCount;
    private boolean gameOver;
    private java.util.function.BiConsumer<Integer, String> penaltyListener;

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public int getPenaltySeconds() {
        return penaltySeconds;
    }

    public int getPenaltyCount() {
        return penaltyCount;
    }

    /** 실제로 흐른 시간 + 차감된 시간 = 제한 시간에서 소모한 시간. */
    public int getUsedSeconds() {
        return getTimeLimit() - remainingSeconds;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isPlaying() {
        return !gameOver && !gameCleared;
    }

    /** 차감이 일어날 때 HUD에 알리기 위한 리스너 (초, 이유). */
    public void setPenaltyListener(java.util.function.BiConsumer<Integer, String> listener) {
        this.penaltyListener = listener;
    }

    /** 1초 경과. 일반 리스너는 부르지 않는다(타이머는 MainController가 직접 갱신). */
    public void tick() {
        if (!isPlaying()) {
            return;
        }
        remainingSeconds = Math.max(0, remainingSeconds - 1);
        if (remainingSeconds == 0) {
            gameOver = true;
            fireChanged();
        }
    }

    /** 잘못된 시도 등 행동에 따른 시간 차감. */
    public void penalize(int seconds, String reason) {
        if (!isPlaying()) {
            return;
        }
        seconds *= difficulty.penaltyMultiplier;
        if (seconds <= 0) {
            if (penaltyListener != null) {
                penaltyListener.accept(0, reason);
            }
            return;
        }
        int cut = Math.min(seconds, remainingSeconds);
        remainingSeconds -= cut;
        penaltySeconds += cut;
        penaltyCount++;
        if (penaltyListener != null) {
            penaltyListener.accept(seconds, reason);
        }
        if (remainingSeconds == 0) {
            gameOver = true;
            fireChanged();
        }
    }

    /** 새 게임: 모든 진행 상황과 리스너를 초기화한다. (다시하기 / 타이틀로) */
    public void reset() {
        reset(Difficulty.NORMAL);
    }

    /** 같은 에피소드로 새 게임. */
    public void reset(Difficulty newDifficulty) {
        reset(newDifficulty, episode);
    }

    public void reset(Difficulty newDifficulty, int newEpisode) {
        difficulty = newDifficulty;
        episode = newEpisode;
        flags.clear();
        ending = null;
        fuseTaken = false;
        currentRoom = null;
        visitedRooms.clear();
        hintsUsed = 0;
        inventory.clear();
        clearedPuzzles.clear();
        clues.clear();
        listeners.clear();
        penaltyListener = null;
        fridgeUnlocked = false;
        panelPowered = false;
        paintingRestored = false;
        gameCleared = false;
        gameOver = false;
        keyFragments = 0;
        selectedItem = null;
        remainingSeconds = newDifficulty.timeLimit;
        penaltySeconds = 0;
        penaltyCount = 0;
    }

    public static String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    // ================= 최종 탈출 / 게임 클리어 =================
    public boolean isGameCleared() {
        return gameCleared;
    }

    /**
     * 열쇠 조각을 모두 모았을 때만 게임 클리어 상태로 전환한다.
     * @return 이번 호출에서 새로 클리어되었으면 true
     */
    public boolean clearGame() {
        if (gameCleared || gameOver || keyFragments < TOTAL_KEY_FRAGMENTS) {
            return false;
        }
        gameCleared = true;
        fireChanged();
        return true;
    }

    // ================= 저장 / 불러오기 (파일 입출력은 SaveManager 담당) =================
    void writeTo(java.util.Properties p) {
        p.setProperty("difficulty", difficulty.name());
        p.setProperty("episode", Integer.toString(episode));
        p.setProperty("flags", String.join(",", flags));
        p.setProperty("remainingSeconds", Integer.toString(remainingSeconds));
        p.setProperty("penaltySeconds", Integer.toString(penaltySeconds));
        p.setProperty("penaltyCount", Integer.toString(penaltyCount));
        p.setProperty("hintsUsed", Integer.toString(hintsUsed));
        p.setProperty("keyFragments", Integer.toString(keyFragments));
        p.setProperty("fridgeUnlocked", Boolean.toString(fridgeUnlocked));
        p.setProperty("panelPowered", Boolean.toString(panelPowered));
        p.setProperty("paintingRestored", Boolean.toString(paintingRestored));
        p.setProperty("fuseTaken", Boolean.toString(fuseTaken));
        p.setProperty("currentRoom", currentRoom == null ? "" : currentRoom);
        p.setProperty("inventory", String.join(",", inventory));
        p.setProperty("cleared", String.join(",", clearedPuzzles));
        p.setProperty("visited", String.join(",", visitedRooms));
        p.setProperty("clueCount", Integer.toString(clues.size()));
        for (int i = 0; i < clues.size(); i++) {
            p.setProperty("clue." + i, clues.get(i));
        }
    }

    void readFrom(java.util.Properties p) {
        reset(Difficulty.valueOf(p.getProperty("difficulty", "NORMAL")),
                Integer.parseInt(p.getProperty("episode", "1")));
        splitInto(p.getProperty("flags", ""), flags);
        remainingSeconds = Integer.parseInt(p.getProperty("remainingSeconds", Integer.toString(getTimeLimit())));
        penaltySeconds = Integer.parseInt(p.getProperty("penaltySeconds", "0"));
        penaltyCount = Integer.parseInt(p.getProperty("penaltyCount", "0"));
        hintsUsed = Integer.parseInt(p.getProperty("hintsUsed", "0"));
        keyFragments = Integer.parseInt(p.getProperty("keyFragments", "0"));
        fridgeUnlocked = Boolean.parseBoolean(p.getProperty("fridgeUnlocked"));
        panelPowered = Boolean.parseBoolean(p.getProperty("panelPowered"));
        paintingRestored = Boolean.parseBoolean(p.getProperty("paintingRestored"));
        fuseTaken = Boolean.parseBoolean(p.getProperty("fuseTaken"));
        String room = p.getProperty("currentRoom", "");
        currentRoom = room.isEmpty() ? null : room;
        splitInto(p.getProperty("inventory", ""), inventory);
        splitInto(p.getProperty("cleared", ""), clearedPuzzles);
        splitInto(p.getProperty("visited", ""), visitedRooms);
        int n = Integer.parseInt(p.getProperty("clueCount", "0"));
        for (int i = 0; i < n; i++) {
            String c = p.getProperty("clue." + i);
            if (c != null) {
                clues.add(c);
            }
        }
    }

    private static void splitInto(String csv, Set<String> target) {
        for (String part : csv.split(",")) {
            if (!part.isBlank()) {
                target.add(part.trim());
            }
        }
    }
}

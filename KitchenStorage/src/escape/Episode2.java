package escape;

import java.util.List;

/**
 * 에피소드 2 "THE LANTERN (호수 위의 등대)" 공용 데이터.
 * 방 ID, 아이템, 퍼즐, 진행 표시(flag), 단서 문장, 힌트, 엔딩 문장을 한곳에 모았다.
 * 기획서: 에피소드2_기획서.md
 */
final class Episode2 {
    private Episode2() {
    }

    // ---- 방 ----
    static final String PIER = "EP2_PIER";
    static final String ARCHIVE = "EP2_ARCHIVE";
    static final String OBSERVATION = "EP2_OBSERVATION";
    static final String ENGINE = "EP2_ENGINE";
    static final String LANTERN = "EP2_LANTERN";
    static final List<String> ROOMS = List.of(PIER, ARCHIVE, OBSERVATION, ENGINE, LANTERN);

    static String roomName(String room) {
        switch (room) {
            case PIER: return "선착장";
            case ARCHIVE: return "1층 기록 보관소";
            case OBSERVATION: return "2층 관찰실";
            case ENGINE: return "3층 기관실";
            case LANTERN: return "꼭대기 등실";
            default: return room;
        }
    }

    // ---- 아이템 (아이콘은 소문자 파일명: ep2_uv_flashlight.png ...) ----
    static final String UV_FLASHLIGHT = "EP2_UV_FLASHLIGHT";
    static final String GEAR_SMALL = "EP2_GEAR_SMALL";
    static final String GEAR_MEDIUM = "EP2_GEAR_MEDIUM";
    static final String GEAR_LARGE = "EP2_GEAR_LARGE";
    static final String SEAL_BRONZE = "EP2_SEAL_BRONZE";
    static final String SEAL_SILVER = "EP2_SEAL_SILVER";
    static final String SEAL_GOLD = "EP2_SEAL_GOLD";
    static final List<String> SEALS = List.of(SEAL_BRONZE, SEAL_SILVER, SEAL_GOLD);

    static String itemName(String id) {
        switch (id) {
            case UV_FLASHLIGHT: return "UV 손전등";
            case GEAR_SMALL: return "작은 톱니바퀴";
            case GEAR_MEDIUM: return "중간 톱니바퀴";
            case GEAR_LARGE: return "큰 톱니바퀴";
            case SEAL_BRONZE: return "청동 인장";
            case SEAL_SILVER: return "은 인장";
            case SEAL_GOLD: return "금 인장";
            default: return id;
        }
    }

    static String itemDescription(String id) {
        switch (id) {
            case UV_FLASHLIGHT: return "보랏빛 렌즈가 달린 낡은 손전등. 손에 들고 비추면 보이지 않던 글씨가 드러날지도 모른다.";
            case GEAR_SMALL: return "작은 황동 톱니바퀴. 톱니를 세어 보니 8개다.";
            case GEAR_MEDIUM: return "중간 크기의 황동 톱니바퀴. 톱니가 16개다.";
            case GEAR_LARGE: return "큰 황동 톱니바퀴. 톱니가 32개나 된다.";
            case SEAL_BRONZE: return "등불 문양이 찍힌 청동 인장. 기록 보관소 서랍에서 나왔다.";
            case SEAL_SILVER: return "등불 문양이 찍힌 은 인장. 관찰실 모니터 뒤에 숨겨져 있었다.";
            case SEAL_GOLD: return "등불 문양이 찍힌 금 인장. 기관실 시계 안에서 나왔다.";
            default: return "특별한 점은 없어 보인다.";
        }
    }

    // ---- 퍼즐 ----
    static final String PUZZLE_DIAL = "EP2_DIAL";        // A 모스 신호 → 철문 다이얼
    static final String PUZZLE_CATALOG = "EP2_CATALOG";  // C 참가자 서랍장
    static final String PUZZLE_CIRCUIT = "EP2_CIRCUIT";  // D 회로 타일
    static final String PUZZLE_MONITOR = "EP2_MONITOR";  // E 멈춘 감시 화면
    static final String PUZZLE_GEARS = "EP2_GEARS";      // F 톱니바퀴
    static final String PUZZLE_CLOCK = "EP2_CLOCK";      // G 시계 바늘
    /** 인장을 주는 퍼즐 (HUD 인장 개수). */
    static final List<String> SEAL_PUZZLES = List.of(PUZZLE_CATALOG, PUZZLE_MONITOR, PUZZLE_CLOCK);

    static final String DIAL_CODE = "0113";
    static final int[] DRAWER_ORDER = {7, 9, 12, 1};
    static final int CLOCK_HOUR = 11;
    static final int CLOCK_MINUTE = 40;

    // ---- 진행 표시 ----
    static final String F_UV_TAKEN = "UV_TAKEN";
    static final String F_GEAR_S_TAKEN = "GEAR_S_TAKEN";
    static final String F_GEAR_M_TAKEN = "GEAR_M_TAKEN";
    static final String F_GEAR_L_TAKEN = "GEAR_L_TAKEN";
    static final String F_GEAR_S_PLACED = "GEAR_S_PLACED";
    static final String F_GEAR_M_PLACED = "GEAR_M_PLACED";
    static final String F_GEAR_L_PLACED = "GEAR_L_PLACED";
    static final String F_SEALS_IN = "SEALS_IN";
    static final String F_POWER = "POWER";

    // ---- 엔딩 선택 ----
    static final String ENDING_OFF = "OFF";
    static final String ENDING_ON = "ON";

    static int sealsFound(GameState s) {
        int n = 0;
        for (String id : SEAL_PUZZLES) {
            if (s.isCleared(id)) {
                n++;
            }
        }
        return n;
    }

    static boolean hasAllSeals(GameState s) {
        for (String seal : SEALS) {
            if (!s.hasItem(seal)) {
                return false;
            }
        }
        return true;
    }

    static boolean gearsDone(GameState s) {
        return s.hasFlag(F_GEAR_S_PLACED) && s.hasFlag(F_GEAR_M_PLACED) && s.hasFlag(F_GEAR_L_PLACED);
    }

    // ================= 단서 문장 =================
    static final String INTRO = """
            저택의 문을 연 것으로는 끝나지 않는다.

            등불이 다시 켜지기 전에 등대 꼭대기로 올라와라.
            등불은 오늘 밤 23시 40분에 켜진다.

            길은 내가 남겨 두겠다.

                                                          - 1""";

    static final String MORSE_TABLE = """
            선원 수첩 - 숫자 신호표
            1  ·−−−−      6  −····
            2  ··−−−      7  −−···
            3  ···−−      8  −−−··
            4  ····−      9  −−−−·
            5  ·····      0  −−−−−
            짧은 빛은 점(·), 긴 빛은 선(−).""";

    static final String POST_NOTE = "기둥에 칼로 새긴 글씨: '신호등은 네 개의 숫자를 깜빡인다. 오두막 수첩으로 읽어라. - 참가자 1'";
    static final String UV_WALL_NOTE = "보랏빛 아래 벽에 글씨가 떠오른다: '실패한 자들의 순서로, 마지막은 처음으로. - 참가자 1'";
    static final String UV_DESK_NOTE = "책상 위에 보랏빛 글씨가 떠오른다: '13번 서랍은 너의 것이다. 먼저 열어 보아라. - 참가자 1'";
    static final String DRAWER13_NOTE = """
            서랍 13 - 당신의 파일. 사진 한 장과 시험관 일지 사본이 들어 있다.
            참가자 7 : 저장고에서 포기.
            참가자 9 : 식당에서 시간 초과.
            참가자 12 : 금고 앞에서 서두르다 실패.
            참가자 13 : 통과. 등대로 이송.""";
    static final String MONITOR_NOTE = "콘솔 위 메모: '녹화된 화면은 시간이 흐르지 않는다. 멈춘 화면 뒤를 보아라. - 참가자 1'";
    static final String CONSOLE_LOG = "관찰실 콘솔 기록지: '자동 점등 예정 23:40. 수동 조작 금지.'";
    static final String SPINDLE_NOTE = "축 옆에 숫자가 새겨져 있다. 위쪽 큰 축 '32', 왼쪽 축 '8', 오른쪽 축 '16'.";

    // ================= 흑등회 기록 (에피소드 2) =================
    /** {제목, 발견 문장, 본문} — Story 가 에피소드에 따라 골라 쓴다. */
    static final String[][] DOCS = {
        {"뱃사공 일지", "오두막 안 낡은 책상 위에 젖은 일지가 펼쳐져 있다.", """
                열세 번, 노를 저어 사람을 실어 날랐다.
                모두 잠들어 있었고, 아무도 돌아가지 않았다.

                오늘 밤 열네 번째 배는 띄우지 않는다.
                밧줄은 내가 끊었다. 미안하다."""},
        {"창립 문서", "책상 서류 더미 맨 아래에 누렇게 바랜 문서가 있다.", """
                흑등회 규약 원본. 1893년, 창립자 서명.
                '등불이 켜져 있는 한, 회는 흩어지지 않는다.'

                마지막 장, 다른 필체:
                '창립자 1931년 사망. 등불은 계속 켠다. 아무에게도 알리지 말 것.'"""},
        {"참가자 1의 파일", "1번 서랍 깊숙이, 인장 아래에 두꺼운 파일이 깔려 있다.", """
                참가자 1. 저택의 문을 처음 연 자.
                회원이 된 뒤 등대 관리 담당으로 배치.

                메모: '관찰실 녹음기를 고치다 알았다.
                시험을 지켜보던 목소리는 테이프였다.
                회장도, 심사위원도 없다. 우리만 있었다.'"""},
        {"테이프 라벨 목록", "녹음기 옆 상자에 같은 글씨체의 라벨이 붙은 테이프들이 있다.", """
                No.1  침실 시험, 통과.
                No.2  서재 시험, 통과.
                No.3  현관 시험, 통과.
                No.4  식당 시험, 통과.
                No.5  주방 시험, 통과.
                No.6  저장고 시험, 통과.
                No.7  참가자 13, 기록으로 남는다.

                모든 테이프에 같은 날짜가 적혀 있다. 오십 년 전."""},
        {"정비 일지", "공구함 바닥에 기름때 묻은 정비 일지가 있다.", """
                등불은 시계 장치에 연결되어 매일 23시 40분에 자동으로 켜진다.
                창립자가 만든 장치. 지금은 아무도 원리를 모른다.

                누구도 끄는 법을 묻지 않았다.
                켜져 있는 것이 당연했으니까."""},
        {"참가자 1의 편지", "의자 위에 봉투가 놓여 있다. 겉봉에 '13에게'.", """
                나는 저택의 등불을 껐지만, 그건 손에 든 작은 불이었다.
                진짜 등불은 여기 있다.

                켜는 것은 기계가 하지만, 끄는 것은 사람만 할 수 있다.
                나는 오십 년 동안 그 사람이 되지 못했다.
                너는 될 수 있기를."""},
    };

    // ================= 엔딩 =================
    static String endingTitle(GameState s, boolean success) {
        if (!success) {
            return "등불이 켜졌다";
        }
        if (ENDING_ON.equals(s.getEnding())) {
            return "엔딩 - 등불지기";
        }
        return Story.trueEnding(s) ? "진 엔딩 - 새벽" : "엔딩 - 꺼진 불";
    }

    static String ending(GameState s, boolean success) {
        if (!success) {
            return "23시 40분. 머리 위에서 기계가 돌고, 등불이 저절로 켜진다.\n"
                    + "스피커에서 낡은 목소리가 흘러나온다. \"참가자 13, 등불지기가 된다.\"";
        }
        if (ENDING_ON.equals(s.getEnding())) {
            return "빛이 점화 렌즈를 지나 호수를 가로지른다. 저택의 창문마다 불이 켜진다.\n"
                    + "당신은 관찰실 녹음기 앞에 앉아 마이크에 대고 천천히 말한다.\n"
                    + "\"...주방 시험, 통과.\"";
        }
        if (Story.trueEnding(s)) {
            return "차단 장치가 닫히고, 오십 년 동안 꺼지지 않던 등불이 꺼진다.\n"
                    + "새벽이 오자 안개가 걷히고, 저택의 문이 하나둘 열린다.\n"
                    + "선착장에서 노인이 웃는다. \"열세 번째에서야 끝났군.\"";
        }
        return "등불이 꺼지고 호수가 어둠에 잠긴다.\n"
                + "그런데 멀리, 다른 호숫가에서 새 등불 하나가 켜진다.\n"
                + "흑등회는 하나가 아니었다.  (흑등회 기록 " + Story.docsFound(s) + "/" + Story.TOTAL_DOCS
                + " - 모두 찾으면 다른 결말이 열린다)";
    }

    /** 엔딩 그림 (없으면 null). */
    static String endingImage(GameState s, boolean success) {
        if (success && ENDING_OFF.equals(s.getEnding()) && Story.trueEnding(s)) {
            return "ep2_ending_true.png";
        }
        if (!success || ENDING_ON.equals(s.getEnding())) {
            return "ep2_ending_bad.png";
        }
        return "ep2_card.png";
    }

    // ================= 힌트 =================
    static String hintFor(GameState s) {
        String here = s.getCurrentRoom();
        if (here != null) {
            String h = roomHint(s, here);
            if (h != null) {
                return h;
            }
        }
        for (String room : ROOMS) {
            String h = roomHint(s, room);
            if (h != null) {
                return "[" + roomName(room) + "] " + h;
            }
        }
        return "모든 준비가 끝났다. 꼭대기 등실의 렌즈로 가자.";
    }

    static String roomHint(GameState s, String room) {
        switch (room) {
            case PIER:
                if (!s.isCleared(PUZZLE_DIAL)) {
                    return s.hasClue(MORSE_TABLE)
                            ? "신호등을 눌러 깜빡임을 보고, 수첩의 신호표로 숫자 네 개를 읽어 철문 다이얼에 넣자."
                            : "오두막 벽에 걸린 선원 수첩에 신호를 읽는 방법이 있다.";
                }
                if (!s.hasFlag(F_UV_TAKEN)) {
                    return "어구 상자 안을 살펴보자.";
                }
                if (!s.hasFlag(F_GEAR_S_TAKEN)) {
                    return "바닥에 놓인 닻 밑을 들춰 보자.";
                }
                return null;
            case ARCHIVE:
                if (!s.isCleared(PUZZLE_CATALOG)) {
                    if (!s.hasClue(UV_WALL_NOTE)) {
                        return s.hasItem(UV_FLASHLIGHT)
                                ? "UV 손전등을 손에 든 채 오른쪽 빈 벽을 비춰 보자."
                                : "선착장 어구 상자에 손전등이 있다.";
                    }
                    if (!s.hasClue(DRAWER13_NOTE)) {
                        return "서랍장을 열고 맨 아래 13번 서랍부터 살펴보자.";
                    }
                    return "벽 글씨대로 실패한 참가자 번호 순서(7, 9, 12)로 서랍을 열고, 마지막은 처음(1)이다.";
                }
                if (!s.hasFlag(F_GEAR_M_TAKEN)) {
                    return "서랍장 아래 틈을 살펴보자.";
                }
                return null;
            case OBSERVATION:
                if (!s.hasFlag(F_POWER)) {
                    return "방에 전원이 없다. 왼쪽 벽 배전반의 타일을 돌려 왼쪽 전원과 오른쪽 출력 단자를 이어 보자.";
                }
                if (!s.isCleared(PUZZLE_MONITOR)) {
                    return "모니터 여섯 대 중 하나만 시계가 멈춰 있다. 몇 초 동안 지켜보고 멈춘 화면을 누르자.";
                }
                return null;
            case ENGINE:
                if (!s.hasFlag(F_GEAR_L_TAKEN)) {
                    return "바닥의 공구함을 열어 보자.";
                }
                if (!gearsDone(s)) {
                    return "축 옆에 새겨진 숫자는 톱니 개수다. 톱니를 손에 들고 맞는 축을 누르자. (위 32 · 왼쪽 8 · 오른쪽 16)";
                }
                if (!s.isCleared(PUZZLE_GEARS)) {
                    return "톱니를 모두 끼웠다. 기계 오른쪽 손잡이를 돌려 시계 장치를 움직이자.";
                }
                if (!s.isCleared(PUZZLE_CLOCK)) {
                    return "시계는 등불이 켜지는 시각에 맞춰야 한다. 관찰실 콘솔 기록에 그 시각이 있다.";
                }
                if (!s.hasFlag(F_SEALS_IN)) {
                    return hasAllSeals(s) ? "인장 세 개가 모였다. 계단 위 철문의 구멍에 끼우자."
                            : "인장은 기록 보관소 서랍, 관찰실 모니터, 기관실 시계에서 하나씩 얻는다.";
                }
                return null;
            case LANTERN:
                if (s.hasFlag(F_SEALS_IN)) {
                    return "렌즈를 누르면 거울 바닥이 나온다. 거울을 돌려 빛을 점화 렌즈(등불을 켠다) 또는 차단 장치(등불을 끈다)로 보내자.";
                }
                return null;
            default:
                return null;
        }
    }
}

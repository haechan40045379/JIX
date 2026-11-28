package escape;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * 에피소드 1 "THE INITIATE" 스토리.
 * 플레이어는 비밀 조직 "흑등회"의 열세 번째 참가자. 저택 곳곳에 흑등회의 기록 6장이 숨겨져 있고,
 * 이전 참가자들이 남긴 쪽지("- 참가자 N")가 길을 알려준다. 기록을 모두 찾으면 결말이 달라진다.
 *
 * 방 FXML 의 분위기용 버튼 userData 에 "DOC:번호" 를 적으면 그 기록을 읽는 버튼이 된다.
 */
final class Story {
    private Story() {
    }

    static final String DOC_PREFIX = "[흑등회 기록 ";
    static final int TOTAL_DOCS = 6;

    static final String INTRO = """
            당신은 흑등회(黑燈會)의 열세 번째 참가자로 선택되었습니다.

            이 저택의 여섯 방에는 각각의 시험이 준비되어 있습니다.
            시험을 통과해 열쇠 조각 세 개를 모으고, 현관문을 스스로 여십시오.
            누구도 대신 문을 열어주지 않습니다.

            제한 시간을 넘긴 참가자는 '기록'으로만 남게 됩니다.

                                                          - 흑등회""";

    /** 기록 1~6: {제목, 발견 문장, 본문}. */
    private static final String[][] DOCS = {
        {"참가자 기록 카드", "협탁 서랍 안쪽에 두꺼운 카드 한 장이 붙어 있다.", """
                참가자 번호 : 13
                선발 사유 : 호기심이 강함. 초대장을 의심하지 않음.
                관찰 기간 : 3개월
                이송 : 수면제 투여 후 2층 침실로 이송 완료.

                비고 : 이번 참가자는 겁이 없다. 시험관의 기대가 크다."""},
        {"흑등회 규약", "협탁 위 작은 상자 안에 접힌 종이가 들어 있다.", """
                제1조. 시험은 저택의 여섯 방에서 치러진다.
                제2조. 참가자는 스스로 문을 열어야 한다.
                제3조. 시험 중 회원은 절대 모습을 드러내지 않는다.
                제4조. 시험을 통과한 자는 등불을 받고 회원이 된다.
                제5조. 회원은 저택을 떠날 수 없다."""},
        {"만찬 기록", "식기장 위 나무 상자 안에 오래된 장부가 있다.", """
                시험을 통과한 자는 이 식탁에서 회원들과 첫 식사를 한다.

                1회 만찬 - 참석 회원 1명
                ...
                12회 만찬 - 열리지 않음

                의자는 언제나 하나가 비어 있다. 다음 참가자의 자리다."""},
        {"시험관 일지", "책상 위에 펼쳐진 일지. 잉크가 아직 마르지 않았다.", """
                참가자 7 : 저장고에서 포기.
                참가자 9 : 식당에서 시간 초과.
                참가자 12 : 금고 앞에서 서두르다 실패.

                참가자 13 : 이전 참가자들의 쪽지를 따라가고 있다.
                쪽지를 지우지 않은 것은 실수였을까, 아니면 바람이었을까."""},
        {"주방 담당 회원의 메모", "양철통 뒤에 접힌 메모가 숨겨져 있다.", """
                국자는 내가 씻었다. 참가자가 눈치채지 않기를.
                냉장고 열쇠는 저장고 상자에 넣어 두었다.

                제3조를 잊지 말 것.
                시험 중에는 절대 모습을 드러내지 않는다."""},
        {"시험관의 마지막 기록", "'반출 금지' 상자 틈에 낡은 수첩이 끼어 있다.", """
                나도 한때 참가자였다. 번호는 1.
                이 저택의 현관문을 처음으로 연 사람.

                문 밖에서 기다리는 것은 자유가 아니라 등불이었다.
                그 뒤로 나는 열두 번, 다른 사람이 문 앞에 서는 것을 지켜보았다.

                열세 번째 참가자여. 문을 열어도 등불은 받지 마라."""},
    };

    /** 지금 에피소드의 기록 6장. */
    private static String[][] docs() {
        return GameState.get().getEpisode() == 2 ? Episode2.DOCS : DOCS;
    }

    static String docText(int n) {
        String[] d = docs()[n - 1];
        return DOC_PREFIX + n + "/" + TOTAL_DOCS + "] " + d[0] + "\n\n" + d[2];
    }

    static boolean isDoc(String clue) {
        return clue.startsWith(DOC_PREFIX);
    }

    static int docsFound(GameState s) {
        int n = 0;
        for (String c : s.getClues()) {
            if (isDoc(c)) {
                n++;
            }
        }
        return n;
    }

    /**
     * 분위기용 버튼(userData) 공통 처리.
     * "DOC:n" → 흑등회 기록 읽기, "- 참가자 N" 서명 → 단서 저장, 그 외 → 문장 표시.
     */
    static void flavor(GameHost host, Object userData) {
        GameState state = GameState.get();
        if (userData == null) {
            host.showMessage("특별한 것은 없다.");
            return;
        }
        String text = userData.toString();
        if (text.startsWith("DOC:")) {
            readDoc(host, Integer.parseInt(text.substring(4).trim()));
        } else if (GameState.isParticipantNote(text)) {
            boolean known = state.hasClue(text);
            state.addClue(text);
            host.showMessage(text + (known ? "" : " (단서 기록에 추가됨)"));
        } else {
            host.showMessage(text);
        }
    }

    static void readDoc(GameHost host, int n) {
        GameState state = GameState.get();
        String text = docText(n);
        boolean known = state.hasClue(text);
        state.addClue(text);
        int found = docsFound(state);

        String[][] docs = docs();
        Label caption = new Label(docs[n - 1][1] + (known ? "" : "  (흑등회 기록 " + found + "/" + TOTAL_DOCS + ")"));
        caption.getStyleClass().add("puzzle-text");
        caption.setWrapText(true);
        caption.setMaxWidth(480);
        Label paper = new Label(docs[n - 1][2]);
        paper.getStyleClass().addAll("note-paper", "story-doc");
        paper.setWrapText(true);
        paper.setMaxWidth(500);
        VBox box = new VBox(14, caption, paper);
        box.setAlignment(Pos.CENTER);
        host.showOverlay("흑등회 기록 " + n + " - " + docs[n - 1][0], box);
        if (!known) {
            host.showMessage("흑등회의 기록을 찾았다. (" + found + "/" + TOTAL_DOCS + ")"
                    + (found == TOTAL_DOCS ? (state.getEpisode() == 2
                            ? " 모든 기록을 모았다. 등불의 진실이 보이기 시작한다."
                            : " 모든 기록을 모았다. 이 저택의 진실이 보이기 시작한다.") : ""));
        }
    }

    /** 퍼즐을 풀었을 때 스피커에서 들리는 시험관의 목소리. 없으면 null. */
    static String voiceFor(String puzzleId, GameState s) {
        if (s.getEpisode() != 1) {
            return null;
        }
        String room = switch (puzzleId) {
            case GameState.PUZZLE_TAROT -> "침실";
            case GameState.PUZZLE_FRAME -> "현관";
            case GameState.PUZZLE_DINING_TABLE -> "식당";
            case GameState.PUZZLE_SAFE -> "서재";
            case GameState.PUZZLE_STOPWATCH, GameState.PUZZLE_SEQUENCE ->
                    s.isCleared(GameState.PUZZLE_STOPWATCH) && s.isCleared(GameState.PUZZLE_SEQUENCE) ? "저장고" : null;
            default -> null; // 주방(도형)은 주방 컨트롤러가 직접 말한다
        };
        if (room == null) {
            return null;
        }
        int passed = 0;
        for (String id : new String[] {GameState.PUZZLE_TAROT, GameState.PUZZLE_FRAME, GameState.PUZZLE_DINING_TABLE,
                GameState.PUZZLE_SAFE, GameState.PUZZLE_SHAPE}) {
            if (s.isCleared(id)) {
                passed++;
            }
        }
        if (s.isCleared(GameState.PUZZLE_STOPWATCH) && s.isCleared(GameState.PUZZLE_SEQUENCE)) {
            passed++;
        }
        return "...어디선가 낮은 목소리가 들린다. \"" + room + " 시험, 통과. (" + passed + "/6)\"";
    }

    static boolean trueEnding(GameState s) {
        return docsFound(s) >= TOTAL_DOCS;
    }

    static String endingTitle(GameState s, boolean success) {
        if (s.getEpisode() == 2) {
            return Episode2.endingTitle(s, success);
        }
        if (!success) {
            return "시험 실패";
        }
        return trueEnding(s) ? "탈출 성공 - 진실" : "탈출 성공";
    }

    static String ending(GameState s, boolean success) {
        if (s.getEpisode() == 2) {
            return Episode2.ending(s, success);
        }
        if (!success) {
            return "제한 시간이 모두 지났다. 스피커에서 목소리가 들린다.\n"
                    + "\"참가자 13, 기록으로 남는다.\"  ...의식이 다시 흐려진다.";
        }
        if (trueEnding(s)) {
            return "현관문 밖, 안개 속에 등불을 든 사람들이 서 있다.\n"
                    + "맨 앞의 노인, 참가자 1이 등불을 내민다. 당신은 고개를 젓는다.\n"
                    + "노인은 처음으로 웃으며 등불을 끈다. 안개 너머로 길이 열린다.";
        }
        return "세 조각의 열쇠가 맞춰지고 현관문이 열렸다.\n"
                + "안개 속에서 등불을 든 사람들이 다가온다. \"축하하네, 열세 번째 회원.\"\n"
                + "(흑등회 기록 " + docsFound(s) + "/" + TOTAL_DOCS + " - 모두 찾으면 다른 결말이 열린다)";
    }
}

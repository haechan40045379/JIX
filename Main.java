import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class Main extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel root = new JPanel(cardLayout);
    private final List<String> textLog = new ArrayList<String>();

    private JTextArea storyText;
    private JPanel choicePanel;
    private String selectedObject = null;
    private String currentText = "중앙방이다. 무엇을 살펴볼까?";
    private final Color dark = new Color(30, 32, 40);
    private final Color panelColor = new Color(43, 46, 57);
    private final Color accent = new Color(218, 190, 137);
    private final Color textColor = new Color(245, 243, 236);

    public Main() {
        super("방탈출(프로토타입)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(980, 680));
        setLocationRelativeTo(null);

        root.add(createTitleScreen(), "title");
        root.add(createScenarioScreen(), "scenario");
        root.add(createGameScreen(), "game");
        setContentPane(root);
        cardLayout.show(root, "title");
    }

    private JPanel createTitleScreen() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("방탈출(프로토타입)", SwingConstants.CENTER);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 34));
        title.setAlignmentX(CENTER_ALIGNMENT);

        JButton startButton = new JButton("게임 시작");
        startButton.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
        startButton.setAlignmentX(CENTER_ALIGNMENT);
        startButton.setMaximumSize(new Dimension(220, 52));
        startButton.addActionListener(e -> cardLayout.show(root, "scenario"));

        panel.add(Box.createVerticalGlue());
        panel.add(title);
        panel.add(Box.createVerticalStrut(32));
        panel.add(startButton);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel createScenarioScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 248, 248));
        panel.setBorder(new EmptyBorder(60, 80, 60, 80));

        JLabel heading = new JLabel("시나리오 선택", SwingConstants.CENTER);
        heading.setFont(new Font("맑은 고딕", Font.BOLD, 30));
        panel.add(heading, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210), 1),
            new EmptyBorder(28, 35, 28, 35)));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel scenarioTitle = new JLabel("독스프(프로토타입)");
        scenarioTitle.setFont(new Font("맑은 고딕", Font.BOLD, 25));
        scenarioTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel details = new JLabel("<html><div style='text-align:center;'>제한 시간: 1시간<br>난이도: 하</div></html>");
        details.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        details.setAlignmentX(CENTER_ALIGNMENT);

        JButton selectButton = new JButton("시나리오 선택");
        selectButton.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        selectButton.setAlignmentX(CENTER_ALIGNMENT);
        selectButton.setMaximumSize(new Dimension(220, 48));
        selectButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                this,
                "‘독스프(프로토타입)’ 시나리오를 시작할까요?",
                "시나리오 시작 확인",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                resetGame();
                cardLayout.show(root, "game");
            }
        });

        content.add(Box.createVerticalGlue());
        content.add(scenarioTitle);
        content.add(Box.createVerticalStrut(22));
        content.add(details);
        content.add(Box.createVerticalStrut(30));
        content.add(selectButton);
        content.add(Box.createVerticalGlue());

        panel.add(content, BorderLayout.CENTER);
        JButton backButton = new JButton("타이틀로 돌아가기");
        backButton.addActionListener(e -> cardLayout.show(root, "title"));
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(backButton);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createGameScreen() {
        JPanel game = new JPanel(new BorderLayout(10, 10));
        game.setBackground(dark);
        game.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(dark);
        header.setBorder(new EmptyBorder(0, 0, 4, 0));

        JLabel timer = new JLabel("남은 시간  01:00:00");
        timer.setForeground(accent);
        timer.setFont(new Font("맑은 고딕", Font.BOLD, 18));

        JLabel gameTitle = new JLabel("독스프(프로토타입)  |  중앙방", SwingConstants.CENTER);
        gameTitle.setForeground(textColor);
        gameTitle.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        JButton menuButton = new JButton("☰ 메뉴");
        menuButton.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        menuButton.addActionListener(e -> showMenuDialog());

        header.add(timer, BorderLayout.WEST);
        header.add(gameTitle, BorderLayout.CENTER);
        header.add(menuButton, BorderLayout.EAST);
        game.add(header, BorderLayout.NORTH);

        JPanel middle = new JPanel(new BorderLayout(10, 0));
        middle.setBackground(dark);

        RoomImagePanel roomImage = new RoomImagePanel();
        middle.add(roomImage, BorderLayout.CENTER);

        JPanel choices = new JPanel(new BorderLayout(0, 8));
        choices.setBackground(panelColor);
        choices.setBorder(new EmptyBorder(12, 12, 12, 12));
        choices.setPreferredSize(new Dimension(250, 100));

        JLabel choiceHeading = new JLabel("선택지");
        choiceHeading.setForeground(accent);
        choiceHeading.setFont(new Font("맑은 고딕", Font.BOLD, 19));
        choices.add(choiceHeading, BorderLayout.NORTH);

        choicePanel = new JPanel();
        choicePanel.setBackground(panelColor);
        choicePanel.setLayout(new BoxLayout(choicePanel, BoxLayout.Y_AXIS));
        JScrollPane choiceScroll = new JScrollPane(choicePanel);
        choiceScroll.setBorder(BorderFactory.createEmptyBorder());
        choiceScroll.getViewport().setBackground(panelColor);
        choiceScroll.getVerticalScrollBar().setUnitIncrement(18);
        choices.add(choiceScroll, BorderLayout.CENTER);
        middle.add(choices, BorderLayout.EAST);

        JPanel dialogue = new JPanel(new BorderLayout(0, 8));
        dialogue.setBackground(panelColor);
        dialogue.setBorder(new EmptyBorder(12, 14, 12, 14));
        dialogue.setPreferredSize(new Dimension(100, 155));

        JLabel dialogueTitle = new JLabel("조사 기록");
        dialogueTitle.setForeground(accent);
        dialogueTitle.setFont(new Font("맑은 고딕", Font.BOLD, 17));

        storyText = new JTextArea(currentText);
        storyText.setEditable(false);
        storyText.setLineWrap(true);
        storyText.setWrapStyleWord(true);
        storyText.setFont(new Font("맑은 고딕", Font.PLAIN, 17));
        storyText.setForeground(textColor);
        storyText.setBackground(panelColor);
        storyText.setBorder(null);

        dialogue.add(dialogueTitle, BorderLayout.NORTH);
        dialogue.add(storyText, BorderLayout.CENTER);
        game.add(middle, BorderLayout.CENTER);
        game.add(dialogue, BorderLayout.SOUTH);

        updateChoicesToMain();
        addLog(currentText);
        return game;
    }

    private class RoomImagePanel extends JPanel {
        private Image image;

        RoomImagePanel() {
            setBackground(new Color(65, 65, 69));
            try {
                image = ImageIO.read(Main.class.getResource("/assets/central_room.png"));
            } catch (IOException | IllegalArgumentException ex) {
                image = null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image == null) {
                g.setColor(Color.WHITE);
                g.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
                g.drawString("중앙방 배경 이미지를 불러오지 못했습니다.", 30, 40);
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int panelW = getWidth();
            int panelH = getHeight();
            double scale = Math.min((double) panelW / image.getWidth(null),
                                    (double) panelH / image.getHeight(null));
            int drawW = (int) (image.getWidth(null) * scale);
            int drawH = (int) (image.getHeight(null) * scale);
            int x = (panelW - drawW) / 2;
            int y = (panelH - drawH) / 2;
            g2.drawImage(image, x, y, drawW, drawH, this);
            g2.dispose();
        }
    }

    private void updateChoicesToMain() {
        choicePanel.removeAll();
        String[] objects = {"각 방의 문", "스프", "전구", "책상", "의자"};
        for (String object : objects) {
            JButton button = createChoiceButton(object);
            button.addActionListener(e -> showObjectOptions(object));
            choicePanel.add(button);
            choicePanel.add(Box.createVerticalStrut(8));
        }
        choicePanel.revalidate();
        choicePanel.repaint();
    }

    private JButton createChoiceButton(String label) {
        JButton button = new JButton(label);
        button.setFont(new Font("맑은 고딕", Font.PLAIN, 17));
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        button.setPreferredSize(new Dimension(205, 46));
        button.setFocusPainted(false);
        return button;
    }

    private void showObjectOptions(String object) {
        selectedObject = object;
        currentText = object + "을(를) 어떻게 할까?";
        storyText.setText(currentText);
        addLog(currentText);

        choicePanel.removeAll();
        JButton inspect = createChoiceButton("조사한다.");
        inspect.addActionListener(e -> {
            currentText = "아직 조사할 수 없다. (개발 중)";
            storyText.setText(currentText);
            addLog(currentText);
        });
        JButton back = createChoiceButton("돌아간다.");
        back.addActionListener(e -> {
            selectedObject = null;
            currentText = "중앙방이다. 무엇을 살펴볼까?";
            storyText.setText(currentText);
            addLog(currentText);
            updateChoicesToMain();
        });
        choicePanel.add(inspect);
        choicePanel.add(Box.createVerticalStrut(8));
        choicePanel.add(back);
        choicePanel.revalidate();
        choicePanel.repaint();
    }

    private void addLog(String text) {
        textLog.add(text);
    }

    private void resetGame() {
        textLog.clear();
        selectedObject = null;
        currentText = "중앙방이다. 무엇을 살펴볼까?";
        if (storyText != null) {
            storyText.setText(currentText);
            updateChoicesToMain();
        }
        addLog(currentText);
    }

    private void showMenuDialog() {
        final JDialog dialog = new JDialog(this, "메뉴", false);
        dialog.setSize(520, 440);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(8, 8));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("맑은 고딕", Font.PLAIN, 15));

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        if (textLog.isEmpty()) {
            logArea.setText("아직 기록이 없습니다.");
        } else {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < textLog.size(); i++) {
                builder.append(i + 1).append(". ").append(textLog.get(i)).append("\n\n");
            }
            logArea.setText(builder.toString());
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
        tabs.addTab("시스템 로그", new JScrollPane(logArea));

        JLabel emptyBag = new JLabel("가방이 비어 있습니다.", SwingConstants.CENTER);
        emptyBag.setFont(new Font("맑은 고딕", Font.PLAIN, 17));
        tabs.addTab("가방", emptyBag);

        JLabel emptyClues = new JLabel("수집한 단서가 없습니다.", SwingConstants.CENTER);
        emptyClues.setFont(new Font("맑은 고딕", Font.PLAIN, 17));
        tabs.addTab("단서", emptyClues);

        dialog.add(tabs, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton returnTitle = new JButton("타이틀로 돌아가기");
        returnTitle.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                dialog,
                "게임을 종료하고 타이틀 화면으로 돌아갈까요?",
                "타이틀로 돌아가기",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                dialog.dispose();
                cardLayout.show(root, "title");
            }
        });
        JButton close = new JButton("닫기");
        close.addActionListener(e -> dialog.dispose());
        buttons.add(returnTitle);
        buttons.add(close);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // 기본 모양을 사용합니다.
            }
            new Main().setVisible(true);
        });
    }
}

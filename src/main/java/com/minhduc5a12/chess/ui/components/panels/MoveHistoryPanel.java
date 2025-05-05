package com.minhduc5a12.chess.ui.components.panels;

import com.minhduc5a12.chess.core.model.BoardState;
import com.minhduc5a12.chess.core.model.ChessMove;
import com.minhduc5a12.chess.history.GameHistoryManager;
import com.minhduc5a12.chess.history.HistoryChangeListener;
import com.minhduc5a12.chess.utils.ImageLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Displays the move history of the chess game with navigation controls.
 */
public class MoveHistoryPanel extends JPanel implements HistoryChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(MoveHistoryPanel.class);
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 50;
    private static final int BUTTON_BORDER_RADIUS = 30;

    private final GameHistoryManager gameHistoryManager;
    private final JPanel moveListPanel;
    private final JScrollPane scrollPane;

    /**
     * Constructs a new MoveHistoryPanel.
     *
     * @param gameHistoryManager The manager providing move history.
     */
    public MoveHistoryPanel(GameHistoryManager gameHistoryManager) {
        this.gameHistoryManager = gameHistoryManager;
        setOpaque(true);
        setBackground(new Color(139, 69, 19));
        setPreferredSize(new Dimension(800, 95));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        moveListPanel = new JPanel();
        moveListPanel.setLayout(new BoxLayout(moveListPanel, BoxLayout.X_AXIS));
        moveListPanel.setOpaque(false);

        scrollPane = new JScrollPane(moveListPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(740, 75));
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(new Color(139, 69, 19));
        scrollPane.setOpaque(true);
        scrollPane.setBackground(new Color(139, 69, 19, 90));

        Image leftArrowImage = ImageLoader.getImage("images/left-arrow.png", 20, 20);
        JButton leftArrow = new RoundButton("");
        leftArrow.setIcon(new ImageIcon(leftArrowImage));
        leftArrow.setBackground(new Color(92, 51, 23));
        leftArrow.setFocusPainted(false);
        leftArrow.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        leftArrow.setMinimumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        leftArrow.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        leftArrow.addActionListener(e -> {
            JScrollBar horizontalBar = scrollPane.getHorizontalScrollBar();
            horizontalBar.setValue(horizontalBar.getValue() - 50);
        });

        JButton rightArrow = new RoundButton("");
        Image rightArrowImage = ImageLoader.getImage("images/right-arrow.png", 20, 20);
        rightArrow.setIcon(new ImageIcon(rightArrowImage));
        rightArrow.setBackground(new Color(92, 51, 23));
        rightArrow.setFocusPainted(false);
        rightArrow.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        rightArrow.setMinimumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        rightArrow.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        rightArrow.addActionListener(e -> {
            JScrollBar horizontalBar = scrollPane.getHorizontalScrollBar();
            horizontalBar.setValue(horizontalBar.getValue() + 50);
        });

        MouseAdapter hoverEffect = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                ((JButton) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ((JButton) e.getSource()).setCursor(Cursor.getDefaultCursor());
            }
        };
        leftArrow.addMouseListener(hoverEffect);
        rightArrow.addMouseListener(hoverEffect);

        scrollPane.addMouseWheelListener(e -> {
            int units = e.getWheelRotation() * 20;
            JScrollBar horizontalBar = scrollPane.getHorizontalScrollBar();
            horizontalBar.setValue(horizontalBar.getValue() + units);
        });

        add(Box.createHorizontalGlue());
        add(leftArrow);
        add(Box.createHorizontalStrut(10));
        add(scrollPane);
        add(Box.createHorizontalStrut(10));
        add(rightArrow);
        add(Box.createHorizontalGlue());
        setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        updateMoveHistory();
    }

    /**
     * Updates the move history display based on the undo stack.
     */
    private void updateMoveHistory() {
        moveListPanel.removeAll();
        List<String> moves = getMoveNotations();
        int moveNumber = 1;
        for (int i = 0; i < moves.size(); i += 2) {
            JLabel moveNumberLabel = new JLabel(moveNumber + ".", SwingConstants.RIGHT);
            moveNumberLabel.setFont(new Font("Georgia", Font.PLAIN, 16));
            moveNumberLabel.setForeground(new Color(245, 245, 220));
            moveNumberLabel.setPreferredSize(new Dimension(40, 30));
            moveListPanel.add(moveNumberLabel);
            moveListPanel.add(Box.createHorizontalStrut(5));

            JLabel whiteMoveLabel = new JLabel(moves.get(i), SwingConstants.CENTER);
            whiteMoveLabel.setFont(new Font("Georgia", Font.PLAIN, 16));
            whiteMoveLabel.setForeground(new Color(245, 245, 220));
            whiteMoveLabel.setPreferredSize(new Dimension(80, 30));
            moveListPanel.add(whiteMoveLabel);
            moveListPanel.add(Box.createHorizontalStrut(5));

            if (i + 1 < moves.size()) {
                JLabel blackMoveLabel = new JLabel(moves.get(i + 1), SwingConstants.CENTER);
                blackMoveLabel.setFont(new Font("Georgia", Font.PLAIN, 16));
                blackMoveLabel.setForeground(new Color(245, 245, 220));
                blackMoveLabel.setPreferredSize(new Dimension(80, 30));
                moveListPanel.add(blackMoveLabel);
                moveListPanel.add(Box.createHorizontalStrut(5));
            }

            moveNumber++;
        }

        moveListPanel.revalidate();
        moveListPanel.repaint();
        scrollPane.getHorizontalScrollBar().setValue(scrollPane.getHorizontalScrollBar().getMaximum());
        logger.debug("Updated move history display, moves: {}", moves.size());
    }

    /**
     * Converts the undo stack to a list of move notations.
     *
     * @return A list of move notations from the undo stack.
     */
    private List<String> getMoveNotations() {
        List<String> notations = new ArrayList<>();
        Stack<BoardState> undoStack = gameHistoryManager.getUndoStack();
        for (BoardState state : undoStack) {
            ChessMove move = state.getLastMove();
            if (move != null) {
                notations.add(move.moveNotation());
            }
        }
        return notations;
    }

    @Override
    public void onHistoryChanged() {
        SwingUtilities.invokeLater(this::updateMoveHistory);
        logger.debug("Received history change event, updating move history");
    }

    /**
     * Custom button with rounded corners.
     */
    private static class RoundButton extends JButton {
        public RoundButton(String text) {
            super(text);
            setContentAreaFilled(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isArmed()) {
                g2d.setColor(getBackground().darker());
            } else {
                g2d.setColor(getBackground());
            }
            g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, BUTTON_BORDER_RADIUS, BUTTON_BORDER_RADIUS);
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            // No border
        }

        @Override
        public boolean contains(int x, int y) {
            return new Rectangle(0, 0, getWidth(), getHeight()).contains(x, y);
        }
    }
}
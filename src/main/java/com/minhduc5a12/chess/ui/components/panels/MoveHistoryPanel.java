package com.minhduc5a12.chess.ui.components.panels;

import com.minhduc5a12.chess.core.model.BoardState;
import com.minhduc5a12.chess.core.model.ChessMove;
import com.minhduc5a12.chess.game.ChessController;
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

public class MoveHistoryPanel extends JPanel implements HistoryChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(MoveHistoryPanel.class);
    private static final int BUTTON_WIDTH = 40;
    private static final int BUTTON_HEIGHT = 40;
    private static final int BUTTON_BORDER_RADIUS = 20;
    private static final int PANEL_HEIGHT = 75;
    private static final int STRUT_WIDTH = 3;
    private static final int VIEWPORT_WIDTH = 740;

    private final GameHistoryManager gameHistoryManager;
    private final ChessController chessController;
    private final JPanel moveListPanel;
    private final JScrollPane scrollPane;

    public MoveHistoryPanel(ChessController chessController) {
        this.gameHistoryManager = chessController.getHistoryManager();
        this.chessController = chessController;
        setOpaque(true);
        setBackground(new Color(139, 69, 19));
        setPreferredSize(new Dimension(800, 95));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        moveListPanel = new JPanel();
        moveListPanel.setLayout(new BoxLayout(moveListPanel, BoxLayout.X_AXIS));
        moveListPanel.setOpaque(false);
        moveListPanel.setBackground(new Color(139, 69, 19));

        scrollPane = new JScrollPane(moveListPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(VIEWPORT_WIDTH, PANEL_HEIGHT));
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(new Color(139, 69, 19));
        scrollPane.setOpaque(true);
        scrollPane.setBackground(new Color(139, 69, 19, 90));
        scrollPane.setFocusable(false);
        moveListPanel.setFocusable(false);

        Image leftArrowImage = ImageLoader.getImage("images/left-arrow.png", 15, 15);
        JButton leftArrow = new RoundButton("");
        leftArrow.setIcon(new ImageIcon(leftArrowImage));
        leftArrow.setBackground(new Color(92, 51, 23));
        leftArrow.setFocusPainted(false);
        leftArrow.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        leftArrow.setMinimumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        leftArrow.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        leftArrow.addActionListener(e -> {
            JViewport viewport = scrollPane.getViewport();
            Point viewPosition = viewport.getViewPosition();
            viewPosition.x = Math.max(0, viewPosition.x - 50);
            viewport.setViewPosition(viewPosition);
            logger.debug("Left arrow clicked, view position: {}", viewPosition.x);
        });

        JButton rightArrow = new RoundButton("");
        Image rightArrowImage = ImageLoader.getImage("images/right-arrow.png", 15, 15);
        rightArrow.setIcon(new ImageIcon(rightArrowImage));
        rightArrow.setBackground(new Color(92, 51, 23));
        rightArrow.setFocusPainted(false);
        rightArrow.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        rightArrow.setMinimumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        rightArrow.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        rightArrow.addActionListener(e -> {
            JViewport viewport = scrollPane.getViewport();
            Point viewPosition = viewport.getViewPosition();
            viewPosition.x = Math.min(moveListPanel.getWidth() - viewport.getWidth(), viewPosition.x + 50);
            viewport.setViewPosition(viewPosition);
            logger.debug("Right arrow clicked, view position: {}", viewPosition.x);
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

        add(Box.createHorizontalGlue());
        add(leftArrow);
        add(Box.createHorizontalStrut(5));
        add(scrollPane);
        add(Box.createHorizontalStrut(5));
        add(rightArrow);
        add(Box.createHorizontalGlue());
        setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        updateMoveHistory();
    }

    private void updateMoveHistory() {
        moveListPanel.removeAll();
        List<String> moves = getMoveNotations();
        int totalWidth = 0;
        int moveNumber = 1;

        for (String move : moves) {
            JLabel moveNumberLabel = new JLabel(moveNumber + ".", SwingConstants.RIGHT);
            moveNumberLabel.setFont(new Font("Roboto", Font.BOLD, 16));
            moveNumberLabel.setForeground(new Color(245, 245, 220));
            Dimension numberSize = moveNumberLabel.getPreferredSize();
            moveNumberLabel.setPreferredSize(new Dimension(numberSize.width, 30));
            moveListPanel.add(moveNumberLabel);
            totalWidth += numberSize.width;
            moveListPanel.add(Box.createHorizontalStrut(STRUT_WIDTH));
            totalWidth += STRUT_WIDTH;

            JLabel whiteMoveLabel = new JLabel(move, SwingConstants.CENTER);
            whiteMoveLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
            whiteMoveLabel.setForeground(new Color(245, 245, 220));
            Dimension moveSize = whiteMoveLabel.getPreferredSize();
            whiteMoveLabel.setPreferredSize(new Dimension(moveSize.width, 30));
            moveListPanel.add(whiteMoveLabel);
            totalWidth += moveSize.width;
            moveListPanel.add(Box.createHorizontalStrut(STRUT_WIDTH));
            totalWidth += STRUT_WIDTH;

            moveNumber++;
        }

        final int finalTotalWidth = totalWidth;

        moveListPanel.setPreferredSize(new Dimension(finalTotalWidth, PANEL_HEIGHT));

        logger.debug("Added {} move labels to moveListPanel, total width: {}", moveListPanel.getComponentCount(), finalTotalWidth);
        logger.debug("moveListPanel actual size: {}", moveListPanel.getSize());
        logger.debug("scrollPane viewport size: {}", scrollPane.getViewport().getSize());

        moveListPanel.revalidate();
        moveListPanel.repaint();
        scrollPane.revalidate();
        scrollPane.repaint();

        SwingUtilities.invokeLater(() -> {
            JViewport viewport = scrollPane.getViewport();
            Point viewPosition = new Point(finalTotalWidth - viewport.getWidth(), 0);
            if (viewPosition.x < 0) viewPosition.x = 0;
            viewport.setViewPosition(viewPosition);
            logger.debug("Set viewport position to: {}", viewPosition.x);
            logger.debug("moveListPanel actual size after revalidate: {}", moveListPanel.getSize());
            logger.debug("scrollPane viewport size after revalidate: {}", scrollPane.getViewport().getSize());
        });
    }

    private List<String> getMoveNotations() {
        List<String> notations = new ArrayList<>();
        Stack<BoardState> undoStack = gameHistoryManager.getUndoStack();
        logger.debug("Undo stack size: {}", undoStack.size());
        for (BoardState state : undoStack) {
            ChessMove move = state.getLastMove();
            logger.debug("BoardState move: {}", move);
            if (move != null) {
                notations.add(move.moveNotation());
            }
        }
        ChessMove currentMove = chessController.getBoardManager().getLastMove();
        if (currentMove != null) {
            notations.add(currentMove.moveNotation());
            logger.debug("Added current move: {}", currentMove.moveNotation());
        }
        logger.debug("Move notations: {}", notations);
        return notations;
    }

    @Override
    public void onHistoryChanged() {
        SwingUtilities.invokeLater(() -> {
            updateMoveHistory();
            logger.debug("Received history change event, updated move history");
        });
    }

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
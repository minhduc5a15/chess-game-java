package com.minhduc5a12.chess.ui.components.dialogs;

import com.minhduc5a12.chess.constants.GameMode;
import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.utils.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * A dialog for selecting the chess game mode and player color.
 */
public class GameModeSelectionDialog extends JDialog {
    private int selectedMode = GameMode.PLAYER_VS_PLAYER; // Default mode
    private PieceColor selectedColor = PieceColor.WHITE; // Default color
    private static final int FRAME_WIDTH = 400;
    private static final int FRAME_HEIGHT = 600;

    /**
     * Constructs a new game mode selection dialog.
     *
     * @param parent the parent {@code Frame} for the dialog
     */
    public GameModeSelectionDialog(Frame parent) {
        super(parent, "Select Game Mode", true);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(30, 30, 30));
        setResizable(false);

        // Handle window close event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });

        JPanel roundedPanel = createRoundedPanel();
        roundedPanel.setLayout(new BorderLayout(10, 10));
        roundedPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        roundedPanel.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));

        JLabel messageLabel = new JLabel("Select Game Mode", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 20));
        messageLabel.setForeground(Color.WHITE);
        roundedPanel.add(messageLabel, BorderLayout.NORTH);

        JPanel buttonPanel = createButtonPanel();
        roundedPanel.add(buttonPanel, BorderLayout.CENTER);
        add(roundedPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Creates a rounded panel with a wood texture background.
     *
     * @return the rounded {@code JPanel}
     */
    private JPanel createRoundedPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Image woodTexture = ImageLoader.getImage("images/wood_texture.png", FRAME_WIDTH, FRAME_HEIGHT);
                g2d.drawImage(woodTexture, 0, 0, getWidth(), getHeight(), this);
                g2d.setColor(new Color(80, 40, 20));
                g2d.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));
                g2d.dispose();
            }
        };
    }

    /**
     * Creates a panel containing buttons for game mode selection.
     *
     * @return the button {@code JPanel}
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        JButton pvpButton = createStyledButton("Player vs Player");
        pvpButton.addActionListener(this::onPlayerVsPlayerSelected);
        JButton pvaiButton = createStyledButton("Player vs Bot");
        pvaiButton.addActionListener(this::onPlayerVsAISelected);
        JButton aivaiButton = createStyledButton("Bot vs Bot");
        aivaiButton.addActionListener(this::onAIVsAISelected);

        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(pvpButton);
        pvpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(Box.createVerticalStrut(30));
        buttonPanel.add(pvaiButton);
        pvaiButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(Box.createVerticalStrut(30));
        buttonPanel.add(aivaiButton);
        aivaiButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(Box.createVerticalGlue());

        return buttonPanel;
    }

    /**
     * Creates a styled button with custom appearance.
     *
     * @param text the button text
     * @return the styled {@code JButton}
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getModel().isPressed() ? new Color(92, 51, 23) : getModel().isRollover() ? new Color(160, 82, 45) : new Color(139, 69, 19));
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2d.setColor(Color.WHITE);
                FontMetrics metrics = g2d.getFontMetrics();
                int x = (getWidth() - metrics.stringWidth(getText())) / 2;
                int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
                g2d.drawString(getText(), x, y);
                g2d.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(200, 50);
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };

        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Handles the selection of Player vs. Player mode.
     *
     * @param e the action event
     */
    private void onPlayerVsPlayerSelected(ActionEvent e) {
        selectedMode = GameMode.PLAYER_VS_PLAYER;
        dispose();
    }

    /**
     * Handles the selection of Player vs. AI mode and shows the color selection dialog.
     *
     * @param e the action event
     */
    private void onPlayerVsAISelected(ActionEvent e) {
        selectedMode = GameMode.PLAYER_VS_AI;
        showColorSelectionDialog();
    }

    /**
     * Handles the selection of AI vs. AI mode.
     *
     * @param e the action event
     */
    private void onAIVsAISelected(ActionEvent e) {
        selectedMode = GameMode.AI_VS_AI;
        dispose();
    }

    /**
     * Shows a dialog for selecting the player's color in Player vs. AI mode.
     */
    private void showColorSelectionDialog() {
        ColorSelectionDialog colorDialog = new ColorSelectionDialog(null);
        colorDialog.setVisible(true);
        selectedColor = colorDialog.getSelectedColor();
        dispose();
    }

    /**
     * Gets the selected game mode.
     *
     * @return the selected game mode
     */
    public int getSelectedMode() {
        return selectedMode;
    }

    /**
     * Gets the selected color for the human player in Player vs. AI mode.
     *
     * @return the selected {@code PieceColor}
     */
    public PieceColor getSelectedColor() {
        return selectedColor;
    }

    /**
     * Handles the dialog close event, disposing the dialog and exiting the application.
     */
    public void onExit() {
        dispose();
        System.exit(0);
    }
}
package com.minhduc5a12.chess.ui.components.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.minhduc5a12.chess.constants.GameMode;
import com.minhduc5a12.chess.game.ChessBoard;
import com.minhduc5a12.chess.game.ChessController;
import com.minhduc5a12.chess.game.GameStateListener;
import com.minhduc5a12.chess.ui.components.dialogs.ResignDialog;
import com.minhduc5a12.chess.utils.ImageLoader;

/**
 * A customizable toolbar for the chess game, containing evenly spaced buttons
 * for game actions. Supports dynamic button configuration, state management,
 * and thread-safe actions. Maintains the original UI with a dark background,
 * 24x24 icons, and 50 px height.
 */
public class ChessToolbar extends JPanel implements GameStateListener {

    private static final Logger logger = LoggerFactory.getLogger(ChessToolbar.class);

    private final ChessController chessController;
    private final ChessBoard chessBoard;
    private final List<ButtonConfig> buttonConfigs;
    private final Map<ButtonConfig, JButton> buttonMap;

    // UI constants
    private static final int ICON_SIZE = 24;
    private static final int TOOLBAR_HEIGHT = 50;
    private static final Color BACKGROUND_COLOR = new Color(40, 40, 40);
    private static final int BORDER_SIZE = 5;

    /**
     * Constructs a ChessToolbar with the specified controller and board.
     *
     * @param controller The ChessController managing game logic
     * @param chessBoard The ChessBoard for UI interactions
     */
    public ChessToolbar(ChessController controller, ChessBoard chessBoard) {
        this.chessController = controller;
        this.chessBoard = chessBoard;
        this.buttonConfigs = new ArrayList<>();
        this.buttonMap = new HashMap<>();

        // Initialize UI
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));
        setPreferredSize(new Dimension(0, TOOLBAR_HEIGHT));

        // Initialize buttons and layout
        initializeButtonConfigs();
        updateButtonLayout();

        // Register as listener
        initializeGameStateListener();

        // Log initial state
        logButtonStates();
    }

    /**
     * Initializes the game state listener after the object is fully
     * constructed.
     */
    private void initializeGameStateListener() {
        chessController.addGameStateListener(this);
        logger.debug("ChessToolbar registered as GameStateListener");
    }

    /**
     * Defines the list of buttons and their configurations.
     */
    private void initializeButtonConfigs() {
        // Flip Board button
        buttonConfigs.add(new ButtonConfig("Flip Board", "images/flip-board.png", e -> chessBoard.flipBoard(), () -> true // Always visible
        ));

        // Resign button (not shown in AI versus AI mode)
        buttonConfigs.add(new ButtonConfig("Resign", "images/resign.png", e -> {
            ResignDialog dialog = new ResignDialog((Frame) SwingUtilities.getWindowAncestor(this), "Are you sure you want to resign?");
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                chessController.resignGame();
            }
        }, () -> chessController.getGameMode() != GameMode.AI_VS_AI && !chessController.isGameEnded()));

        // Move Back (Undo) button
        buttonConfigs.add(new ButtonConfig("Move Back", "images/back.png", e -> chessController.undoMove(), () -> !chessController.isGameEnded() && chessController.getHistoryManager() != null));

        // Move Forward (Redo) button
        buttonConfigs.add(new ButtonConfig("Move Forward", "images/forward.png", e -> chessController.redoMove(), () -> !chessController.isGameEnded() && chessController.getHistoryManager() != null));
    }

    /**
     * Updates the layout and buttons based on visible configurations.
     */
    private void updateButtonLayout() {
        // Count visible buttons
        int visibleCount = 0;
        for (ButtonConfig config : buttonConfigs) {
            if (config.isVisible.getAsBoolean()) {
                visibleCount++;
            }
        }

        // Update layout
        removeAll();
        setLayout(new GridLayout(1, visibleCount > 0 ? visibleCount : 1));

        // Clear existing buttons
        buttonMap.clear();

        // Create and add visible buttons
        for (ButtonConfig config : buttonConfigs) {
            if (config.isVisible.getAsBoolean()) {
                JButton button = createButton(config);
                buttonMap.put(config, button);
                add(button);
            }
        }

        // Update button states after layout
        updateButtonStates();

        // Log button states
        logger.debug("Updated toolbar layout: visible buttons = {}", visibleCount);
        logButtonStates();

        revalidate();
        repaint();
    }

    /**
     * Creates a single button with the specified configuration.
     *
     * @param config The button configuration
     * @return The created JButton
     */
    private JButton createButton(ButtonConfig config) {
        Image iconImage = ImageLoader.getImage(config.iconPath, ICON_SIZE, ICON_SIZE);
        JButton button = new JButton(new ImageIcon(iconImage));
        button.setToolTipText(config.tooltip);
        button.setBackground(BACKGROUND_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));
        button.setFocusPainted(false);

        // Wrap action in EDT and state management
        button.addActionListener(e -> {
            if (!button.isEnabled()) {
                return;
            }
            // Disable all buttons during action
            setAllButtonsEnabled(false);
            SwingUtilities.invokeLater(() -> {
                try {
                    config.action.actionPerformed(e);
                } finally {
                    // Update layout after action
                    updateButtonLayout();
                }
            });
        });

        return button;
    }

    /**
     * Updates the enabled state of all buttons based on their conditions.
     */
    public void updateButtonStates() {
        for (ButtonConfig config : buttonConfigs) {
            JButton button = buttonMap.get(config);
            if (button != null) {
                boolean shouldBeEnabled = isButtonEnabled(config);
                button.setEnabled(shouldBeEnabled);
            }
        }
    }

    /**
     * Refreshes the toolbar layout and button states. Called when game state
     * changes via GameStateListener.
     */
    public void refresh() {
        logger.debug("Refreshing toolbar");
        SwingUtilities.invokeLater(() -> {
            updateButtonLayout();
            logButtonStates();
        });
    }

    @Override
    public void onGameStateChanged() {
        logger.debug("Received game state change notification");
        refresh();
    }

    /**
     * Checks if a button should be enabled based on its configuration.
     *
     * @param config The button configuration
     * @return Whether the button should be enabled
     */
    private boolean isButtonEnabled(ButtonConfig config) {
        if (chessController.getHistoryManager() == null) {
            return false;
        }
        if (config.tooltip.equals("Move Back")) {
            return !chessController.getHistoryManager().getUndoStack().isEmpty();
        } else if (config.tooltip.equals("Move Forward")) {
            return !chessController.getHistoryManager().getRedoStack().isEmpty();
        }
        return config.isVisible.getAsBoolean(); // Other buttons use a visibility condition
    }

    /**
     * Sets the enabled state of all buttons.
     *
     * @param enabled Whether buttons should be enabled
     */
    private void setAllButtonsEnabled(boolean enabled) {
        for (JButton button : buttonMap.values()) {
            button.setEnabled(enabled);
        }
    }

    /**
     * Logs the state of all buttons and game conditions for debugging.
     */
    private void logButtonStates() {
        boolean historyManagerExists = chessController.getHistoryManager() != null;
        int undoStackSize = historyManagerExists ? chessController.getHistoryManager().getUndoStack().size() : 0;
        int redoStackSize = historyManagerExists ? chessController.getHistoryManager().getRedoStack().size() : 0;
        boolean gameEnded = chessController.isGameEnded();

        logger.debug("Toolbar state: historyManager = {}, undoStackSize = {}, redoStackSize = {}, gameEnded = {}", historyManagerExists, undoStackSize, redoStackSize, gameEnded);
    }

    /**
     * Configuration for a toolbar button.
     */
    private record ButtonConfig(String tooltip, String iconPath, ActionListener action, java.util.function.BooleanSupplier isVisible) {

    }
}

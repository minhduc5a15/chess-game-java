package com.minhduc5a12.chess.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.minhduc5a12.chess.GameController;
import com.minhduc5a12.chess.constants.PieceColor;

public class Stockfish {
    private static final Logger logger = LoggerFactory.getLogger(Stockfish.class);
    private Process stockfishProcess;
    private BufferedReader reader;
    private BufferedWriter writer;

    public Stockfish() {
        startEngine();
    }

    private void startEngine() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String stockfishFile = os.contains("win") ? "stockfish.exe" : "stockfish";
            String stockfishPath = Objects.requireNonNull(getClass().getClassLoader().getResource(stockfishFile)).getPath();
            ProcessBuilder pb = new ProcessBuilder(stockfishPath);
            pb.redirectErrorStream(true);
            stockfishProcess = pb.start();

            reader = new BufferedReader(new InputStreamReader(stockfishProcess.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(stockfishProcess.getOutputStream()));

            sendCommand("uci");
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("uciok")) break;
            }
        } catch (IOException e) {
            logger.error("Failed to start Stockfish", e);
        }
    }

    public void sendCommand(String command) {
        try {
            writer.write(command + "\n");
            writer.flush();
        } catch (IOException e) {
            logger.error("Failed to send command to Stockfish: {}", command, e);
        }
    }

    public List<String> getOutput() {
        List<String> output = new ArrayList<>();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line);
                if (line.contains("bestmove")) break;
            }
        } catch (IOException e) {
            logger.error("Error reading Stockfish output", e);
        }
        return output;
    }

    public void makeMove(GameController gameController) {
        if (gameController.isGameEnded() || gameController.getCurrentPlayerColor() != PieceColor.BLACK) {
            return;
        }

        String fen = gameController.getNotationUtils().getFen();
        sendCommand("position fen " + fen);
        sendCommand("go depth 25");
        List<String> output = getOutput();
        String bestMove = null;
        for (String line : output) {
            if (line.startsWith("bestmove")) {
                bestMove = line.split(" ")[1];
                logger.info("Stockfish best move for Black: {}", bestMove);
                break;
            }
        }

        if (bestMove != null && bestMove.length() >= 4) {
            int startX = bestMove.charAt(0) - 'a';
            int startY = 7 - (bestMove.charAt(1) - '1');
            int endX = bestMove.charAt(2) - 'a';
            int endY = 7 - (bestMove.charAt(3) - '1');
            String promotion = (bestMove.length() > 4) ? bestMove.substring(4) : null;
            SwingUtilities.invokeLater(() -> gameController.makeMove(startX, startY, endX, endY, promotion));
        }
    }

    public void close() {
        sendCommand("quit");
        try {
            if (stockfishProcess != null) {
                stockfishProcess.waitFor();
            }
            if (reader != null) reader.close();
            if (writer != null) writer.close();
        } catch (InterruptedException | IOException e) {
            logger.error("Error closing Stockfish", e);
        }
    }
}
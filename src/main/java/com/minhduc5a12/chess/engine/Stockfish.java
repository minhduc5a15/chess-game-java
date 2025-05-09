package com.minhduc5a12.chess.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages interaction with the Stockfish chess engine.
 * Provides methods to start the engine, send commands, retrieve moves, and stop the engine.
 */
public class Stockfish {
    private static final Logger logger = LoggerFactory.getLogger(Stockfish.class);
    private Process stockfishProcess;
    private BufferedReader reader;
    private BufferedWriter writer;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Constructs a new Stockfish instance.
     * The engine is not started until the {@link #start()} method is called.
     */
    public Stockfish() {
    }

    /**
     * Starts the Stockfish engine asynchronously.
     * Initializes the engine process and establishes communication channels.
     *
     * @throws RuntimeException if the Stockfish executable is not found or fails to initialize
     */
    public void start() {
        executor.submit(() -> {
            try {
                startEngine();
            } catch (Exception e) {
                logger.error("Error starting Stockfish", e);
            }
        });
    }

    /**
     * Sends a command to the Stockfish engine.
     *
     * @param command the command to send
     * @throws RuntimeException if there is an error sending the command
     */
    public void sendCommand(String command) {
        try {
            logger.debug("Sending command to Stockfish: {}", command);
            writer.write(command + "\n");
            writer.flush();
        } catch (IOException | IllegalStateException e) {
            logger.error("Error sending command to Stockfish: {}", command, e);
            throw new RuntimeException("Error sending command to Stockfish", e);
        }
    }

    /**
     * Retrieves the output from the Stockfish engine until a "bestmove" line is received.
     *
     * @return a list of output lines from the engine
     */
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

    /**
     * Gets the best move from Stockfish for a given board position in FEN notation.
     * Uses a random search depth between 20 and 26.
     *
     * @param fen the board position in FEN notation
     * @return the best move in UCI notation, or null if no move is found
     */
    public String getBestMove(String fen) {
        sendCommand("position fen " + fen);
        Random random = new Random();
        // random depth from 20 to 26
        int depth = random.nextInt(7) + 20;
        sendCommand("go depth " + depth);
        List<String> output = getOutput();
        for (String line : output) {
            if (line.startsWith("bestmove")) {
                String[] parts = line.split(" ");
                return parts[1];
            }
        }
        return null;
    }

    /**
     * Stops the Stockfish engine and releases resources.
     */
    public void stopEngine() {
        if (stockfishProcess != null) {
            sendCommand("quit");
            stockfishProcess.destroy();
            try {
                stockfishProcess.waitFor();
            } catch (InterruptedException e) {
                logger.error("Error stopping Stockfish", e);
            }
        }
        executor.shutdown();
    }

    /**
     * Initializes the Stockfish engine process and communication channels.
     * Detects the operating system to select the appropriate executable.
     *
     * @throws RuntimeException if the executable is not found or initialization fails
     */
    private void startEngine() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String stockfishFile = os.contains("win") ? "stockfish.exe" : "stockfish";
            String stockfishPath = Objects.requireNonNull(getClass().getClassLoader().getResource(stockfishFile), "Stockfish executable not found: " + stockfishFile).getPath();
            logger.info("Starting Stockfish at path: {}", stockfishPath);
            ProcessBuilder pb = new ProcessBuilder(stockfishPath);
            pb.redirectErrorStream(true);
            stockfishProcess = pb.start();

            reader = new BufferedReader(new InputStreamReader(stockfishProcess.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(stockfishProcess.getOutputStream()));

            sendCommand("uci");
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("uciok")) {
                    logger.info("Stockfish initialized successfully");
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("Failed to start Stockfish", e);
            throw new RuntimeException("Stockfish initialization failed", e);
        } catch (NullPointerException e) {
            logger.error("Stockfish executable not found in resources", e);
            throw new RuntimeException("Stockfish executable missing", e);
        }
    }
}
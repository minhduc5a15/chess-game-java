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

public class Stockfish {
    private static final Logger logger = LoggerFactory.getLogger(Stockfish.class);
    private Process stockfishProcess;
    private BufferedReader reader;
    private BufferedWriter writer;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

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

    public void start() {
        executor.submit(() -> {
            try {
                startEngine();
            } catch (Exception e) {
                logger.error("Error starting Stockfish", e);
            }
        });
    }
}

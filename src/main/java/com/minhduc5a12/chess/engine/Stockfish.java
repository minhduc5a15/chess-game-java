package com.minhduc5a12.chess.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
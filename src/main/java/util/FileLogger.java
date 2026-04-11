package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileLogger {
    private static final String LOG_FILE = "session.log";
    private static PrintWriter writer;

    public static void init() {
        try {
            writer = new PrintWriter(new FileWriter(LOG_FILE, true));
            log("=== VoltSync Pro Session Started ===");
        } catch (IOException e) {
            System.err.println("Logger init failed: " + e.getMessage());
        }
    }

    public static void log(String message) {
        if (writer != null) {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.println("[" + time + "] " + message);
            writer.flush();
        }
    }

    public static void close() {
        if (writer != null) writer.close();
    }
}
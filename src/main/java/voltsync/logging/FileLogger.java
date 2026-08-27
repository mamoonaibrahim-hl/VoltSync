package voltsync.logging;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileLogger implements Loggable {

    private final String filePath;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private BufferedWriter writer;

    public FileLogger(String filePath) {
        this.filePath = filePath;
        try {
            
            this.writer = new BufferedWriter(new FileWriter(filePath, true));
            log("=== VoltSync Session Started ===");
        } catch (IOException e) {
            System.err.println("[FileLogger] Could not open log file: " + e.getMessage());
        }
    }

    @Override
    public void log(String message) {
        write("INFO ", message);
    }

    @Override
    public void logWarning(String message) {
        write("WARN ", message);
    }

    @Override
    public void logError(String message) {
        write("ERROR", message);
    }

    private void write(String level, String message) {
        
        try {
            String line = String.format("[%s] [%s] %s%n",
                LocalDateTime.now().format(fmt), level, message);
            writer.write(line);
        } catch (IOException e) {
            System.err.println("[FileLogger] Write failed: " + e.getMessage());
        }
    }

    @Override
    public void flush() {
        try {
            if (writer != null) writer.flush();
        } catch (IOException e) {
            System.err.println("[FileLogger] Flush failed: " + e.getMessage());
        }
    }

    public void close() {
        
        flush();
        try {
            if (writer != null) writer.close();
        } catch (IOException e) {
            System.err.println("[FileLogger] Close failed: " + e.getMessage());
        }
    }

    public String getFilePath() { return filePath; }
}

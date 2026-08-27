package voltsync.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConsoleLogger implements Loggable {

    private static final String RESET  = "\u001B[0m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED    = "\u001B[31m";

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public void log(String message) {
        System.out.printf("%s[%s INFO ] %s%s%n", GREEN, now(), message, RESET);
    }

    @Override
    public void logWarning(String message) {
        System.out.printf("%s[%s WARN ] %s%s%n", YELLOW, now(), message, RESET);
    }

    @Override
    public void logError(String message) {
        System.out.printf("%s[%s ERROR] %s%s%n", RED, now(), message, RESET);
    }

    @Override
    public void flush() {  }

    private String now() {
        return LocalDateTime.now().format(fmt);
    }
}

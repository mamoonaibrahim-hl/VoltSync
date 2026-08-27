package voltsync.logging;

public interface Loggable {
    void log(String message);
    void logWarning(String message);
    void logError(String message);
    void flush();                    
}

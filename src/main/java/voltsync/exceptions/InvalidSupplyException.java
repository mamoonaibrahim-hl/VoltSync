package voltsync.exceptions;

public class InvalidSupplyException extends RuntimeException {
    public InvalidSupplyException(String sourceName, double value) {
        super(String.format("[INVALID SUPPLY] Source '%s' reported illegal value: %.1f kW", sourceName, value));
    }
}

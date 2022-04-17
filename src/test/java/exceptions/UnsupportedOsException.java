package exceptions;

/**
 * Exception that is being thrown when unsuported browser is provided
 * Chrome and Firefox web browsers are only supported
 */
public class UnsupportedOsException extends Exception {
    public UnsupportedOsException(String message) {
        super(message);
    }
}

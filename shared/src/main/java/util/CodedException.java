package util;

/**
 * Associates an HTTP status code with an exception message.
 */
public class CodedException extends Exception {
    final private int statusCode;

    public CodedException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}

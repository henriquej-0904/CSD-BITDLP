package itdlp.tp1.api.operations;

public class InvalidOperationException extends Exception {

    /**
     * @param message
     */
    public InvalidOperationException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public InvalidOperationException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
    
}

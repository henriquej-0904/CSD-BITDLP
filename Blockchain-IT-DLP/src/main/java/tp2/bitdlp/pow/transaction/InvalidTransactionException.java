package tp2.bitdlp.pow.transaction;

public class InvalidTransactionException extends RuntimeException {

    /**
     * @param message
     */
    public InvalidTransactionException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public InvalidTransactionException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public InvalidTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
    
}

package tp2.bitdlp.impl.client;

public class InvalidReplyWithSignaturesException extends RuntimeException {

    /**
     * @param message
     */
    public InvalidReplyWithSignaturesException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public InvalidReplyWithSignaturesException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public InvalidReplyWithSignaturesException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
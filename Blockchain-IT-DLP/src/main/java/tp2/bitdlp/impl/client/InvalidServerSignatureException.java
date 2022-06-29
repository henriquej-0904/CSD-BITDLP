package tp2.bitdlp.impl.client;

public class InvalidServerSignatureException extends RuntimeException {

    /**
     * @param message
     */
    public InvalidServerSignatureException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public InvalidServerSignatureException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public InvalidServerSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
    
}

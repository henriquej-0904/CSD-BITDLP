package itdlp.tp1.data;

/**
 * An error occurred in the ledger db layer.
 */
public class LedgerDBlayerException extends Exception
{

    /**
     * @param message
     */
    public LedgerDBlayerException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public LedgerDBlayerException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public LedgerDBlayerException(String message, Throwable cause) {
        super(message, cause);
    }
    
}

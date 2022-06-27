package tp2.bitdlp.pow;

public class Settings
{
    public static final int DEFAULT_VERSION = 0x0001;

    public static final int DEFAULT_DIFFICULTY_TARGET = 0x3FFFFFFF;

    public static final int DEFAULT_NUM_TRANSACTIONS_IN_BLOCK = 8;

    public static final int DEFAULT_GENERATION_TRANSACTION_VALUE = 100;

    /**
     * Get the current version of BC blocks.
     * @return the current version of BC blocks.
     */
    public static int getCurrentVersion()
    {
        return DEFAULT_VERSION;
    }

    /**
     * Get the difficulty target for the PoW.
     * The returned number represents the upper bound for the
     * valid hash found through PoW. The most significant 32 bits of
     * that hash must be less than this difficulty target.
     * 
     * @return the difficulty target for the PoW.
     */
    public static int getDifficultyTarget()
    {
        return DEFAULT_DIFFICULTY_TARGET;
    }

    /**
     * Get the valid number of transaction in a block.
     * @return the valid number of transaction in a block.
     */
    public static int getValidNumberTransactionsInBlock()
    {
        return DEFAULT_NUM_TRANSACTIONS_IN_BLOCK;
    }

    public static int getGenerationTransactionValue()
    {
        return DEFAULT_GENERATION_TRANSACTION_VALUE;
    }

}

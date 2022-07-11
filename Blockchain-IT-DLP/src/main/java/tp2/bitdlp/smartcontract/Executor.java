package tp2.bitdlp.smartcontract;

import java.io.File;

import tp2.bitdlp.pow.transaction.LedgerTransaction;
import tp2.bitdlp.util.Utils;
import tp2.bitdlp.util.result.Result;

/**
 * Class to execute smart contracts in an isolated environment.
 */
class Executor
{
    /**
     * Execute a smart contract in an isolated environment.
     * @param className The class name.
     * @param classPath The class path.
     * @param params The parameters for the smart contract.
     * @return A result of boolean (true for verified transaction, false otherwise).
     */
    public static Result<Boolean> execute(String className, File classPath, String... params)
    {
        // TODO:
        return null;
    }

    /**
     * Execute a smart contract in an isolated environment.
     * @param className The class name.
     * @param classPath The class path.
     * @param transaction The transaction to verify.
     * @return A result of boolean (true for verified transaction, false otherwise).
     */
    public static Result<Boolean> execute(String className, File classPath,
        LedgerTransaction transaction)
    {
        return execute(className, classPath, convertTransactionToParams(transaction));
    }

    protected static String[] convertTransactionToParams(LedgerTransaction transaction)
    {
        return new String[] {
            Utils.toBase64(transaction.getOrigin().getObjectId()),
            Utils.toBase64(transaction.getDest().getObjectId()),
            Integer.toString(transaction.getValue()),
            Integer.toString(transaction.getNonce()),
            transaction.getClientSignature()
        };
    }
}

package tp2.bitdlp.smartcontract;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tp2.bitdlp.pow.transaction.LedgerTransaction;
import tp2.bitdlp.util.Utils;
import tp2.bitdlp.util.result.Result;

/**
 * Class to execute smart contracts in an isolated environment.
 */
class Executor
{
    protected static final File SMART_CONTRACT_SECURITY_POLICY = new File("smart-contracts", "smart-contract.policy");

    /**
     * Execute a smart contract in an isolated environment.
     * @param className The class name.
     * @param classPath The class path.
     * @param params The parameters for the smart contract.
     * @return A result of boolean (true for verified transaction, false otherwise).
     */
    public static Result<Boolean> execute(String className, File classPath, String... params)
    {
        List<String> args = new LinkedList<>();
        args.add("java");
        args.add("-Djava.security.manager");
        args.add("-Djava.security.policy==" + SMART_CONTRACT_SECURITY_POLICY.getAbsolutePath());
        args.add(className);
        args.addAll(List.of(params));

        File errorFile = new File(classPath, "error.txt");

        ProcessBuilder pBuilder = new ProcessBuilder(args);
        pBuilder.directory(classPath);
        pBuilder.redirectError(errorFile);

        try {
            Process p = pBuilder.start();
            String res = p.inputReader().readLine();

            p.waitFor();

            if (res != null && res.equalsIgnoreCase(Boolean.TRUE.toString()))
                return Result.ok(true);
            else if (res != null && res.equalsIgnoreCase(Boolean.FALSE.toString()))
                return Result.ok(false);

            // An error occurred.
            if (!errorFile.isFile())
                return Result.error(new WebApplicationException(
                    "The smart contract execution encountered and error.", Status.CONFLICT));
            
            try (InputStream errorStream = new FileInputStream(errorFile))
            {
                return Result.error(new WebApplicationException(
                    new String(errorStream.readAllBytes()), Status.CONFLICT));
            }
            
        } catch (Exception e) {
            return Result.error(new InternalServerErrorException(e.getMessage(), e));
        }
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

package tp2.bitdlp.smartcontract;

import java.io.File;

import tp2.bitdlp.pow.transaction.LedgerTransaction;
import tp2.bitdlp.util.result.Result;

/**
 * Represents a Smart contract that can be compiled and executed.
 */
public class SmartContract
{
    private final String name;
    private final byte[] code;

    private File directory;

    /**
     * Creates a new Smart contract.
     * @param name The name of the Smart-Contract
     * @param code The code to be compiled and executed.
     */
    public SmartContract(String name, byte[] code) {
        this.name = name;
        this.code = code;
    }

    /**
     * Compile the smart contract.
     * @return Result status.
     */
    public Result<Void> compile()
    {
        Result<File> res = Compiler.compile(name, code);

        if (res.isOK())
        {
            this.directory = res.value();
            return Result.ok();
        }
            
        return Result.error(res.errorException());
    }

    /**
     * Execute a smart contract in an isolated environment.
     * @param transaction The transaction to verify.
     * @return A result of boolean (true for verified transaction, false otherwise).
     */
    public Result<Boolean> execute(LedgerTransaction transaction)
    {
        return Executor.execute(this.name, this.directory, transaction);
    }
}

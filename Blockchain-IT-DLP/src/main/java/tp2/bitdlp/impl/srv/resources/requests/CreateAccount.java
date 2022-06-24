package tp2.bitdlp.impl.srv.resources.requests;

import tp2.bitdlp.util.Pair;

public class CreateAccount extends Request {

    private Pair<byte[],byte[]> accountUserPair;
    
    private String userSignature;

    public CreateAccount() {
        super(Operation.CREATE_ACCOUNT);
    }

    public CreateAccount(Pair<byte[],byte[]> accountUserPair, String userSignature){
        super(Operation.CREATE_ACCOUNT);
        this.accountUserPair = accountUserPair;
        this.userSignature = userSignature;
    }

    /**
     * @return the accountUserPair
     */
    public Pair<byte[], byte[]> getAccountUserPair() {
        return accountUserPair;
    }

    /**
     * @param accountUserPair the accountUserPair to set
     */
    public void setAccountUserPair(Pair<byte[], byte[]> accountUserPair) {
        this.accountUserPair = accountUserPair;
    }

    /**
     * @return the userSignature
     */
    public String getUserSignature() {
        return userSignature;
    }

    /**
     * @param userSignature the userSignature to set
     */
    public void setUserSignature(String userSignature) {
        this.userSignature = userSignature;
    }

    
}



package tp2.bitdlp.impl.srv.resources.requests;

public class GetAccount extends Request {

    private String id;

    public GetAccount(){
        super(Operation.GET_ACCOUNT);
    }

    public GetAccount(String id){
        super(Operation.GET_ACCOUNT);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }   
}

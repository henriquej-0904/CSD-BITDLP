package itdlp.tp1.impl.srv.resources.requests;

public class GetFullLedger extends Request {
    private static final long serialVersionUID = 1243244634L;

    public GetFullLedger() {
        super(Operation.GET_LEDGER);
    }
}

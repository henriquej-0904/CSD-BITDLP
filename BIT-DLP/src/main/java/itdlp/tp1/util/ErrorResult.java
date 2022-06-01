package itdlp.tp1.util;

import jakarta.ws.rs.WebApplicationException;

class ErrorResult<T> implements Result<T> {

    private static final long serialVersionUID = 12432404654L;

    final int error;
    final String msg;
    final StackTraceElement[] stackTrace;
    final Throwable cause;

    ErrorResult(int error) {
        this.error = error;
        this.msg = null;
        this.stackTrace = null;
        this.cause = null;
    }

    ErrorResult(WebApplicationException ex) {
        this.error = ex.getResponse().getStatus();
        this.msg = ex.getMessage();
        this.stackTrace = ex.getStackTrace();
        this.cause = ex.getCause();
    }

    public boolean isOK() {
        return false;
    }

    public T value() {
        throw new RuntimeException("Attempting to extract the value of an Error: " + error());
    }

    public int error() {
        return this.error;
    }

    public String toString() {
        return "(" + error() + ")";
    }

    public T resultOrThrow() {
        throw errorException();
    }

    @Override
    public WebApplicationException errorException() {
        WebApplicationException ex;
        if (msg != null)
            ex = new WebApplicationException(msg, cause, error);
        else
            ex = new WebApplicationException(cause, error);

        ex.setStackTrace(stackTrace);
        throw ex;
    }
}
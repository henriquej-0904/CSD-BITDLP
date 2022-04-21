package itdlp.tp1.util;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

class ErrorResult<T> implements Result<T> {

    private static final long serialVersionUID = 12432404654L;

    final int error;
    final WebApplicationException ex;

    ErrorResult(int error) {
        this.error = error;
        this.ex = new WebApplicationException(Status.fromStatusCode(error));
    }

    ErrorResult(WebApplicationException ex) {
        this.error = ex.getResponse().getStatus();
        this.ex = ex;
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
        throw this.ex;
    }

    @Override
    public WebApplicationException errorException() {
        return ex;
    }

    
}
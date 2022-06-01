package itdlp.tp1.util;

import java.io.Serializable;

import jakarta.ws.rs.WebApplicationException;

public interface Result<T> extends Serializable {
    boolean isOK();

    T resultOrThrow() throws WebApplicationException;

    T value();

    int error();

    WebApplicationException errorException();

    static <T> Result<T> ok(T result) {
        return new OkResult<T>(result);
    }

    static <T> OkResult<T> ok() {
        return new OkResult<T>(null);
    }

    static <T> ErrorResult<T> error(int error) {
        return new ErrorResult<T>(error);
    }

    static <T> ErrorResult<T> error(WebApplicationException e) {
        return new ErrorResult<T>(e);
    }
}
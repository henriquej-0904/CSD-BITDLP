package itdlp.tp1.util;

import jakarta.ws.rs.WebApplicationException;

class OkResult<T> implements Result<T> {
    final T result;

    OkResult(T result) {
        this.result = result;
    }

    public boolean isOK() {
        return true;
    }

    public T value() {
        return this.result;
    }

    public int error() {
        if (this.result == null) {
            return 204;
        }
        
        return 200;
    }

    public String toString() {
        return "(OK, " + value() + ")";
    }

    public T resultOrThrow() {
        return this.result;
    }

    @Override
    public WebApplicationException errorException() {
        return null;
    }

    
}
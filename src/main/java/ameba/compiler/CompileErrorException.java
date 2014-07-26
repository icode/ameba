package ameba.compiler;

import ameba.exceptions.AmebaException;

public class CompileErrorException extends AmebaException {
    private static final long serialVersionUID = 1L;

    protected CompileErrorException(String message) {
        super(message);
    }

    public CompileErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public CompileErrorException() {
    }

    public CompileErrorException(Throwable cause) {
        super(cause);
    }
}

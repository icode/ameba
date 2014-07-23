package ameba.compiler;

import ameba.exceptions.AmebaException;

public class CompileErrorException extends AmebaException {
    private static final long serialVersionUID = 1L;

    public CompileErrorException() {
        super();
    }

    public CompileErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public CompileErrorException(String message) {
        super(message);
    }

    public CompileErrorException(Throwable cause) {
        super(cause);
    }
}

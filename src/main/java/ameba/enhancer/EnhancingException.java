package ameba.enhancer;

import ameba.exception.AmebaException;

/**
 * @author icode
 */
public class EnhancingException extends AmebaException {
    public EnhancingException() {
    }

    public EnhancingException(Throwable cause) {
        super(cause);
    }

    public EnhancingException(String message) {
        super(message);
    }

    public EnhancingException(String message, Throwable cause) {
        super(message, cause);
    }
}

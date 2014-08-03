package ameba.container;

import ameba.exceptions.AmebaException;

/**
 * @author icode
 */
public class ContainerException extends AmebaException {
    public ContainerException() {
        super();
    }

    protected ContainerException(Throwable cause) {
        super(cause);
    }

    public ContainerException(String message) {
        super(message);
    }

    public ContainerException(String message, Throwable cause) {
        super(message, cause);
    }
}

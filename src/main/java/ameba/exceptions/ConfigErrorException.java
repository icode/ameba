package ameba.exceptions;

/**
 * @author icode
 */
public class ConfigErrorException extends AmebaExceptionWithJavaSource {
    public ConfigErrorException() {
    }

    public ConfigErrorException(String message) {
        super(message);
    }

    public ConfigErrorException(String message, Throwable cause, Integer line) {
        super(message, cause, line);
    }

    public ConfigErrorException(String message, Throwable cause) {
        super(message, cause, -1);
    }
}

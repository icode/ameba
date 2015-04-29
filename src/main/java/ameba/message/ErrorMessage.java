package ameba.message;

import ameba.util.Result;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * @author icode
 */
public class ErrorMessage extends Result {

    @JsonIgnore
    private Throwable throwable;

    public ErrorMessage(boolean success) {
        super(success);
    }

    public ErrorMessage(String message, List<Error> errors) {
        super(message, errors);
    }

    public ErrorMessage(Integer code, String message, List<Error> errors) {
        super(code, message, errors);
    }

    public ErrorMessage(Integer code, String message, String description, List<Error> errors) {
        super(code, message, description, errors);
    }

    public ErrorMessage(boolean success, String message) {
        super(success, message);
    }

    public ErrorMessage(boolean success, Integer code, String message) {
        super(success, code, message);
    }

    public ErrorMessage(String message, String description, List<Error> errors) {
        super(message, description, errors);
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}

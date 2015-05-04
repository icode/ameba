package ameba.message.error;

import ameba.i18n.Messages;
import ameba.util.Result;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * @author icode
 */
public class ErrorMessage extends Result {
    @JsonIgnore
    public final static String LOCALE_FILE = "ameba.message.error.localization";
    @JsonIgnore
    private Throwable throwable;
    private int status;

    public ErrorMessage() {
        super(false);
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

    public static String getLocaleMessage(String key) {
        return Messages.get(LOCALE_FILE, key);
    }

    public static String getLocaleMessage(int status) {
        return getLocaleMessage(status + ".error.message");
    }

    public static String getLocaleMessage() {
        return getLocaleMessage("default.error.message");
    }

    public static String getLocaleDescription() {
        return getLocaleMessage("default.error.description");
    }


    public static String getLocaleDescription(int status) {
        return getLocaleMessage(status + ".error.description");
    }


    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static ErrorMessage fromStatus(int status){
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setStatus(status);
        errorMessage.setMessage(ErrorMessage.getLocaleMessage(status));
        errorMessage.setDescription(ErrorMessage.getLocaleDescription(status));
        return errorMessage;
    }
}

package ameba.message.error;

import ameba.core.Requests;
import ameba.i18n.Messages;
import ameba.util.ClassUtils;
import ameba.util.Result;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import org.glassfish.jersey.message.internal.MessageBodyProviderNotFoundException;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ResourceInfo;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * @author icode
 */
public class ErrorMessage extends Result {
    @JsonIgnore
    public final static String LOCALE_FILE = "ameba.message.error.http";
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
        return Messages.get(LOCALE_FILE, key, null);
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

    public static ErrorMessage fromStatus(int status) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setStatus(status);
        errorMessage.setMessage(parseMessage(status));
        errorMessage.setDescription(parseDescription(status));
        return errorMessage;
    }

    public static int parseHttpStatus(Throwable exception) {
        int status = 500;
        Exception ex = (Exception) Requests.getProperty(DefaultExceptionMapper.BEFORE_EXCEPTION_KEY);
        if (ex != null) {
            exception = ex;
        }
        if (exception instanceof InternalServerErrorException) {
            if (exception.getCause() instanceof MessageBodyProviderNotFoundException) {
                MessageBodyProviderNotFoundException e = (MessageBodyProviderNotFoundException) exception.getCause();
                if (e.getMessage().startsWith("MessageBodyReader")) {
                    status = 415;
                } else if (e.getMessage().startsWith("MessageBodyWriter")) {
                    status = 406;
                }
            }
        } else if (exception instanceof WebApplicationException) {
            status = ((WebApplicationException) exception).getResponse().getStatus();
        } else if (exception instanceof FileNotFoundException) {
            status = 404;
        }
        return status;
    }

    public static List<Result.Error> parseErrors(Throwable exception, int status) {
        List<Result.Error> errors = Lists.newArrayList();
        if (status == 500 || status == 400) {
            Throwable cause = exception;
            while (cause != null) {
                StackTraceElement[] stackTraceElements = cause.getStackTrace();
                if (stackTraceElements != null && stackTraceElements.length > 0) {
                    Result.Error error = new Result.Error(
                            cause.getClass().getCanonicalName().hashCode(),
                            cause.getMessage());

                    if (status == 500) {
                        StringBuilder descBuilder = new StringBuilder();
                        for (StackTraceElement element : stackTraceElements) {
                            descBuilder
                                    .append(element.toString())
                                    .append("\n");
                        }

                        error.setDescription(descBuilder.toString());
                    }
                    StackTraceElement stackTraceElement = stackTraceElements[0];
                    String source = stackTraceElement.toString();
                    error.setSource(source);

                    errors.add(error);
                }
                cause = cause.getCause();
            }
        }

        return errors;
    }

    public static String parseMessage(int status) {
        String msg = null;
        if (status < 500) {
            if (status == 402
                    || (status > 417 && status < 421)
                    || status > 424) {
                msg = ErrorMessage.getLocaleMessage(400);
            } else {
                msg = ErrorMessage.getLocaleMessage(status);
            }
        } else {
            switch (status) {
                case 501:
                    msg = ErrorMessage.getLocaleMessage(status);
                    break;
            }
        }

        if (msg == null) {
            msg = ErrorMessage.getLocaleMessage();
        }

        return msg;
    }

    public static String parseDescription(int status) {
        String desc = null;
        if (status < 500) {
            if (status == 402
                    || (status > 417 && status < 421)
                    || status > 424) {
                desc = ErrorMessage.getLocaleDescription(400);
            } else {
                desc = ErrorMessage.getLocaleDescription(status);
            }
        } else {
            switch (status) {
                case 501:
                    desc = ErrorMessage.getLocaleDescription(status);
                    break;
            }
        }

        if (desc == null) {
            desc = ErrorMessage.getLocaleDescription();
        }

        return desc;
    }

    public static String parseSource(ResourceInfo resourceInfo) {
        if (resourceInfo != null) {
            Class clazz = resourceInfo.getResourceClass();
            if (clazz != null)
                return ClassUtils.toString(clazz, resourceInfo.getResourceMethod());
        }
        return null;
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

    public String toString() {
        return Messages.get("error.code") + ": " + getCode() + "\n"
                + Messages.get("error.message") + ": " + getMessage() + "\n"
                + Messages.get("error.description") + ": " + getDescription();
    }
}

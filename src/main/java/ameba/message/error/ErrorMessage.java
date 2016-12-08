package ameba.message.error;

import ameba.core.Requests;
import ameba.i18n.Messages;
import ameba.util.ClassUtils;
import ameba.util.Result;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import org.glassfish.jersey.message.internal.MessageBodyProviderNotFoundException;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ResourceInfo;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * <p>ErrorMessage class.</p>
 *
 * @author icode
 *
 */
public class ErrorMessage extends Result {
    /**
     * Constant <code>LOCALE_FILE="ameba.message.error.http"</code>
     */
    @JsonIgnore
    public final static String LOCALE_FILE = "ameba.message.error.http";
    @JsonIgnore
    private Throwable throwable;
    private int status;

    /**
     * <p>Constructor for ErrorMessage.</p>
     */
    public ErrorMessage() {
        super(false);
    }

    /**
     * <p>Constructor for ErrorMessage.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param errors a {@link java.util.List} object.
     */
    public ErrorMessage(String message, List<Error> errors) {
        super(message, errors);
    }

    /**
     * <p>Constructor for ErrorMessage.</p>
     *
     * @param code a {@link java.lang.String} object.
     * @param message a {@link java.lang.String} object.
     * @param description a {@link java.lang.String} object.
     * @param errors a {@link java.util.List} object.
     */
    public ErrorMessage(String code, String message, String description, List<Error> errors) {
        super(code, message, description, errors);
    }

    /**
     * <p>Constructor for ErrorMessage.</p>
     *
     * @param success a boolean.
     * @param message a {@link java.lang.String} object.
     */
    public ErrorMessage(boolean success, String message) {
        super(success, message);
    }

    /**
     * <p>Constructor for ErrorMessage.</p>
     *
     * @param success a boolean.
     * @param code a {@link java.lang.String} object.
     * @param message a {@link java.lang.String} object.
     */
    public ErrorMessage(boolean success, String code, String message) {
        super(success, code, message);
    }

    /**
     * <p>Constructor for ErrorMessage.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param description a {@link java.lang.String} object.
     * @param errors a {@link java.util.List} object.
     */
    public ErrorMessage(String message, String description, List<Error> errors) {
        super(message, description, errors);
    }

    /**
     * <p>getLocaleMessage.</p>
     *
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getLocaleMessage(String key) {
        return Messages.get(LOCALE_FILE, key, null);
    }

    /**
     * <p>getLocaleMessage.</p>
     *
     * @param status a int.
     * @return a {@link java.lang.String} object.
     */
    public static String getLocaleMessage(int status) {
        return getLocaleMessage(status + ".error.message");
    }

    /**
     * <p>getLocaleMessage.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getLocaleMessage() {
        return getLocaleMessage("default.error.message");
    }

    /**
     * <p>getLocaleDescription.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getLocaleDescription() {
        return getLocaleMessage("default.error.description");
    }


    /**
     * <p>getLocaleDescription.</p>
     *
     * @param status a int.
     * @return a {@link java.lang.String} object.
     */
    public static String getLocaleDescription(int status) {
        return getLocaleMessage(status + ".error.description");
    }

    /**
     * <p>fromStatus.</p>
     *
     * @param status a int.
     * @return a {@link ameba.message.error.ErrorMessage} object.
     */
    public static ErrorMessage fromStatus(int status) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setStatus(status);
        errorMessage.setMessage(parseMessage(status));
        errorMessage.setDescription(parseDescription(status));
        return errorMessage;
    }

    /**
     * <p>parseHttpStatus.</p>
     *
     * @param exception a {@link java.lang.Throwable} object.
     * @return a int.
     */
    public static int parseHttpStatus(Throwable exception) {
        int status = 500;
        Exception ex = Requests.getProperty(DefaultExceptionMapper.BEFORE_EXCEPTION_KEY);
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

    /**
     * <p>parseErrors.</p>
     *
     * @param exception a {@link java.lang.Throwable} object.
     * @param status a int.
     * @return a {@link java.util.List} object.
     */
    public static List<Result.Error> parseErrors(Throwable exception, int status) {
        List<Result.Error> errors = Lists.newArrayList();
        if (status == 500 || status == 400) {
            Throwable cause = exception;
            while (cause != null) {
                StackTraceElement[] stackTraceElements = cause.getStackTrace();
                if (stackTraceElements != null && stackTraceElements.length > 0) {
                    Result.Error error = new Result.Error(
                            Hashing.murmur3_32().hashUnencodedChars(exception.getClass().getName()).toString(),
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

    /**
     * <p>parseMessage.</p>
     *
     * @param status a int.
     * @return a {@link java.lang.String} object.
     */
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

    /**
     * <p>parseDescription.</p>
     *
     * @param status a int.
     * @return a {@link java.lang.String} object.
     */
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

    /**
     * <p>parseSource.</p>
     *
     * @param resourceInfo a {@link javax.ws.rs.container.ResourceInfo} object.
     * @return a {@link java.lang.String} object.
     */
    public static String parseSource(ResourceInfo resourceInfo) {
        if (resourceInfo != null) {
            Class clazz = resourceInfo.getResourceClass();
            if (clazz != null)
                return ClassUtils.toString(clazz, resourceInfo.getResourceMethod());
        }
        return null;
    }

    /**
     * <p>Getter for the field <code>throwable</code>.</p>
     *
     * @return a {@link java.lang.Throwable} object.
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * <p>Setter for the field <code>throwable</code>.</p>
     *
     * @param throwable a {@link java.lang.Throwable} object.
     */
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    /**
     * <p>Getter for the field <code>status</code>.</p>
     *
     * @return a int.
     */
    public int getStatus() {
        return status;
    }

    /**
     * <p>Setter for the field <code>status</code>.</p>
     *
     * @param status a int.
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * <p>toString.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return Messages.get("error.code") + ": " + getCode() + "\n"
                + Messages.get("error.message") + ": " + getMessage() + "\n"
                + Messages.get("error.description") + ": " + getDescription();
    }
}

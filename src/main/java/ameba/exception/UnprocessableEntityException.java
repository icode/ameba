package ameba.exception;

import ameba.i18n.Messages;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;

/**
 * <p>UnprocessableEntityException class.</p>
 *
 * @author icode
 *
 */
public class UnprocessableEntityException extends ClientErrorException {
    /**
     * Constant <code>STATUS=422</code>
     */
    public static final int STATUS = 422;

    /**
     * Construct a new bad client request exception.
     */
    public UnprocessableEntityException() {
        super(STATUS);
    }

    /**
     * Construct a new bad client request exception.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     */
    public UnprocessableEntityException(String message) {
        super(message, STATUS);
    }

    /**
     * Construct a new bad client request exception.
     *
     * @param response error response.
     * @throws java.lang.IllegalArgumentException in case the status code set in the response
     *                                  is not HTTP {@code 422}.
     */
    public UnprocessableEntityException(Response response) {
        super(validate(response, STATUS));
    }

    /**
     * Construct a new bad client request exception.
     *
     * @param message  the detail message (which is saved for later retrieval
     *                 by the {@link #getMessage()} method).
     * @param response error response.
     * @throws java.lang.IllegalArgumentException in case the status code set in the response
     *                                  is not HTTP {@code 422}.
     */
    public UnprocessableEntityException(String message, Response response) {
        super(message, validate(response, STATUS));
    }

    /**
     * Construct a new bad client request exception.
     *
     * @param cause the underlying cause of the exception.
     */
    public UnprocessableEntityException(Throwable cause) {
        super(STATUS, cause);
    }

    /**
     * Construct a new bad client request exception.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the underlying cause of the exception.
     */
    public UnprocessableEntityException(String message, Throwable cause) {
        super(message, STATUS, cause);
    }

    /**
     * Construct a new bad client request exception.
     *
     * @param response error response.
     * @param cause    the underlying cause of the exception.
     * @throws java.lang.IllegalArgumentException in case the status code set in the response
     *                                  is not HTTP {@code 422}.
     */
    public UnprocessableEntityException(Response response, Throwable cause) {
        super(validate(response, STATUS), cause);
    }

    /**
     * Construct a new bad client request exception.
     *
     * @param message  the detail message (which is saved for later retrieval
     *                 by the {@link #getMessage()} method).
     * @param response error response.
     * @param cause    the underlying cause of the exception.
     * @throws java.lang.IllegalArgumentException in case the status code set in the response
     *                                  is not HTTP {@code 422}.
     */
    public UnprocessableEntityException(String message, Response response, Throwable cause) {
        super(message, validate(response, STATUS), cause);
    }

    static Response validate(final Response response, int expectedStatus) {
        if (expectedStatus != response.getStatus()) {
            throw new IllegalArgumentException(String.format("Invalid response status code. Expected [%d], was [%d].",
                    expectedStatus, response.getStatus()));
        }
        return response;
    }

    /**
     * <p>throwErr.</p>
     *
     * @param msg a {@link java.lang.String} object.
     */
    public static void throwErr(String msg) {
        throw new UnprocessableEntityException(msg);
    }

    /**
     * <p>throwQuery.</p>
     *
     * @param arg a {@link java.lang.Object} object.
     */
    public static void throwQuery(Object arg) {
        throw new UnprocessableEntityException(Messages.get("info.query.unprocessable.entity", arg));
    }
}

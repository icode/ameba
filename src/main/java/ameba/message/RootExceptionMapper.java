package ameba.message;

import ameba.core.Application;
import ameba.util.Result;
import com.google.common.collect.Lists;
import org.glassfish.jersey.message.internal.MessageBodyProviderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.List;

/**
 * <p>RootExceptionMapper class.</p>
 *
 * @author ICode
 * @since 13-8-17 下午2:00
 */
@Priority(Priorities.ENTITY_CODER)
@Singleton
public class RootExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger logger = LoggerFactory.getLogger(RootExceptionMapper.class);

    @Inject
    private Application application;

    protected int getStatus(Throwable exception) {
        int status = 500;
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
        }
        return status;
    }

    protected String parseMessage(Throwable exception) {
        return "系统错误";
    }

    protected String parseDescription(Throwable exception) {
        return exception.getLocalizedMessage();
    }

    protected List<Result.Error> parseErrors(Throwable exception) {
        List<Result.Error> errors = Lists.newArrayList();

        Throwable cause;
        while ((cause = exception.getCause()) != null) {
            StackTraceElement stackTraceElement = cause.getStackTrace()[0];
            Result.Error error = new Result.Error(cause.hashCode(),
                    cause.getMessage(),
                    stackTraceElement.toString());
            errors.add(error);
        }

        return errors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response toResponse(Throwable exception) {
        int status = getStatus(exception);

        ErrorMessage message = new ErrorMessage(false);
        message.setCode(status);
        message.setThrowable(exception);
        message.setMessage(parseMessage(exception));
        message.setDescription(parseDescription(exception));
        if (application.getMode().isDev()) {
            message.setErrors(parseErrors(exception));
        }

        if (status == 500) {
            logger.error("系统发生错误", exception);
        }

        return Response.status(status).entity(message).build();
    }
}

package ameba.message.error;

import ameba.core.Application;
import ameba.util.Result;
import com.google.common.collect.Lists;
import org.glassfish.jersey.message.internal.MessageBodyProviderNotFoundException;
import org.glassfish.jersey.server.internal.process.MappableException;
import org.glassfish.jersey.server.spi.ResponseErrorMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.List;

/**
 * <p>DefaultExceptionMapper class.</p>
 *
 * @author icode
 * @since 13-8-17 下午2:00
 */
@Singleton
@Priority(Priorities.USER)
public class DefaultExceptionMapper implements ExceptionMapper<Throwable>, ResponseErrorMapper {

    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionMapper.class);
    @Inject
    private Application application;

    protected int getHttpStatus(Throwable exception) {
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

    protected String parseMessage(Throwable exception, int status) {
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

    protected String parseDescription(Throwable exception, int status) {
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

    protected List<Result.Error> parseErrors(Throwable exception, int status) {
        List<Result.Error> errors = null;
        if (status == 500 || status == 400) {
            boolean isDev = application.getMode().isDev();
            errors = Lists.newArrayList();
            Throwable cause = exception;
            while (cause != null) {
                StackTraceElement[] stackTraceElements = cause.getStackTrace();
                if (stackTraceElements != null && stackTraceElements.length > 0) {
                    Result.Error error = new Result.Error(
                            cause.getClass().getCanonicalName().hashCode(),
                            cause.getMessage());

                    if (isDev) {
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
                    }

                    errors.add(error);
                }
                cause = cause.getCause();
            }
        }

        return errors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response toResponse(Throwable exception) {
        int status = getHttpStatus(exception);

        ErrorMessage message = new ErrorMessage();

        if (exception instanceof MappableException
                && exception.getCause() != null) {
            exception = exception.getCause();
        }

        message.setCode(exception.getClass().getCanonicalName().hashCode());
        message.setStatus(status);
        message.setThrowable(exception);
        message.setMessage(parseMessage(exception, status));
        message.setDescription(parseDescription(exception, status));
        message.setErrors(parseErrors(exception, status));

        if (status == 500) {
            logger.error("系统发生错误", exception);
        }

        return Response.status(status)
                .type(ExceptionMapperUtils.getResponseType())
                .entity(message).build();
    }
}

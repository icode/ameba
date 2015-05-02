package ameba.message.error;

import ameba.core.Application;
import ameba.i18n.Messages;
import ameba.util.Result;
import com.google.common.collect.Lists;
import org.glassfish.jersey.message.internal.MessageBodyProviderNotFoundException;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.process.MappableException;
import org.glassfish.jersey.server.spi.ResponseErrorMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.List;
import java.util.Locale;

/**
 * <p>RootExceptionMapper class.</p>
 *
 * @author icode
 * @since 13-8-17 下午2:00
 */
@Singleton
@Priority(Priorities.USER)
public class DefaultExceptionMapper implements ExceptionMapper<Throwable>, ResponseErrorMapper {

    private final static String LOCALE_FILE = "ameba.message.error.localization";

    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionMapper.class);
    @Inject
    private Application application;
    @Inject
    private Provider<ContainerRequest> requestProvider;

    private String getMessage(String key) {
        return Messages.get(LOCALE_FILE, key);
    }

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

    protected String parseMessage(Throwable exception, Locale locale, int status) {
        String msg = null;
        if (status < 500) {
            if (status == 402
                    || (status > 417 && status < 421)
                    || status > 424) {
                msg = getMessage("400.error.message");
            } else {
                msg = getMessage(status + ".error.message");
            }
        } else {
            switch (status) {
                case 501:
                    msg = getMessage("501.error.message");
                    break;
            }
        }

        if (msg == null) {
            msg = getMessage("default.error.message");
        }

        return msg;
    }

    protected String parseDescription(Throwable exception, Locale locale, int status) {
        String desc = null;
        if (status < 500) {
            if (status == 402
                    || (status > 417 && status < 421)
                    || status > 424) {
                desc = getMessage("400.error.description");
            } else {
                desc = getMessage(status + ".error.description");
            }
        } else {
            switch (status) {
                case 501:
                    desc = getMessage("501.error.description");
                    break;
            }
        }

        if (desc == null) {
            desc = getMessage("default.error.description");
        }

        return desc;
    }

    protected List<Result.Error> parseErrors(Throwable exception, Locale locale, int status) {
        List<Result.Error> errors = Lists.newArrayList();

        Throwable cause = exception;
        while (cause != null) {
            StackTraceElement[] stackTraceElements = cause.getStackTrace();
            if (stackTraceElements != null && stackTraceElements.length > 0) {
                StackTraceElement stackTraceElement = stackTraceElements[0];
                String source = stackTraceElement.toString();
                Result.Error error = new Result.Error(
                        cause.getClass().getCanonicalName().hashCode(),
                        cause.getMessage());

                StringBuilder descBuilder = new StringBuilder();
                for (StackTraceElement element : stackTraceElements) {
                    descBuilder
                            .append(element.toString())
                            .append("\n");
                }

                error.setDescription(descBuilder.toString());
                error.setSource(source);

                errors.add(error);
            }
            cause = cause.getCause();
        }

        return errors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response toResponse(Throwable exception) {
        int status = getHttpStatus(exception);

        ErrorMessage message = new ErrorMessage(false);

        if (exception instanceof MappableException
                && exception.getCause() != null) {
            exception = exception.getCause();
        }

        Locale locale = Locale.getDefault();
        try {
            locale = requestProvider.get().getLanguage();
        } catch (Exception e) {
            // no op
        }
        message.setCode(exception.getClass().getCanonicalName().hashCode());
        message.setStatus(status);
        message.setThrowable(exception);
        message.setMessage(parseMessage(exception, locale, status));
        message.setDescription(parseDescription(exception, locale, status));
        if (application.getMode().isDev() && status == 500) {
            message.setErrors(parseErrors(exception, locale, status));
        }

        if (status == 500) {
            logger.error("系统发生错误", exception);
        }

        return Response.status(status).entity(message).build();
    }
}

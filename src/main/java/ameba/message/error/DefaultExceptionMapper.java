package ameba.message.error;

import ameba.core.Application;
import ameba.core.Requests;
import ameba.util.ClassUtils;
import ameba.util.Result;
import com.google.common.collect.Lists;
import org.glassfish.jersey.server.internal.process.MappableException;
import org.glassfish.jersey.server.spi.ResponseErrorMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
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

    public static final String BEFORE_EXCEPTION_KEY = DefaultExceptionMapper.class.getName() + ".BEFORE_EXCEPTION";
    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionMapper.class);
    @Inject
    private Application application;
    @Context
    private ResourceInfo resourceInfo;

    protected int parseHttpStatus(Throwable exception) {
        return ErrorMessage.parseHttpStatus(exception);
    }

    protected String parseMessage(Throwable exception, int status) {
        return ErrorMessage.parseMessage(status);
    }

    protected String parseDescription(Throwable exception, int status) {
        return ErrorMessage.parseDescription(status);
    }

    protected List<Result.Error> parseErrors(Throwable exception, int status) {
        List<Result.Error> errors = Lists.newArrayList();
        boolean isDev = application.getMode().isDev();
        if (resourceInfo != null && (status == 500 || status == 400)) {
            Class clazz = resourceInfo.getResourceClass();
            if (clazz != null) {
                errors.add(new Result.Error(
                        exception.getClass().getCanonicalName().hashCode(),
                        exception.getMessage(),
                        null,
                        isDev ? ClassUtils.toString(clazz, resourceInfo.getResourceMethod()) : null
                ));
            }
        }

        if (isDev) {
            errors.addAll(ErrorMessage.parseErrors(exception, status));
        }

        return errors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response toResponse(Throwable exception) {
        int status = parseHttpStatus(exception);

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

        MediaType type = ExceptionMapperUtils.getResponseType();
        if (status == 500) {
            String uri = "";
            if (Requests.getRequest() != null) {
                uri = " > " + Requests.getUriInfo().getRequestUri();
            }
            logger.error(message.getMessage() + uri, exception);
        } else if (status == 404) {
            Requests.setProperty(BEFORE_EXCEPTION_KEY, exception);
        }

        return Response.status(status)
                .type(type)
                .entity(message).build();
    }
}

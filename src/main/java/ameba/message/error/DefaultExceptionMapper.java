package ameba.message.error;

import ameba.core.Application;
import ameba.util.Result;
import org.glassfish.jersey.server.internal.process.MappableException;
import org.glassfish.jersey.server.spi.ResponseErrorMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Priorities;
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
        return ErrorMessage.parseErrors(exception, status, application.getMode().isDev());
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

        if (status == 500) {
            logger.error("系统发生错误", exception);
        }

        return Response.status(status)
                .type(ExceptionMapperUtils.getResponseType())
                .entity(message).build();
    }
}

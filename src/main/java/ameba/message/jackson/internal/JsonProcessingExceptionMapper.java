package ameba.message.jackson.internal;

import ameba.core.Application;
import ameba.message.error.ErrorMessage;
import ameba.message.error.ExceptionMapperUtils;
import ameba.util.IOUtils;
import ameba.util.Result;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.glassfish.jersey.spi.ExceptionMappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.PersistenceException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * @author icode
 */
@Singleton
public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {
    private static final Logger logger = LoggerFactory.getLogger(JsonProcessingExceptionMapper.class);

    @Context
    private Provider<ExceptionMappers> exceptionMappers;
    @Inject
    private Application application;

    @Override
    public Response toResponse(JsonProcessingException exception) {
        Throwable throwable = exception;
        while (throwable != null) {
            if (throwable instanceof PersistenceException) {
                throwable = throwable.getCause();
                break;
            }
            throwable = throwable.getCause();
        }

        if (throwable != null) {
            return exceptionMappers.get().findMapping(throwable).toResponse(throwable);
        }

        logger.debug("Json Processing error", exception);
        String message = exception.getOriginalMessage();
        String desc = null;
        String source = null;
        if (application.getMode().isDev()) {
            desc = IOUtils.getStackTrace(exception);
            JsonLocation location = exception.getLocation();
            if (location != null) {
                source = "line: " + location.getLineNr() +
                        ", column: " + location.getColumnNr();
            } else {
                source = exception.getStackTrace()[0].toString();
            }
        }

        ErrorMessage errorMessage = ErrorMessage.fromStatus(Response.Status.BAD_REQUEST.getStatusCode());
        errorMessage.setThrowable(exception);
        errorMessage.setCode(exception.getClass().getCanonicalName().hashCode());

        errorMessage.addError(new Result.Error(
                errorMessage.getCode(),
                message != null ? message : exception.getMessage(),
                desc,
                source
        ));

        return Response.status(errorMessage.getStatus())
                .entity(errorMessage)
                .type(ExceptionMapperUtils.getResponseType())
                .build();
    }
}

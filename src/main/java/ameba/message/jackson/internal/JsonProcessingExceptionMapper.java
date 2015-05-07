package ameba.message.jackson.internal;

import ameba.message.error.ErrorMessage;
import ameba.message.error.ExceptionMapperUtils;
import ameba.util.ClassUtils;
import ameba.util.IOUtils;
import ameba.util.Result;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.persistence.PersistenceException;
import javax.ws.rs.container.ResourceInfo;
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
    private ResourceInfo resourceInfo;

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

        String message;
        String source = null;
        String desc;
        if (throwable != null) {
            logger.debug("Executing SQL error", throwable);
            message = throwable.getMessage();
            desc = IOUtils.getStackTrace(throwable);
            if (resourceInfo != null) {
                Class clazz = resourceInfo.getResourceClass();
                if (clazz != null)
                    source = ClassUtils.toString(clazz, resourceInfo.getResourceMethod());
            }
        } else {
            logger.debug("Json Processing error", exception);
            message = exception.getOriginalMessage();
            desc = IOUtils.getStackTrace(exception);
            JsonLocation location = exception.getLocation();
            if (location != null) {
                source = "line: " + location.getLineNr() +
                        ", column: " + location.getColumnNr();
            } else {
                source = exception.getStackTrace()[0].toString();
            }
        }
        ErrorMessage errorMessage;
        if (throwable != null) {
            errorMessage = ErrorMessage.fromStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            errorMessage.setThrowable(throwable);
            errorMessage.setCode(throwable.getClass().getCanonicalName().hashCode());
        } else {
            errorMessage = ErrorMessage.fromStatus(Response.Status.BAD_REQUEST.getStatusCode());
            errorMessage.setThrowable(exception);
            errorMessage.setCode(exception.getClass().getCanonicalName().hashCode());
        }

        errorMessage.setErrors(Lists.newArrayList(new Result.Error(
                errorMessage.getCode(),
                message != null ? message : exception.getMessage(),
                desc,
                source
        )));

        return Response.status(errorMessage.getStatus())
                .entity(errorMessage)
                .type(ExceptionMapperUtils.getResponseType())
                .build();
    }
}

package ameba.message.jackson.internal;

import ameba.message.error.ErrorMessage;
import ameba.message.error.ExceptionMapperUtils;
import ameba.util.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * @author icode
 */
@Singleton
public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {
    private static final Logger logger = LoggerFactory.getLogger(JsonProcessingExceptionMapper.class);

    @Override
    public Response toResponse(JsonProcessingException exception) {
        logger.debug("Json Processing error", exception);
        ErrorMessage errorMessage = ErrorMessage.fromStatus(Response.Status.BAD_REQUEST.getStatusCode());
        errorMessage.setThrowable(exception);
        errorMessage.setCode(exception.getClass().getCanonicalName().hashCode());
        String message = exception.getOriginalMessage();
        errorMessage.setErrors(Lists.newArrayList(new Result.Error(
                errorMessage.getCode(),
                message != null ? message : exception.getMessage(),
                null,
                "line: " + exception.getLocation().getLineNr() +
                        ", column: " + exception.getLocation().getColumnNr()
        )));

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorMessage)
                .type(ExceptionMapperUtils.getResponseType())
                .build();
    }
}

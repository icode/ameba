package ameba.validation;

import ameba.message.error.ErrorMessage;
import ameba.message.error.ExceptionMapperUtils;
import ameba.util.Result;
import org.glassfish.jersey.server.validation.internal.LocalizationMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Singleton;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Priorities;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.List;

/**
 * @author icode
 */
@Priority(Priorities.USER)
@Singleton
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger logger = LoggerFactory.getLogger(ValidationExceptionMapper.class);

    @Override
    public Response toResponse(final ConstraintViolationException exception) {
        logger.trace(LocalizationMessages.CONSTRAINT_VIOLATIONS_ENCOUNTERED(), exception);
        Response.Status status = ValidationHelper.getResponseStatus(exception);
        ErrorMessage errorMessage = ErrorMessage.fromStatus(status.getStatusCode());
        errorMessage.setThrowable(exception);
        errorMessage.setCode(exception.getClass().getCanonicalName().hashCode());

        List<Result.Error> errors = ValidationHelper.constraintViolationToValidationErrors(exception);

        errorMessage.setErrors(errors);

        return Response.status(status)
                .entity(errorMessage)
                .type(ExceptionMapperUtils.getResponseType())
                .build();
    }
}


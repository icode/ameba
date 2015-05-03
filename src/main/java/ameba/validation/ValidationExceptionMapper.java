package ameba.validation;

import ameba.message.error.ErrorMessage;
import ameba.util.Result;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.validation.ValidationError;
import org.glassfish.jersey.server.validation.internal.LocalizationMessages;
import org.glassfish.jersey.spi.ExtendedExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.ws.rs.Priorities;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author icode
 */
@Priority(Priorities.USER)
public class ValidationExceptionMapper implements ExtendedExceptionMapper<ValidationException> {

    private static final Logger logger = LoggerFactory.getLogger(ValidationExceptionMapper.class);

    @Context
    private HttpHeaders headers;
    @Context
    private ExtendedUriInfo uriInfo;

    @Override
    public Response toResponse(final ValidationException exception) {
        logger.debug(LocalizationMessages.CONSTRAINT_VIOLATIONS_ENCOUNTERED(), exception);

        final ConstraintViolationException cve = (ConstraintViolationException) exception;
        Response.Status status = ValidationHelper.getResponseStatus(cve);
        ErrorMessage errorMessage = new ErrorMessage();
        int statusCode = status.getStatusCode();
        errorMessage.setStatus(statusCode);
        errorMessage.setThrowable(exception);
        errorMessage.setCode(exception.getClass().getCanonicalName().hashCode());
        errorMessage.setMessage(ErrorMessage.getLocaleMessage(statusCode));
        errorMessage.setDescription(ErrorMessage.getLocaleDescription(statusCode));


        List<ValidationError> validationErrors = ValidationHelper.constraintViolationToValidationErrors(cve);
        Response.ResponseBuilder response = Response.status(status);

        List<MediaType> accepts = headers.getAcceptableMediaTypes();
        if (accepts != null && accepts.size() > 0) {
            MediaType m = accepts.get(0);
            response.type(m);
        } else {
            response.type(headers.getMediaType());
        }

        List<Result.Error> errors = Lists.transform(validationErrors,
                new Function<ValidationError, Result.Error>() {

                    @Override
                    public Result.Error apply(final ValidationError error) {
                        return new Result.Error(
                                error.getMessageTemplate().hashCode(),
                                error.getMessage(),
                                null,
                                error.getPath()
                        );
                    }
                });

        errorMessage.setErrors(errors);

        return response.entity(errorMessage).build();
    }

    @Override
    public boolean isMappable(ValidationException exception) {
        return exception instanceof ConstraintViolationException;
    }
}


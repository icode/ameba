package ameba.db;

import ameba.core.Application;
import ameba.message.error.ErrorMessage;
import ameba.message.error.ExceptionMapperUtils;
import ameba.util.Result;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.PersistenceException;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.List;

/**
 * <p>PersistenceExceptionMapper class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
@Singleton
public class PersistenceExceptionMapper implements ExceptionMapper<PersistenceException> {
    private static final Logger logger = LoggerFactory.getLogger(PersistenceExceptionMapper.class);

    @Context
    private ResourceInfo resourceInfo;
    @Inject
    private Application.Mode mode;

    /**
     * {@inheritDoc}
     */
    @Override
    public Response toResponse(PersistenceException exception) {
        logger.error("Executing SQL error", exception);

        ErrorMessage errorMessage = ErrorMessage.fromStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        errorMessage.setThrowable(exception);
        errorMessage.setCode(Hashing.murmur3_32().hashUnencodedChars(exception.getClass().getName()).toString());

        boolean isDev = mode.isDev();
        List<ErrorMessage.Error> errors = Lists.newArrayList();

        errors.add(new Result.Error(
                errorMessage.getCode(),
                exception.getMessage(),
                null,
                isDev ? ErrorMessage.parseSource(resourceInfo) : null
        ));

        if (isDev) {
            errors.addAll(ErrorMessage.parseErrors(exception, errorMessage.getStatus()));
        }
        errorMessage.setErrors(errors);

        return Response.status(errorMessage.getStatus())
                .entity(errorMessage)
                .type(ExceptionMapperUtils.getResponseType())
                .build();
    }
}

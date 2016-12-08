package ameba.db.ebean.jackson;

import com.avaje.ebean.text.json.JsonIOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.glassfish.jersey.spi.ExceptionMappers;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * <p>JsonIOExceptionMapper class.</p>
 *
 * @author icode
 *
 */
@Singleton
public class JsonIOExceptionMapper implements ExceptionMapper<JsonIOException> {

    @Inject
    private Provider<ExceptionMappers> mappers;

    /**
     * {@inheritDoc}
     */
    @Override
    public Response toResponse(JsonIOException exception) {
        Throwable throwable = exception;
        if (exception.getCause() instanceof JsonProcessingException) {
            throwable = exception.getCause();
        }

        return mappers.get().findMapping(throwable).toResponse(throwable);
    }
}

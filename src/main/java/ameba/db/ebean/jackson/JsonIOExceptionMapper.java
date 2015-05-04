package ameba.db.ebean.jackson;

import com.avaje.ebean.text.json.JsonIOException;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.inject.Singleton;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Providers;

/**
 * @author icode
 */
@Singleton
public class JsonIOExceptionMapper implements ExceptionMapper<JsonIOException> {

    @Context
    private Providers providers;

    @Override
    public Response toResponse(JsonIOException exception) {
        if (exception.getCause() instanceof JsonProcessingException) {
            return providers.getExceptionMapper(JsonProcessingException.class)
                    .toResponse((JsonProcessingException) exception.getCause());
        } else {
            return providers.getExceptionMapper(Throwable.class).toResponse(exception);
        }
    }
}

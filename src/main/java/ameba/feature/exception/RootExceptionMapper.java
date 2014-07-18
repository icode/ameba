package ameba.feature.exception;

import org.glassfish.jersey.spi.ExtendedExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Singleton;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * @author: ICode
 * @since: 13-8-17 下午2:00
 */
@Priority(Priorities.ENTITY_CODER)
@Singleton
public class RootExceptionMapper implements ExtendedExceptionMapper<Throwable> {

    private static final Logger logger = LoggerFactory.getLogger(RootExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        logger.error("发生错误", exception);
        return Response.serverError().entity(exception).build();
    }

    @Override
    public boolean isMappable(Throwable exception) {
        if (WebApplicationException.class.isInstance(exception)) {
            if (((WebApplicationException) exception).getResponse().getStatus() != 500) {
                return false;
            }
        }
        return true;
    }
}

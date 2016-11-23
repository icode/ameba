package ameba.db.ebean.filter;

import ameba.db.dsl.QuerySyntaxException;
import ameba.exception.UnprocessableEntityException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Providers;

/**
 * <p>QuerySyntaxExceptionMapper class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
@Singleton
public class QuerySyntaxExceptionMapper implements ExceptionMapper<QuerySyntaxException> {
    @Inject
    private Providers providers;

    /**
     * {@inheritDoc}
     */
    @Override
    public Response toResponse(QuerySyntaxException exception) {
        return providers.getExceptionMapper(Throwable.class)
                .toResponse(new UnprocessableEntityException(exception.getMessage(), exception));
    }
}

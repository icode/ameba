package ameba.db.migration;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;

/**
 * request filter for db change to merge
 *
 * @author icode
 * @since 0.1.6e
 */
public class DatabaseMigrationFilter implements ContainerRequestFilter {
    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

    }
}

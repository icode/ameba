package ameba.message.filtering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;

/**
 * @author icode
 */
@PreMatching
@Singleton
@Priority(0)
public class RequestLoggingFilter extends BaseLoggingFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    public RequestLoggingFilter(Logger logger, boolean printEntity) {
        super(logger, printEntity);
    }

    public RequestLoggingFilter(Logger logger, int maxEntitySize) {
        super(logger, maxEntitySize);
    }

    public RequestLoggingFilter() {
        this(logger, true);
    }

}

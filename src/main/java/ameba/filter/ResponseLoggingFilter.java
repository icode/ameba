package ameba.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.WriterInterceptor;

/**
 * @author icode
 */
@Priority(Integer.MAX_VALUE)
public class ResponseLoggingFilter extends BaseLoggingFilter implements ContainerResponseFilter, WriterInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(ResponseLoggingFilter.class);

    public ResponseLoggingFilter(Logger logger, boolean printEntity) {
        super(logger, printEntity);
    }

    public ResponseLoggingFilter(Logger logger, int maxEntitySize) {
        super(logger, maxEntitySize);
    }

    public ResponseLoggingFilter() {
        this(logger, true);
    }

}
